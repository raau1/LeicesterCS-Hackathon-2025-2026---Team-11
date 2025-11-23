package com.studybuddy.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.studybuddy.dto.SessionRequest;
import com.studybuddy.dto.SessionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private com.google.firebase.auth.FirebaseAuth firebaseAuth;

    @Autowired
    private RatingService ratingService;

    public SessionResponse createSession(SessionRequest request, String creatorUid) {
        try {
            // Get creator info from Firestore first, then fallback to Firebase Auth
            DocumentSnapshot userDoc = firestore.collection("users").document(creatorUid).get().get();
            String creatorName = null;

            if (userDoc.exists()) {
                creatorName = userDoc.getString("name");
            }

            // Fallback to Firebase Auth displayName if Firestore doesn't have the name
            if (creatorName == null || creatorName.isEmpty()) {
                try {
                    com.google.firebase.auth.UserRecord userRecord = firebaseAuth.getUser(creatorUid);
                    creatorName = userRecord.getDisplayName();
                    if (creatorName == null || creatorName.isEmpty()) {
                        creatorName = userRecord.getEmail();
                    }
                } catch (Exception e) {
                    creatorName = "Unknown User";
                }
            }

            // Create session document
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("title", request.getTitle());
            sessionData.put("module", request.getModule());
            sessionData.put("year", request.getYear());

            // Handle startNow - use current date/time if starting immediately
            boolean isLive = request.getStartNow() != null && request.getStartNow();
            if (isLive) {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                sessionData.put("date", now.toLocalDate().toString());
                sessionData.put("time", now.toLocalTime().toString());
            } else {
                if (request.getDate() == null || request.getTime() == null) {
                    throw new RuntimeException("Date and time are required when not starting now");
                }
                sessionData.put("date", request.getDate());
                sessionData.put("time", request.getTime());
            }

            sessionData.put("duration", request.getDuration().longValue());
            sessionData.put("maxParticipants", request.getMaxParticipants().longValue());
            sessionData.put("preferences", request.getPreferences() != null ? request.getPreferences() : "");
            sessionData.put("description", request.getDescription());
            sessionData.put("isLive", isLive);
            sessionData.put("creatorId", creatorUid);
            sessionData.put("creatorName", creatorName);
            sessionData.put("participants", Arrays.asList(creatorUid));
            sessionData.put("requests", new ArrayList<String>());
            sessionData.put("status", "open");
            sessionData.put("createdAt", System.currentTimeMillis());
            sessionData.put("updatedAt", System.currentTimeMillis());

            DocumentReference docRef = firestore.collection("sessions").document();
            docRef.set(sessionData).get();

            return mapToSessionResponse(docRef.getId(), sessionData);
        } catch (Exception e) {
            throw new RuntimeException("Error creating session: " + e.getMessage());
        }
    }

    public List<SessionResponse> getAllSessions(String year, String module) {
        try {
            Query query = firestore.collection("sessions").whereEqualTo("status", "open");

            if (year != null && !year.isEmpty()) {
                query = query.whereEqualTo("year", year);
            }

            if (module != null && !module.isEmpty()) {
                query = query.whereEqualTo("module", module);
            }

            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            System.out.println("DEBUG: Found " + documents.size() + " sessions with status=open");

            return documents.stream()
                    .map(doc -> {
                        try {
                            System.out.println("DEBUG: Mapping session " + doc.getId());
                            return mapToSessionResponse(doc.getId(), doc.getData());
                        } catch (Exception e) {
                            System.err.println("DEBUG ERROR: Failed to map session " + doc.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(s -> s != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("DEBUG ERROR: getAllSessions failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error getting sessions: " + e.getMessage());
        }
    }

    public SessionResponse getSessionById(String sessionId) {
        try {
            DocumentSnapshot doc = firestore.collection("sessions").document(sessionId).get().get();
            if (!doc.exists()) {
                throw new RuntimeException("Session not found");
            }
            return mapToSessionResponse(doc.getId(), doc.getData());
        } catch (Exception e) {
            throw new RuntimeException("Error getting session: " + e.getMessage());
        }
    }

    public List<SessionResponse> getSessionsByCreator(String creatorUid) {
        try {
            QuerySnapshot snapshot = firestore.collection("sessions")
                    .whereEqualTo("creatorId", creatorUid)
                    .get().get();

            return snapshot.getDocuments().stream()
                    .map(doc -> mapToSessionResponse(doc.getId(), doc.getData()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting user sessions: " + e.getMessage());
        }
    }

    public List<SessionResponse> getSessionsJoined(String userUid) {
        try {
            QuerySnapshot snapshot = firestore.collection("sessions")
                    .whereArrayContains("participants", userUid)
                    .get().get();

            return snapshot.getDocuments().stream()
                    .map(doc -> mapToSessionResponse(doc.getId(), doc.getData()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error getting joined sessions: " + e.getMessage());
        }
    }

    public void requestToJoin(String sessionId, String userUid) {
        try {
            DocumentReference docRef = firestore.collection("sessions").document(sessionId);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                throw new RuntimeException("Session not found");
            }

            List<String> participants = (List<String>) doc.get("participants");
            List<String> requests = (List<String>) doc.get("requests");
            Long maxParticipants = doc.getLong("maxParticipants");

            if (participants != null && participants.contains(userUid)) {
                throw new RuntimeException("You are already a participant in this session");
            }

            if (requests != null && requests.contains(userUid)) {
                throw new RuntimeException("Request already pending");
            }

            // Check if session is full
            if (participants != null && maxParticipants != null && participants.size() >= maxParticipants) {
                throw new RuntimeException("Session is full");
            }

            // Add to pending requests
            docRef.update("requests", FieldValue.arrayUnion(userUid)).get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void acceptRequest(String sessionId, String userUid, String creatorUid) {
        try {
            DocumentReference docRef = firestore.collection("sessions").document(sessionId);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                throw new RuntimeException("Session not found");
            }

            if (!creatorUid.equals(doc.getString("creatorId"))) {
                throw new RuntimeException("Only creator can accept requests");
            }

            // Move user from requests to participants
            docRef.update(
                    "requests", FieldValue.arrayRemove(userUid),
                    "participants", FieldValue.arrayUnion(userUid),
                    "updatedAt", System.currentTimeMillis()
            ).get();

            // Check if session became full
            doc = docRef.get().get();
            List<String> participants = (List<String>) doc.get("participants");
            Long maxParticipants = doc.getLong("maxParticipants");

            if (participants != null && maxParticipants != null && participants.size() >= maxParticipants) {
                docRef.update("status", "full").get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void declineRequest(String sessionId, String userUid, String creatorUid) {
        try {
            DocumentReference docRef = firestore.collection("sessions").document(sessionId);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                throw new RuntimeException("Session not found");
            }

            if (!creatorUid.equals(doc.getString("creatorId"))) {
                throw new RuntimeException("Only creator can decline requests");
            }

            // Remove user from requests
            docRef.update("requests", FieldValue.arrayRemove(userUid)).get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public void deleteSession(String sessionId, String userUid) {
        try {
            DocumentReference docRef = firestore.collection("sessions").document(sessionId);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) {
                throw new RuntimeException("Session not found");
            }

            if (!userUid.equals(doc.getString("creatorId"))) {
                throw new RuntimeException("Only creator can delete session");
            }

            docRef.delete().get();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private SessionResponse mapToSessionResponse(String id, Map<String, Object> data) {
        SessionResponse response = new SessionResponse();
        response.setId(id);
        response.setTitle((String) data.get("title"));
        response.setModule((String) data.get("module"));
        response.setYear((String) data.get("year"));
        response.setDate((String) data.get("date"));
        response.setTime((String) data.get("time"));
        Long duration = (Long) data.get("duration");
        response.setDuration(duration != null ? duration.intValue() : 0);

        Long maxParticipants = (Long) data.get("maxParticipants");
        response.setMaxParticipants(maxParticipants != null ? maxParticipants.intValue() : 1);

        // Convert preferences to list (handle both String and ArrayList from Firestore)
        Object prefsData = data.get("preferences");
        if (prefsData instanceof String) {
            String prefsString = (String) prefsData;
            if (!prefsString.isEmpty()) {
                response.setPreferences(Arrays.asList(prefsString.split(",")));
            } else {
                response.setPreferences(new ArrayList<>());
            }
        } else if (prefsData instanceof List) {
            response.setPreferences((List<String>) prefsData);
        } else {
            response.setPreferences(new ArrayList<>());
        }

        response.setDescription((String) data.get("description"));
        response.setIsLive(data.get("isLive") != null ? (Boolean) data.get("isLive") : false);
        response.setStatus((String) data.get("status"));
        response.setCreatorId((String) data.get("creatorId"));
        response.setCreatorName((String) data.get("creatorName"));

        List<String> participants = (List<String>) data.get("participants");
        int participantCount = participants != null ? participants.size() : 0;
        response.setParticipantCount(participantCount);
        response.setSpotsLeft(response.getMaxParticipants() - participantCount);
        response.setParticipants(participants);
        response.setJoinRequests((List<String>) data.get("requests"));

        // Fetch creator rating
        String creatorId = (String) data.get("creatorId");
        if (creatorId != null) {
            try {
                Map<String, Object> ratingStats = ratingService.getUserRatingStats(creatorId);
                response.setCreatorRating((Double) ratingStats.get("averageRating"));
                response.setCreatorRatingCount((Integer) ratingStats.get("ratingCount"));
            } catch (Exception e) {
                response.setCreatorRating(0.0);
                response.setCreatorRatingCount(0);
            }
        }

        return response;
    }
}
