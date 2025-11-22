package com.studybuddy.dto;

import com.studybuddy.model.User;
import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String year;
    private List<String> modules;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setYear(user.getYear());
        response.setModules(user.getModules());
        return response;
    }
}
