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

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime time;

    @NotNull
    private Integer duration;

    @NotBlank
    private String location;

    private Integer maxParticipants = 4;

    private List<String> preferences;

    private String description;
}
