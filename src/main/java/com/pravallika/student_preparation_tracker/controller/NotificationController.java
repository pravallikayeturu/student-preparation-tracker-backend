package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.entity.Notification;
import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.repository.NotificationRepository;
import com.pravallika.student_preparation_tracker.repository.StudyTaskRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    private final StudyTaskRepository studyTaskRepository;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public NotificationController(
            NotificationRepository notificationRepository,
            StudyTaskRepository studyTaskRepository) {

        this.notificationRepository = notificationRepository;
        this.studyTaskRepository = studyTaskRepository;
    }

    // =====================================================
    // GET CURRENT USER'S NOTIFICATIONS
    // =====================================================

    @GetMapping
    public ResponseEntity<?> getNotifications(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        List<Notification> notifications =
                notificationRepository
                        .findByUserEmailOrderByCreatedAtDesc(
                                userEmail
                        );

        List<Map<String, Object>> response =
                createNotificationResponse(notifications);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // GET CURRENT USER'S UNREAD NOTIFICATIONS
    // =====================================================

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        List<Notification> notifications =
                notificationRepository
                        .findByUserEmailAndReadFalseOrderByCreatedAtDesc(
                                userEmail
                        );

        List<Map<String, Object>> response =
                createNotificationResponse(notifications);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // ADD STUDY TASK DEADLINE TO NOTIFICATION RESPONSE
    // =====================================================

    private List<Map<String, Object>> createNotificationResponse(
            List<Notification> notifications) {

        List<Map<String, Object>> response =
                new ArrayList<>();

        for (Notification notification : notifications) {

            Map<String, Object> item =
                    new HashMap<>();

            item.put(
                    "id",
                    notification.getId()
            );

            item.put(
                    "userEmail",
                    notification.getUserEmail()
            );

            item.put(
                    "type",
                    notification.getType()
            );

            item.put(
                    "title",
                    notification.getTitle()
            );

            item.put(
                    "message",
                    notification.getMessage()
            );

            item.put(
                    "studyTaskId",
                    notification.getStudyTaskId()
            );

            item.put(
                    "read",
                    notification.isRead()
            );

            item.put(
                    "createdAt",
                    notification.getCreatedAt()
            );

            // =================================================
            // GET DEADLINE FROM THE ORIGINAL STUDY TASK
            // =================================================

            if (notification.getStudyTaskId() != null) {

                Optional<StudyTask> optionalTask =
                        studyTaskRepository.findByIdAndUserEmail(
                                notification.getStudyTaskId(),
                                notification.getUserEmail()
                        );

                if (optionalTask.isPresent()) {

                    StudyTask task =
                            optionalTask.get();

                    item.put(
                            "deadline",
                            task.getDeadline()
                    );
                }
            }

            response.add(item);
        }

        return response;
    }

    // =====================================================
    // GET UNREAD COUNT
    // =====================================================

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        long unreadCount =
                notificationRepository
                        .countByUserEmailAndReadFalse(
                                userEmail
                        );

        return ResponseEntity.ok(unreadCount);
    }

    // =====================================================
    // MARK ONE NOTIFICATION AS READ
    // =====================================================

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        Optional<Notification> optionalNotification =
                notificationRepository
                        .findByIdAndUserEmail(
                                id,
                                userEmail
                        );

        if (optionalNotification.isEmpty()) {

            return ResponseEntity
                    .status(404)
                    .body("Notification not found.");
        }

        Notification notification =
                optionalNotification.get();

        notification.setRead(true);

        Notification savedNotification =
                notificationRepository.save(
                        notification
                );

        return ResponseEntity.ok(savedNotification);
    }

    // =====================================================
    // MARK ONE NOTIFICATION AS UNREAD
    // =====================================================

    @PutMapping("/{id}/unread")
    public ResponseEntity<?> markAsUnread(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        Optional<Notification> optionalNotification =
                notificationRepository
                        .findByIdAndUserEmail(
                                id,
                                userEmail
                        );

        if (optionalNotification.isEmpty()) {

            return ResponseEntity
                    .status(404)
                    .body("Notification not found.");
        }

        Notification notification =
                optionalNotification.get();

        notification.setRead(false);

        Notification savedNotification =
                notificationRepository.save(
                        notification
                );

        return ResponseEntity.ok(savedNotification);
    }

    // =====================================================
    // MARK ALL NOTIFICATIONS AS READ
    // =====================================================

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        List<Notification> notifications =
                notificationRepository
                        .findByUserEmailOrderByCreatedAtDesc(
                                userEmail
                        );

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);

        return ResponseEntity.ok(
                "All notifications marked as read."
        );
    }

    // =====================================================
    // DELETE ONE NOTIFICATION
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        Optional<Notification> optionalNotification =
                notificationRepository
                        .findByIdAndUserEmail(
                                id,
                                userEmail
                        );

        if (optionalNotification.isEmpty()) {

            return ResponseEntity
                    .status(404)
                    .body("Notification not found.");
        }

        Notification notification =
                optionalNotification.get();

        notificationRepository.delete(notification);

        return ResponseEntity.ok(
                "Notification deleted successfully."
        );
    }

    // =====================================================
    // CLEAR ALL CURRENT USER'S NOTIFICATIONS
    // =====================================================

    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAll(
            Authentication authentication) {

        if (authentication == null
                || authentication.getName() == null) {

            return ResponseEntity
                    .status(401)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        notificationRepository.deleteByUserEmail(
                userEmail
        );

        return ResponseEntity.ok(
                "All notifications cleared successfully."
        );
    }
}