package com.studybuddy.service;

import com.studybuddy.dto.SessionRequest;
import com.studybuddy.dto.SessionResponse;
import com.studybuddy.model.StudySession;
import com.studybuddy.model.User;
import com.studybuddy.repository.SessionRepository;
import com.studybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public SessionResponse createSession(SessionRequest request, String userEmail) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StudySession session = new StudySession();
        session.setTitle(request.getTitle());
        session.setModule(request.getModule());
        session.setYear(request.getYear());
        session.setDate(request.getDate());
        session.setTime(request.getTime());
        session.setDuration(request.getDuration());
        session.setLocation(request.getLocation());
        session.setMaxParticipants(request.getMaxParticipants());
        session.setPreferences(request.getPreferences());
        session.setDescription(request.getDescription());
        session.setCreator(creator);
        session.getParticipants().add(creator); // Creator is first participant

        session = sessionRepository.save(session);

        return SessionResponse.fromEntity(session);
    }

    public List<SessionResponse> getAllSessions(String year, String module) {
        List<StudySession> sessions;

        if (year != null && !year.isEmpty() && module != null && !module.isEmpty()) {
            sessions = sessionRepository.findByStatusAndYearAndModuleOrderByCreatedAtDesc(
                    StudySession.SessionStatus.OPEN, year, module);
        } else if (year != null && !year.isEmpty()) {
            sessions = sessionRepository.findByStatusAndYearOrderByCreatedAtDesc(
                    StudySession.SessionStatus.OPEN, year);
        } else if (module != null && !module.isEmpty()) {
            sessions = sessionRepository.findByStatusAndModuleOrderByCreatedAtDesc(
                    StudySession.SessionStatus.OPEN, module);
        } else {
            sessions = sessionRepository.findByStatusOrderByCreatedAtDesc(StudySession.SessionStatus.OPEN);
        }

        return sessions.stream()
                .map(SessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public SessionResponse getSessionById(Long id) {
        StudySession session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return SessionResponse.fromEntity(session);
    }

    public List<SessionResponse> getSessionsByCreator(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sessionRepository.findByCreatorOrderByCreatedAtDesc(user).stream()
                .map(SessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<SessionResponse> getSessionsJoined(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sessionRepository.findByParticipant(user).stream()
                .map(SessionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void requestToJoin(Long sessionId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StudySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getParticipants().contains(user)) {
            throw new RuntimeException("Already a participant");
        }

        if (session.getRequests().contains(user)) {
            throw new RuntimeException("Already requested to join");
        }

        session.getRequests().add(user);
        sessionRepository.save(session);
    }

    @Transactional
    public void acceptRequest(Long sessionId, Long userId, String creatorEmail) {
        StudySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getCreator().getEmail().equals(creatorEmail)) {
            throw new RuntimeException("Only creator can accept requests");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        session.getRequests().remove(user);
        session.getParticipants().add(user);

        // Check if session is full
        if (session.getParticipants().size() >= session.getMaxParticipants()) {
            session.setStatus(StudySession.SessionStatus.FULL);
        }

        sessionRepository.save(session);
    }

    @Transactional
    public void declineRequest(Long sessionId, Long userId, String creatorEmail) {
        StudySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getCreator().getEmail().equals(creatorEmail)) {
            throw new RuntimeException("Only creator can decline requests");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        session.getRequests().remove(user);
        sessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(Long sessionId, String userEmail) {
        StudySession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getCreator().getEmail().equals(userEmail)) {
            throw new RuntimeException("Only creator can delete session");
        }

        sessionRepository.delete(session);
    }
}
