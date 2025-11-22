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

    public SessionResponse createSession(SessionRequest request, String creatorUid) {
        try {
            // Get creator info
            DocumentSnapshot userDoc = firestore.collection("users").document(creatorUid).get().get();
            String creatorName = userDoc.getString("name");

            // Create session document
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("title", request.getTitle());
            sessionData.put("module", request.getModule());
            sessionData.put("year", request.getYear());
            sessionData.put("date", request.getDate().toString());
            sessionData.put("time", request.getTime().toString());
            sessionData.put("duration", request.getDuration());
            sessionData.put("location", request.getLocation());
            sessionData.put("maxParticipants", request.getMaxParticipants());
            sessionData.put("preferences", request.getPreferences() != null ? request.getPreferences() : new ArrayList<>());
            sessionData.put("description", request.getDescription());
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

            return documents.stream()
                    .map(doc -> mapToSessionResponse(doc.getId(), doc.getData()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
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

            if (participants != null && participants.contains(userUid)) {
                throw new RuntimeException("Already a participant");
            }

            if (requests != null && requests.contains(userUid)) {
                throw new RuntimeException("Already requested to join");
            }

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

            docRef.update(
                    "requests", FieldValue.arrayRemove(userUid),
                    "participants", FieldValue.arrayUnion(userUid),
                    "updatedAt", System.currentTimeMillis()
            ).get();

            // Check if full
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
        response.setDate(java.time.LocalDate.parse((String) data.get("date")));
        response.setTime(java.time.LocalTime.parse((String) data.get("time")));
        response.setDuration(((Long) data.get("duration")).intValue());
        response.setLocation((String) data.get("location"));
        response.setMaxParticipants(((Long) data.get("maxParticipants")).intValue());
        response.setPreferences((List<String>) data.get("preferences"));
        response.setDescription((String) data.get("description"));
        response.setCreatorId((String) data.get("creatorId"));
        response.setCreatorName((String) data.get("creatorName"));

        List<String> participants = (List<String>) data.get("participants");
        int participantCount = participants != null ? participants.size() : 0;
        response.setParticipantCount(participantCount);
        response.setSpotsLeft(response.getMaxParticipants() - participantCount);
        response.setStatus((String) data.get("status"));

        return response;
    }
}
