package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.repository.StudyTaskRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class NotificationService {

    private final StudyTaskRepository studyTaskRepository;
    private final ResendEmailService resendEmailService;

    public NotificationService(
            StudyTaskRepository studyTaskRepository,
            ResendEmailService resendEmailService) {

        this.studyTaskRepository = studyTaskRepository;
        this.resendEmailService = resendEmailService;
    }

    // =========================================
    // CHECK UPCOMING TASKS
    // Runs every minute
    // =========================================

    @Scheduled(fixedRate = 60000)
    public void checkUpcomingTasks() {

        LocalDateTime now = LocalDateTime.now();

        System.out.println(
                "========== REMINDER CHECK =========="
        );

        System.out.println(
                "Current time: " + now
        );

        // Get only today's tasks whose reminder
        // has not already been sent
        List<StudyTask> tasks =
                studyTaskRepository
                        .findByReadingDateAndReminderSentFalse(
                                LocalDate.now()
                        );

        System.out.println(
                "Pending reminder tasks found: " + tasks.size()
        );

        for (StudyTask task : tasks) {

            // Ignore incomplete tasks
            if (task.getStartTime() == null
                    || task.getUserEmail() == null) {

                System.out.println(
                        "Skipping task ID " + task.getId()
                                + " because start time or email is missing."
                );

                continue;
            }

            LocalDateTime startDateTime =
                    LocalDateTime.of(
                            task.getReadingDate(),
                            task.getStartTime()
                    );

            // Current time rounded to minute
            LocalDateTime currentMinute =
                    now.truncatedTo(ChronoUnit.MINUTES);

            // Calculate minutes until study session
            long minutesUntilStart =
                    ChronoUnit.MINUTES.between(
                            currentMinute,
                            startDateTime
                    );

            // =========================================
            // DEBUG LOG
            // =========================================

            System.out.println(
                    "Task ID: " + task.getId()
                            + " | Email: " + task.getUserEmail()
                            + " | Subject: " + task.getSubject()
                            + " | Start: " + startDateTime
                            + " | Current: " + currentMinute
                            + " | Minutes until start: "
                            + minutesUntilStart
                            + " | Reminder sent: "
                            + task.isReminderSent()
            );

            // =========================================
            // SEND REMINDER
            // 10 MINUTES BEFORE
            //
            // Using <= 10 and > 0 makes the scheduler
            // more reliable if it misses exactly one minute.
            // =========================================

            if (minutesUntilStart <= 10
                    && minutesUntilStart > 0) {

                System.out.println(
                        "Reminder condition matched for task ID: "
                                + task.getId()
                );

                sendReminderEmail(task);
            }
        }

        System.out.println(
                "========== REMINDER CHECK COMPLETE =========="
        );
    }

    // =========================================
    // SEND REMINDER EMAIL
    // =========================================

    private void sendReminderEmail(StudyTask task) {

        try {

            System.out.println(
                    "Attempting to send reminder email to: "
                            + task.getUserEmail()
            );

            resendEmailService.sendStudyReminder(
                    task.getUserEmail(),
                    task.getSubject(),
                    task.getTopic(),
                    task.getReadingDate(),
                    task.getStartTime(),
                    task.getEndTime()
            );

            // =========================================
            // IMPORTANT:
            // Mark reminder as sent only AFTER
            // email sending succeeds.
            // =========================================

            task.setReminderSent(true);

            studyTaskRepository.save(task);

            System.out.println(
                    "Study reminder sent successfully to: "
                            + task.getUserEmail()
            );

            System.out.println(
                    "Reminder marked as sent for task ID: "
                            + task.getId()
            );

        } catch (Exception e) {

            // If email sending fails,
            // reminderSent remains false.

            System.err.println(
                    "Failed to send study reminder to: "
                            + task.getUserEmail()
            );

            System.err.println(
                    "Reason: " + e.getMessage()
            );

            // Print complete error details
            e.printStackTrace();
        }
    }
}