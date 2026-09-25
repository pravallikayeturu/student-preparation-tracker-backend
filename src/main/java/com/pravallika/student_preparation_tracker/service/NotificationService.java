package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.Notification;
import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.entity.StudyTaskOccurrence;
import com.pravallika.student_preparation_tracker.entity.User;

import com.pravallika.student_preparation_tracker.repository.NotificationRepository;
import com.pravallika.student_preparation_tracker.repository.StudyTaskOccurrenceRepository;
import com.pravallika.student_preparation_tracker.repository.StudyTaskRepository;
import com.pravallika.student_preparation_tracker.repository.UserRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {

    private final StudyTaskRepository studyTaskRepository;
    private final StudyTaskOccurrenceRepository occurrenceRepository;
    private final NotificationRepository notificationRepository;
    private final BrevoEmailService brevoEmailService;
    private final UserRepository userRepository;
    private final WebPushService webPushService;

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public NotificationService(
            StudyTaskRepository studyTaskRepository,
            StudyTaskOccurrenceRepository occurrenceRepository,
            NotificationRepository notificationRepository,
            BrevoEmailService brevoEmailService,
            UserRepository userRepository,
            WebPushService webPushService) {

        this.studyTaskRepository = studyTaskRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.notificationRepository = notificationRepository;
        this.brevoEmailService = brevoEmailService;
        this.userRepository = userRepository;
        this.webPushService = webPushService;
    }


    // =====================================================
    // CHECK UPCOMING STUDY TASKS
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
        System.out.println(
                "========== NOTIFICATION CHECK =========="
        );

        System.out.println(
                "Current India time: " + now
        );


        // =================================================
        // GET ALL TASKS
        // =================================================

        List<StudyTask> tasks =
                studyTaskRepository.findAll();

        if (tasks == null || tasks.isEmpty()) {

            System.out.println(
                    "No study tasks found."
            );

            createDeadlineNotifications();

            return;
        }


        System.out.println(
                "Total study tasks: " + tasks.size()
        );


        // =================================================
        // CHECK EACH TASK
        // =================================================

        for (StudyTask task : tasks) {

            if (task == null) {
                continue;
            }


            // -------------------------------------------------
            // USER EMAIL
            // -------------------------------------------------

            String userEmail =
                    task.getUserEmail();

            if (userEmail == null
                    || userEmail.trim().isEmpty()) {

                continue;
            }


            // -------------------------------------------------
            // FIND USER
            // -------------------------------------------------

            User user =
                    userRepository
                            .findByEmail(userEmail)
                            .orElse(null);

            if (user == null) {

                System.out.println(
                        "User not found for task ID: "
                                + task.getId()
                );

                continue;
            }


            // -------------------------------------------------
            // TASK REMINDERS SETTING
            // -------------------------------------------------

            if (!Boolean.TRUE.equals(
                    user.isTaskReminders())) {

                continue;
            }


            // -------------------------------------------------
            // FIND TODAY'S OCCURRENCE
            // -------------------------------------------------

            StudyTaskOccurrence occurrence =
                    occurrenceRepository
                            .findByStudyTaskIdAndOccurrenceDate(
                                    task.getId(),
                                    today
                            )
                            .orElse(null);

            if (occurrence == null) {
                continue;
            }


            // -------------------------------------------------
            // COMPLETED OCCURRENCE
            // -------------------------------------------------

            if ("COMPLETED".equalsIgnoreCase(
                    occurrence.getStatus())) {

                continue;
            }


            // -------------------------------------------------
            // REMINDER ALREADY SENT
            // -------------------------------------------------

            if (occurrence.isReminderSent()) {

                continue;
            }


            // -------------------------------------------------
            // START TIME
            // -------------------------------------------------

            LocalTime startTime =
                    occurrence.getStartTime();

            if (startTime == null) {
                continue;
            }


            // -------------------------------------------------
            // OCCURRENCE START
            // -------------------------------------------------

            LocalDateTime occurrenceStart =
                    LocalDateTime.of(
                            occurrence.getOccurrenceDate(),
                            startTime
                    );


            // -------------------------------------------------
            // MINUTES UNTIL START
            // -------------------------------------------------

            long minutesUntilStart =
                    ChronoUnit.MINUTES.between(
                            currentMinute,
                            occurrenceStart
                    );


            System.out.println(
                    "Task ID: "
                            + task.getId()
                            + " | Subject: "
                            + task.getSubject()
                            + " | Date: "
                            + occurrence.getOccurrenceDate()
                            + " | Start: "
                            + occurrence.getStartTime()
                            + " | Minutes Until Start: "
                            + minutesUntilStart
            );


            // =================================================
            // REMINDER WINDOW
            // =================================================
            //
            // Scheduler runs every minute.
            //
            // Example:
            // 5:49 -> 11 minutes -> skip
            // 5:50 -> 10 minutes -> send
            // 5:51 -> 9 minutes  -> send only if not already sent
            //
            // Once sent, occurrence.reminderSent becomes true.
            //

            if (minutesUntilStart > 10
                    || minutesUntilStart < 0) {

                continue;
            }


            // =================================================
            // CREATE IN-APP NOTIFICATION
            // =================================================

            Notification notification =
                    new Notification();

            notification.setUserEmail(
                    userEmail
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

            notification.setRead(false);

            notification.setCreatedAt(
                    LocalDateTime.now(INDIA_ZONE)
            );


            try {

                notificationRepository.save(
                        notification
                );

                System.out.println(
                        "Study notification created successfully."
                );

            } catch (Exception notificationException) {

                System.err.println(
                        "Failed to create study notification: "
                                + notificationException.getMessage()
                );

                /*
                 * Do not mark the occurrence as reminded
                 * if the in-app notification itself failed.
                 */
                continue;
            }


            // =================================================
            // BROWSER PUSH
            // =================================================

            try {

                webPushService.sendPushNotification(
                        userEmail,
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
                        "Push notification failed: "
                                + pushException.getMessage()
                );
            }


            // =================================================
            // EMAIL
            // =================================================

            if (Boolean.TRUE.equals(
                    user.isEmailNotifications())) {

                try {

                    brevoEmailService.sendStudyReminder(
                            userEmail,
                            task.getSubject(),
                            task.getTopic(),
                            occurrence.getOccurrenceDate(),
                            occurrence.getStartTime(),
                            occurrence.getEndTime()
                    );

                    System.out.println(
                            "Study reminder email sent successfully."
                    );

                } catch (Exception emailException) {

                    System.err.println(
                            "Study reminder email failed: "
                                    + emailException.getMessage()
                    );
                }

            } else {

                System.out.println(
                        "Email notifications are OFF for user: "
                                + userEmail
                );
            }


            // =================================================
            // MARK ONLY THIS OCCURRENCE AS REMINDED
            // =================================================

            occurrence.setReminderSent(true);

            occurrenceRepository.save(
                    occurrence
            );

            System.out.println(
                    "Occurrence reminder marked as sent."
            );
        }


        // =================================================
        // DEADLINE NOTIFICATIONS
        // =================================================

        createDeadlineNotifications();


        System.out.println(
                "========== NOTIFICATION CHECK COMPLETE =========="
        );

        System.out.println();
    }


    // =====================================================
    // DEADLINE NOTIFICATIONS
    // =====================================================

    private void createDeadlineNotifications() {

        try {

            List<StudyTask> allTasks =
                    studyTaskRepository.findAll();

            if (allTasks == null
                    || allTasks.isEmpty()) {

                return;
            }


            LocalDate today =
                    LocalDate.now(INDIA_ZONE);


            for (StudyTask task : allTasks) {

                if (task == null) {
                    continue;
                }


                // -------------------------------------------------
                // DEADLINE VALIDATION
                // -------------------------------------------------

                if (task.getDeadline() == null) {
                    continue;
                }


                String userEmail =
                        task.getUserEmail();

                if (userEmail == null
                        || userEmail.trim().isEmpty()) {

                    continue;
                }


                // -------------------------------------------------
                // FIND USER
                // -------------------------------------------------

                User user =
                        userRepository
                                .findByEmail(userEmail)
                                .orElse(null);

                if (user == null) {
                    continue;
                }


                // -------------------------------------------------
                // TASK REMINDERS OFF
                // -------------------------------------------------

                if (!Boolean.TRUE.equals(
                        user.isTaskReminders())) {

                    continue;
                }


                // -------------------------------------------------
                // DEADLINE IS NOT TODAY
                // -------------------------------------------------

                if (!task.getDeadline().equals(today)) {
                    continue;
                }


                // =================================================
                // CHECK DUPLICATE
                // =================================================

                boolean alreadyExists =
                        notificationRepository
                                .existsByUserEmailAndStudyTaskIdAndType(
                                        userEmail,
                                        task.getId(),
                                        "deadline"
                                );

                if (alreadyExists) {
                    continue;
                }


                // =================================================
                // CREATE IN-APP DEADLINE NOTIFICATION
                // =================================================

                Notification notification =
                        new Notification();

                notification.setUserEmail(
                        userEmail
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
                                + "\" is due today."
                );

                notification.setStudyTaskId(
                        task.getId()
                );

                notification.setDeadline(
                        task.getDeadline()
                );

                notification.setRead(false);

                notification.setCreatedAt(
                        LocalDateTime.now(INDIA_ZONE)
                );


                try {

                    notificationRepository.save(
                            notification
                    );

                } catch (Exception notificationException) {

                    System.err.println(
                            "Failed to create deadline notification: "
                                    + notificationException.getMessage()
                    );

                    continue;
                }


                // =================================================
                // DEADLINE PUSH
                // =================================================

                try {

                    webPushService.sendPushNotification(
                            userEmail,
                            "Deadline Reminder",
                            "Your task \""
                                    + task.getSubject()
                                    + "\" is due today."
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
                // DEADLINE EMAIL
                // =================================================
                //
                // We use the existing Brevo study-reminder
                // method so this service does not introduce
                // a method that may not exist in your
                // BrevoEmailService.
                //

                if (Boolean.TRUE.equals(
                        user.isEmailNotifications())) {

                    try {

                        brevoEmailService.sendStudyReminder(
                                userEmail,
                                task.getSubject(),
                                task.getTopic(),
                                task.getReadingDate(),
                                task.getStartTime(),
                                task.getEndTime()
                        );

                        System.out.println(
                                "Deadline email sent successfully."
                        );

                    } catch (Exception emailException) {

                        System.err.println(
                                "Deadline email failed: "
                                        + emailException.getMessage()
                        );
                    }

                } else {

                    System.out.println(
                            "Email notifications are OFF for user: "
                                    + userEmail
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