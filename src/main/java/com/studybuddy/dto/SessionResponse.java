package com.studybuddy.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SessionResponse {
    private String id;
    private String title;
    private String module;
    private String year;
    private LocalDate date;
    private LocalTime time;
    private Integer duration;
    private Integer maxParticipants;
    private List<String> preferences;
    private String description;
    private String creatorId;
    private String creatorName;
    private Integer participantCount;
    private Integer spotsLeft;
    private String status;
    private List<String> participants;
    private List<String> requests;
    private Boolean isLive;
}
