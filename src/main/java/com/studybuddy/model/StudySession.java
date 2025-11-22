package com.studybuddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String module;

    @NotBlank
    @Column(nullable = false)
    private String year;

    @NotNull
    @Column(name = "session_date", nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "session_time", nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private Integer duration; // in minutes

    @NotBlank
    @Column(nullable = false)
    private String location;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @ElementCollection
    @CollectionTable(name = "session_preferences", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "preference")
    private List<String> preferences = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    // Creator relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // Participants
    @ManyToMany
    @JoinTable(
        name = "session_participants",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

    // Pending join requests
    @ManyToMany
    @JoinTable(
        name = "session_requests",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> requests = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.OPEN;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SessionStatus {
        OPEN, FULL, COMPLETED, CANCELLED
    }
}
