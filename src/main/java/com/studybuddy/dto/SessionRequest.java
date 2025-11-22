package com.studybuddy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SessionRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String module;

    @NotBlank
    private String year;

    // These can be null if startNow is true
    private LocalDate date;

    private LocalTime time;

    @NotNull
    private Integer duration;

    private Integer maxParticipants = 4;

    private List<String> preferences;

    private String description;

    // Whether to start the session immediately
    private Boolean startNow = false;
}
