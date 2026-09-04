
package com.pravallika.student_preparation_tracker.repository;

import com.pravallika.student_preparation_tracker.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // Get all notifications for a user
    List<Notification> findByUserEmailOrderByCreatedAtDesc(
            String userEmail
    );

    // Get unread notifications for a user
    List<Notification> findByUserEmailAndReadFalseOrderByCreatedAtDesc(
            String userEmail
    );

    // Count unread notifications
    long countByUserEmailAndReadFalse(
            String userEmail
    );

    // Find one notification belonging to the current user
    Optional<Notification> findByIdAndUserEmail(
            Long id,
            String userEmail
    );

    // Delete all notifications belonging to the current user
    void deleteByUserEmail(
            String userEmail
    );

    // Check whether a study notification already exists
    boolean existsByUserEmailAndStudyTaskIdAndType(
            String userEmail,
            Long studyTaskId,
            String type
    );
}
