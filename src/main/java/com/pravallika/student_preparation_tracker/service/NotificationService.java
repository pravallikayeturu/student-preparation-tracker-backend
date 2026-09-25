package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.Notification;
import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.entity.User;
import com.pravallika.student_preparation_tracker.repository.NotificationRepository;
import com.pravallika.student_preparation_tracker.repository.StudyTaskRepository;
import com.pravallika.student_preparation_tracker.repository.UserRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {

    private final StudyTaskRepository studyTaskRepository;
    private final NotificationRepository notificationRepository;
    private final BrevoEmailService brevoEmailService;
    private final UserRepository userRepository;
    private final WebPushService webPushService;

    // =====================================================
    // INDIA TIMEZONE
    // =====================================================

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public NotificationService(
            StudyTaskRepository studyTaskRepository,
            NotificationRepository notificationRepository,
            BrevoEmailService brevoEmailService,
            UserRepository userRepository,
            WebPushService webPushService) {

        this.studyTaskRepository = studyTaskRepository;
        this.notificationRepository = notificationRepository;
        this.brevoEmailService = brevoEmailService;
        this.userRepository = userRepository;
        this.webPushService = webPushService;
    }

    // =====================================================
    // CHECK UPCOMING TASKS
    // RUNS EVERY MINUTE
    // =====================================================

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkUpcomingTasks() {

        // IMPORTANT:
        // Render server may use UTC.
        // Your task times are entered in India time.
        // So we explicitly use Asia/Kolkata here.

        LocalDateTime now =
                LocalDateTime.now(INDIA_ZONE);

        LocalDate today =
                LocalDate.now(INDIA_ZONE);

        System.out.println();
        System.out.println("========== NOTIFICATION CHECK ==========");
        System.out.println("Current India time: " + now);

        // =====================================================
        // 1. CHECK STUDY REMINDERS
        // =====================================================

        List<StudyTask> tasks =
                studyTaskRepository
                        .findByOriginalReadingDateAndReminderSentFalse(
                                today
                        );

        System.out.println(
                "Pending study reminder tasks: "
                        + tasks.size()
        );

        for (StudyTask task : tasks) {

            if (task == null) {
                continue;
            }

            if (task.getOriginalReadingDate() == null
                    || task.getOriginalStartTime() == null
                    || task.getUserEmail() == null
                    || task.getUserEmail().trim().isEmpty()) {

                System.out.println(
                        "Skipping task ID "
                                + task.getId()
                                + " because required information is missing."
                );

                continue;
            }

            // =================================================
            // GET USER SETTINGS
            // =================================================

            User user =
                    userRepository
                            .findByEmail(task.getUserEmail())
                            .orElse(null);

            if (user == null) {

                System.out.println(
                        "Skipping task ID "
                                + task.getId()
                                + " because user was not found."
                );

                continue;
            }

            // =================================================
            // TASK REMINDERS OFF
            // =================================================

            if (!Boolean.TRUE.equals(
                    user.isTaskReminders())) {

                System.out.println(
                        "Task reminders are OFF for user: "
                                + task.getUserEmail()
                );

                continue;
            }

            // =================================================
            // ORIGINAL START DATE + TIME
            // =================================================

            LocalDateTime originalStartDateTime =
                    LocalDateTime.of(
                            task.getOriginalReadingDate(),
                            task.getOriginalStartTime()
                    );

            // =================================================
            // CURRENT INDIA TIME - MINUTE PRECISION
            // =================================================

            LocalDateTime currentMinute =
                    now.truncatedTo(
                            ChronoUnit.MINUTES
                    );

            // =================================================
            // CALCULATE MINUTES UNTIL ORIGINAL START
            // =================================================

            long minutesUntilStart =
                    ChronoUnit.MINUTES.between(
                            currentMinute,
                            originalStartDateTime
                    );

            System.out.println(
                    "Task ID: "
                            + task.getId()
                            + " | Subject: "
                            + task.getSubject()
                            + " | Start: "
                            + originalStartDateTime
                            + " | Minutes until start: "
                            + minutesUntilStart
                            + " | Deadline: "
                            + task.getDeadline()
            );

            // =================================================
            // STUDY REMINDER
            // 10 MINUTES BEFORE ORIGINAL START TIME
            // =================================================

            if (minutesUntilStart <= 10
                    && minutesUntilStart >= 0
                    && !task.isReminderSent()) {

                System.out.println(
                        "Creating study reminder for task ID: "
                                + task.getId()
                );

                // =================================================
                // CREATE IN-APP NOTIFICATION
                // =================================================

                createStudyNotification(task);

                // =================================================
                // SEND BROWSER PUSH NOTIFICATION
                // =================================================

                try {

                    webPushService.sendPushNotification(
                            task.getUserEmail(),
                            "Study Reminder",
                            "Your study task \""
                                    + task.getSubject()
                                    + "\" starts in 10 minutes."
                    );

                    System.out.println(
                            "Study reminder push notification sent."
                    );

                } catch (Exception pushException) {

                    System.err.println(
                            "Push notification sending failed: "
                                    + pushException.getMessage()
                    );
                }

                // =================================================
                // SEND EMAIL
                // ONLY IF EMAIL NOTIFICATIONS = ON
                // =================================================

                if (Boolean.TRUE.equals(
                        user.isEmailNotifications())) {

                    try {

                        brevoEmailService.sendStudyReminder(
                                task.getUserEmail(),
                                task.getSubject(),
                                task.getTopic(),
                                task.getOriginalReadingDate(),
                                task.getOriginalStartTime(),
                                task.getOriginalEndTime()
                        );

                        System.out.println(
                                "Study reminder email sent successfully."
                        );

                    } catch (Exception emailException) {

                        System.err.println(
                                "Email sending failed: "
                                        + emailException.getMessage()
                        );
                    }

                } else {

                    System.out.println(
                            "Email notifications are OFF. "
                                    + "Study reminder email not sent."
                    );
                }

                // =================================================
                // MARK STUDY REMINDER AS SENT
                // =================================================

                task.setReminderSent(true);

                studyTaskRepository.save(task);

                System.out.println(
                        "Study reminder marked as sent for task ID: "
                                + task.getId()
                );
            }
        }

        // =====================================================
        // 2. CHECK DEADLINE NOTIFICATIONS
        // =====================================================

        createDeadlineNotifications();

        System.out.println(
                "========== NOTIFICATION CHECK COMPLETE =========="
        );

        System.out.println();
    }

    // =====================================================
    // CREATE STUDY NOTIFICATION
    // =====================================================

    private void createStudyNotification(
            StudyTask task) {

        try {

            // =================================================
            // CHECK DUPLICATE STUDY NOTIFICATION
            // =================================================

            boolean alreadyExists =
                    notificationRepository
                            .existsByUserEmailAndStudyTaskIdAndType(
                                    task.getUserEmail(),
                                    task.getId(),
                                    "study"
                            );

            if (alreadyExists) {

                System.out.println(
                        "Study notification already exists for task ID: "
                                + task.getId()
                );

                return;
            }

            // =================================================
            // CREATE NOTIFICATION
            // =================================================

            Notification notification =
                    new Notification();

            notification.setUserEmail(
                    task.getUserEmail()
            );

            notification.setType(
                    "study"
            );

            notification.setTitle(
                    "Study Reminder"
            );

            notification.setMessage(
                    "Your study task \""
                            + task.getSubject()
                            + "\" starts in 10 minutes."
            );

            notification.setStudyTaskId(
                    task.getId()
            );

            // =================================================
            // COPY DEADLINE
            // =================================================

            notification.setDeadline(
                    task.getDeadline()
            );

            notification.setRead(false);

            // IMPORTANT:
            // Save notification creation time in India time.

            notification.setCreatedAt(
                    LocalDateTime.now(INDIA_ZONE)
            );

            notificationRepository.save(
                    notification
            );

            System.out.println(
                    "Study notification created successfully."
            );

        } catch (Exception e) {

            System.err.println(
                    "Failed to create study notification."
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // CREATE DEADLINE NOTIFICATIONS
    // =====================================================

    private void createDeadlineNotifications() {

        try {

            // =================================================
            // GET ALL TASKS
            // =================================================

            List<StudyTask> allTasks =
                    studyTaskRepository.findAll();

            // IMPORTANT:
            // Use India date instead of server date.

            LocalDate today =
                    LocalDate.now(INDIA_ZONE);

            for (StudyTask task : allTasks) {

                if (task == null) {
                    continue;
                }

                // =================================================
                // REQUIRED INFORMATION
                // =================================================

                if (task.getDeadline() == null
                        || task.getUserEmail() == null
                        || task.getUserEmail().trim().isEmpty()) {

                    continue;
                }

                // =================================================
                // GET USER
                // =================================================

                User user =
                        userRepository
                                .findByEmail(task.getUserEmail())
                                .orElse(null);

                if (user == null) {
                    continue;
                }

                // =================================================
                // TASK REMINDERS OFF
                // =================================================

                if (!Boolean.TRUE.equals(
                        user.isTaskReminders())) {

                    continue;
                }

                // =================================================
                // ONLY CREATE DEADLINE NOTIFICATION
                // ON THE DEADLINE DATE
                // =================================================

                if (!task.getDeadline().equals(today)) {
                    continue;
                }

                // =================================================
                // CHECK DUPLICATE
                // =================================================

                boolean alreadyExists =
                        notificationRepository
                                .existsByUserEmailAndStudyTaskIdAndType(
                                        task.getUserEmail(),
                                        task.getId(),
                                        "deadline"
                                );

                if (alreadyExists) {
                    continue;
                }

                // =================================================
                // CREATE DEADLINE NOTIFICATION
                // =================================================

                Notification notification =
                        new Notification();

                notification.setUserEmail(
                        task.getUserEmail()
                );

                notification.setType(
                        "deadline"
                );

                notification.setTitle(
                        "Deadline Reminder"
                );

                notification.setMessage(
                        "Your task \""
                                + task.getSubject()
                                + "\" is due on "
                                + formatDeadline(
                                        task.getDeadline()
                                )
                                + "."
                );

                notification.setStudyTaskId(
                        task.getId()
                );

                // =================================================
                // COPY DEADLINE
                // =================================================

                notification.setDeadline(
                        task.getDeadline()
                );

                notification.setRead(false);

                // IMPORTANT:
                // Save creation time in India time.

                notification.setCreatedAt(
                        LocalDateTime.now(INDIA_ZONE)
                );

                notificationRepository.save(
                        notification
                );

                // =================================================
                // SEND BROWSER PUSH NOTIFICATION
                // =================================================

                try {

                    webPushService.sendPushNotification(
                            task.getUserEmail(),
                            "Deadline Reminder",
                            "Your task \""
                                    + task.getSubject()
                                    + "\" is due on "
                                    + formatDeadline(
                                            task.getDeadline()
                                    )
                                    + "."
                    );

                    System.out.println(
                            "Deadline push notification sent."
                    );

                } catch (Exception pushException) {

                    System.err.println(
                            "Deadline push notification failed: "
                                    + pushException.getMessage()
                    );
                }

                // =================================================
                // LOG DEADLINE NOTIFICATION
                // =================================================

                System.out.println(
                        "========================================"
                );

                System.out.println(
                        "DEADLINE NOTIFICATION CREATED"
                );

                System.out.println(
                        "Task ID: "
                                + task.getId()
                );

                System.out.println(
                        "Subject: "
                                + task.getSubject()
                );

                System.out.println(
                        "Deadline: "
                                + task.getDeadline()
                );

                System.out.println(
                        "========================================"
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Failed to create deadline notifications."
            );

            e.printStackTrace();
        }
    }

    // =====================================================
    // FORMAT DEADLINE
    // =====================================================

    private String formatDeadline(
            LocalDate deadline) {

        if (deadline == null) {
            return "No deadline";
        }

        return deadline.getMonth()
                .toString()
                .substring(0, 1)
                + deadline.getMonth()
                        .toString()
                        .substring(1)
                        .toLowerCase()
                + " "
                + deadline.getDayOfMonth();
    }
}