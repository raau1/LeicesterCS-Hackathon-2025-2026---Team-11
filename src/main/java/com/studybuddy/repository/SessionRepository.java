package com.studybuddy.repository;

import com.studybuddy.model.StudySession;
import com.studybuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<StudySession, Long> {

    List<StudySession> findByStatusOrderByCreatedAtDesc(StudySession.SessionStatus status);

    List<StudySession> findByStatusAndYearOrderByCreatedAtDesc(
            StudySession.SessionStatus status, String year);

    List<StudySession> findByStatusAndModuleOrderByCreatedAtDesc(
            StudySession.SessionStatus status, String module);

    List<StudySession> findByStatusAndYearAndModuleOrderByCreatedAtDesc(
            StudySession.SessionStatus status, String year, String module);

    List<StudySession> findByCreatorOrderByCreatedAtDesc(User creator);

    @Query("SELECT s FROM StudySession s JOIN s.participants p WHERE p = :user ORDER BY s.createdAt DESC")
    List<StudySession> findByParticipant(@Param("user") User user);

    @Query("SELECT s FROM StudySession s JOIN s.requests r WHERE r = :user ORDER BY s.createdAt DESC")
    List<StudySession> findByRequester(@Param("user") User user);
}
