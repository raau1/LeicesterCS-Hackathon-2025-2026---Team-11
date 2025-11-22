package com.studybuddy.service;

import com.studybuddy.dto.UserResponse;
import com.studybuddy.model.User;
import com.studybuddy.repository.RatingRepository;
import com.studybuddy.repository.SessionRepository;
import com.studybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RatingRepository ratingRepository;

    public UserResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public Map<String, Object> getUserStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("sessionsCreated", sessionRepository.findByCreatorOrderByCreatedAtDesc(user).size());
        stats.put("sessionsJoined", sessionRepository.findByParticipant(user).size());

        Double avgRating = ratingRepository.getAverageRatingForUser(user);
        Long ratingCount = ratingRepository.getRatingCountForUser(user);

        stats.put("averageRating", avgRating != null ? avgRating : 0.0);
        stats.put("ratingCount", ratingCount != null ? ratingCount : 0);

        return stats;
    }

    public void updateModules(String email, List<String> modules) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setModules(modules);
        userRepository.save(user);
    }
}
