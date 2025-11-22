package com.studybuddy.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String year;
    private List<String> modules;
}
