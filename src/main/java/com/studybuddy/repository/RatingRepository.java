package com.studybuddy.repository;

import com.studybuddy.model.Rating;
import com.studybuddy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByToUser(User user);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.toUser = :user")
    Double getAverageRatingForUser(@Param("user") User user);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.toUser = :user")
    Long getRatingCountForUser(@Param("user") User user);
}
