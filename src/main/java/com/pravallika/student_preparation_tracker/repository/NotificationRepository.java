package com.pravallika.student_preparation_tracker.repository;

import com.pravallika.student_preparation_tracker.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // =========================================
    // GET ALL NOTIFICATIONS FOR A USER
    // =========================================

    List<Notification> findByUserEmailOrderByCreatedAtDesc(
            String userEmail
    );

    // =========================================
    // GET UNREAD NOTIFICATIONS FOR A USER
    // =========================================

    List<Notification> findByUserEmailAndReadFalseOrderByCreatedAtDesc(
            String userEmail
    );

    // =========================================
    // COUNT UNREAD NOTIFICATIONS
    // =========================================

    long countByUserEmailAndReadFalse(
            String userEmail
    );

    // =========================================
    // FIND ONE NOTIFICATION BELONGING
    // TO THE CURRENT USER
    // =========================================

    Optional<Notification> findByIdAndUserEmail(
            Long id,
            String userEmail
    );

    // =========================================
    // DELETE ALL NOTIFICATIONS BELONGING
    // TO THE CURRENT USER
    // =========================================

    void deleteByUserEmail(
            String userEmail
    );

    // =========================================
    // CHECK DUPLICATE DEADLINE NOTIFICATION
    // =========================================

    boolean existsByUserEmailAndStudyTaskIdAndType(
            String userEmail,
            Long studyTaskId,
            String type
    );
}