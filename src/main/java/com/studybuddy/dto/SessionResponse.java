package com.studybuddy.dto;

import com.studybuddy.model.StudySession;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SessionResponse {
    private Long id;
    private String title;
    private String module;
    private String year;
    private LocalDate date;
    private LocalTime time;
    private Integer duration;
    private String location;
    private Integer maxParticipants;
    private List<String> preferences;
    private String description;
    private Long creatorId;
    private String creatorName;
    private Integer participantCount;
    private Integer spotsLeft;
    private String status;

    public static SessionResponse fromEntity(StudySession session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setModule(session.getModule());
        response.setYear(session.getYear());
        response.setDate(session.getDate());
        response.setTime(session.getTime());
        response.setDuration(session.getDuration());
        response.setLocation(session.getLocation());
        response.setMaxParticipants(session.getMaxParticipants());
        response.setPreferences(session.getPreferences());
        response.setDescription(session.getDescription());
        response.setCreatorId(session.getCreator().getId());
        response.setCreatorName(session.getCreator().getName());
        response.setParticipantCount(session.getParticipants().size());
        response.setSpotsLeft(session.getMaxParticipants() - session.getParticipants().size());
        response.setStatus(session.getStatus().name().toLowerCase());
        return response;
    }
}
