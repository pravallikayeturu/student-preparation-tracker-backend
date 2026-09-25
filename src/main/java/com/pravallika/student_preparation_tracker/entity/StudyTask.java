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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        LocalDateTime now =
                LocalDateTime.now(INDIA_ZONE);

        LocalDate today =
                now.toLocalDate();

        LocalDateTime currentMinute =
                now.truncatedTo(ChronoUnit.MINUTES);

        System.out.println();
        System.out.println("========== NOTIFICATION CHECK ==========");
        System.out.println("Current India time: " + now);

        // =================================================
        // GET ALL TASKS
        // =================================================

        List<StudyTask> tasks =
                studyTaskRepository.findAll();

        System.out.println(
                "Total study tasks: " + tasks.size()
        );

        // =================================================
        // CHECK STUDY REMINDERS
        // =================================================

        for (StudyTask task : tasks) {

            if (task == null) {
                continue;
            }

            if (task.getUserEmail() == null
                    || task.getUserEmail().trim().isEmpty()) {

                continue;
            }

            if (task.getOriginalStartTime() == null) {
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

                System.out.println(
                        "User not found for task ID: "
                                + task.getId()
                );

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
            // CHECK WHETHER TASK OCCURS TODAY
            // =================================================

            if (!occursToday(task, today)) {

                continue;
            }

            // =================================================
            // CHECK RECURRENCE END DATE
            // =================================================

            if (task.getRecurrenceEndDate() != null
                    && today.isAfter(
                            task.getRecurrenceEndDate())) {

                continue;
            }

            // =================================================
            // TASK START TIME
            // =================================================

            LocalTime startTime =
                    task.getOriginalStartTime();

            LocalDateTime occurrenceDateTime =
                    LocalDateTime.of(
                            today,
                            startTime
                    );

            // =================================================
            // CALCULATE MINUTES UNTIL START
            // =================================================

            long minutesUntilStart =
                    ChronoUnit.MINUTES.between(
                            currentMinute,
                            occurrenceDateTime
                    );

            System.out.println(
                    "Task ID: "
                            + task.getId()
                            + " | Subject: "
                            + task.getSubject()
                            + " | Recurrence: "
                            + task.getRecurrenceType()
                            + " | Occurrence date: "
                            + today
                            + " | Start: "
                            + occurrenceDateTime
                            + " | Minutes until start: "
                            + minutesUntilStart
            );

            // =================================================
            // ONLY SEND DURING 10-MINUTE WINDOW
            // =================================================

            if (minutesUntilStart > 10
                    || minutesUntilStart < 0) {

                continue;
            }

            // =================================================
            // CHECK DUPLICATE FOR THIS OCCURRENCE
            // =================================================

            boolean alreadyExists =
                    notificationRepository
                            .existsByUserEmailAndStudyTaskIdAndTypeAndOccurrenceDate(
                                    task.getUserEmail(),
                                    task.getId(),
                                    "study",
                                    today
                            );

            if (alreadyExists) {

                System.out.println(
                        "Study reminder already exists for "
                                + "task ID "
                                + task.getId()
                                + " on "
                                + today
                );

                continue;
            }

            // =================================================
            // CREATE IN-APP NOTIFICATION
            // =================================================

            boolean notificationCreated =
                    createStudyNotification(
                            task,
                            today
                    );

            if (!notificationCreated) {

                System.out.println(
                        "Study notification was not created "
                                + "for task ID: "
                                + task.getId()
                );

                continue;
            }

            // =================================================
            // SEND BROWSER PUSH
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
            // =================================================

            if (Boolean.TRUE.equals(
                    user.isEmailNotifications())) {

                try {

                    brevoEmailService.sendStudyReminder(
                            task.getUserEmail(),
                            task.getSubject(),
                            task.getTopic(),
                            today,
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
                        "Email notifications are OFF for user: "
                                + task.getUserEmail()
                );
            }
        }

        // =====================================================
        // DEADLINE NOTIFICATIONS
        // =====================================================

        createDeadlineNotifications();

        System.out.println(
                "========== NOTIFICATION CHECK COMPLETE =========="
        );

        System.out.println();
    }

    // =====================================================
    // CHECK WHETHER TASK OCCURS TODAY
    // =====================================================

    private boolean occursToday(
            StudyTask task,
            LocalDate today) {

        String recurrence =
                task.getRecurrenceType();

        if (recurrence == null
                || recurrence.trim().isEmpty()) {

            recurrence = "ONE_TIME";
        }

        recurrence =
                recurrence.trim().toUpperCase();

        // =================================================
        // ONE TIME
        // =================================================

        if ("ONE_TIME".equals(recurrence)) {

            return task.getOriginalReadingDate() != null
                    && task.getOriginalReadingDate()
                    .equals(today);
        }

        // =================================================
        // DAILY
        // =================================================

        if ("DAILY".equals(recurrence)) {

            if (task.getOriginalReadingDate() == null) {
                return false;
            }

            return !today.isBefore(
                    task.getOriginalReadingDate()
            );
        }

        // =================================================
        // WEEKLY
        // =================================================

        if ("WEEKLY".equals(recurrence)) {

            if (task.getOriginalReadingDate() == null) {
                return false;
            }

            if (today.isBefore(
                    task.getOriginalReadingDate())) {

                return false;
            }

            String recurrenceDays =
                    task.getRecurrenceDays();

            if (recurrenceDays == null
                    || recurrenceDays.trim().isEmpty()) {

                return false;
            }

            DayOfWeek todayDay =
                    today.getDayOfWeek();

            String todayName =
                    todayDay.name();

            String[] selectedDays =
                    recurrenceDays
                            .toUpperCase()
                            .split(",");

            for (String day : selectedDays) {

                if (day.trim().equals(todayName)) {
                    return true;
                }
            }

            return false;
        }

        // =================================================
        // MONTHLY
        // =================================================

        if ("MONTHLY".equals(recurrence)) {

            if (task.getOriginalReadingDate() == null) {
                return false;
            }

            if (today.isBefore(
                    task.getOriginalReadingDate())) {

                return false;
            }

            Integer selectedDay =
                    task.getRecurrenceDayOfMonth();

            if (selectedDay == null) {
                return false;
            }

            int lastDayOfMonth =
                    today.lengthOfMonth();

            int actualDay =
                    Math.min(
                            selectedDay,
                            lastDayOfMonth
                    );

            return today.getDayOfMonth()
                    == actualDay;
        }

        return false;
    }

    // =====================================================
    // CREATE STUDY NOTIFICATION
    // =====================================================

    private boolean createStudyNotification(
            StudyTask task,
            LocalDate occurrenceDate) {

        try {

            // =================================================
            // DOUBLE CHECK DUPLICATE
            // =================================================

            boolean alreadyExists =
                    notificationRepository
                            .existsByUserEmailAndStudyTaskIdAndTypeAndOccurrenceDate(
                                    task.getUserEmail(),
                                    task.getId(),
                                    "study",
                                    occurrenceDate
                            );

            if (alreadyExists) {

                System.out.println(
                        "Study notification already exists."
                );

                return false;
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

            notification.setDeadline(
                    task.getDeadline()
            );

            notification.setOccurrenceDate(
                    occurrenceDate
            );

            notification.setRead(false);

            notification.setCreatedAt(
                    LocalDateTime.now(INDIA_ZONE)
            );

            notificationRepository.save(
                    notification
            );

            System.out.println(
                    "Study notification created successfully."
            );

            return true;

        } catch (Exception e) {

            System.err.println(
                    "Failed to create study notification."
            );

            e.printStackTrace();

            return false;
        }
    }

    // =====================================================
    // CREATE DEADLINE NOTIFICATIONS
    // =====================================================

    private void createDeadlineNotifications() {

        try {

            List<StudyTask> allTasks =
                    studyTaskRepository.findAll();

            LocalDate today =
                    LocalDate.now(INDIA_ZONE);

            for (StudyTask task : allTasks) {

                if (task == null) {
                    continue;
                }

                if (task.getDeadline() == null
                        || task.getUserEmail() == null
                        || task.getUserEmail()
                        .trim()
                        .isEmpty()) {

                    continue;
                }

                User user =
                        userRepository
                                .findByEmail(
                                        task.getUserEmail()
                                )
                                .orElse(null);

                if (user == null) {
                    continue;
                }

                if (!Boolean.TRUE.equals(
                        user.isTaskReminders())) {

                    continue;
                }

                if (!task.getDeadline().equals(today)) {
                    continue;
                }

                boolean alreadyExists =
                        notificationRepository
                                .existsByUserEmailAndStudyTaskIdAndTypeAndOccurrenceDate(
                                        task.getUserEmail(),
                                        task.getId(),
                                        "deadline",
                                        today
                                );

                if (alreadyExists) {
                    continue;
                }

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

                notification.setDeadline(
                        task.getDeadline()
                );

                notification.setOccurrenceDate(
                        today
                );

                notification.setRead(false);

                notification.setCreatedAt(
                        LocalDateTime.now(INDIA_ZONE)
                );

                notificationRepository.save(
                        notification
                );

                // =================================================
                // SEND DEADLINE PUSH
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

                System.out.println(
                        "DEADLINE NOTIFICATION CREATED | "
                                + "Task ID: "
                                + task.getId()
                                + " | Date: "
                                + today
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