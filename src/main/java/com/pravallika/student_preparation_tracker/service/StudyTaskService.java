package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.repository.StudyTaskRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyTaskService {

    private final StudyTaskRepository studyTaskRepository;

    public StudyTaskService(StudyTaskRepository studyTaskRepository) {
        this.studyTaskRepository = studyTaskRepository;
    }

    // =========================
    // CREATE STUDY TASK
    // =========================
    public StudyTask createTask(
            StudyTask task,
            String userEmail) {

        // Validate start and end time
        if (task.getStartTime() == null
                || task.getEndTime() == null) {

            throw new RuntimeException(
                    "Please enter both start time and end time."
            );
        }

        // End time must be after start time
        if (!task.getEndTime()
                .isAfter(task.getStartTime())) {

            throw new RuntimeException(
                    "Please enter a valid timing. "
                    + "End time must be after start time."
            );
        }

        // User information
        task.setUserEmail(userEmail);

        // New task starts as PENDING
        task.setStatus("PENDING");

        // =========================================
        // STORE ORIGINAL SCHEDULE
        // =========================================

        task.setOriginalReadingDate(
                task.getReadingDate()
        );

        task.setOriginalStartTime(
                task.getStartTime()
        );

        task.setOriginalEndTime(
                task.getEndTime()
        );

        return studyTaskRepository.save(task);
    }

    // =========================
    // GET ALL STUDY TASKS
    // =========================
    public List<StudyTask> getTasks(
            String userEmail) {

        return studyTaskRepository
                .findByUserEmail(userEmail);
    }

    // =========================
    // UPDATE STUDY TASK
    // =========================
    public StudyTask updateTask(
            Long id,
            StudyTask updatedTask,
            String userEmail) {

        // =========================================
        // FIND USER'S TASK
        // =========================================

        StudyTask existingTask =
                studyTaskRepository
                        .findByIdAndUserEmail(id, userEmail)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found."
                                )
                        );

        // =========================================
        // VALIDATE NEW TIMING
        // =========================================

        if (updatedTask.getStartTime() == null
                || updatedTask.getEndTime() == null) {

            throw new RuntimeException(
                    "Please enter both start time and end time."
            );
        }

        if (!updatedTask.getEndTime()
                .isAfter(updatedTask.getStartTime())) {

            throw new RuntimeException(
                    "Please enter a valid timing. "
                    + "End time must be after start time."
            );
        }

        // =========================================
        // BACKWARD COMPATIBILITY
        // For old tasks created before we added
        // original schedule fields.
        // =========================================

        if (existingTask.getOriginalReadingDate() == null) {

            existingTask.setOriginalReadingDate(
                    existingTask.getReadingDate()
            );
        }

        if (existingTask.getOriginalStartTime() == null) {

            existingTask.setOriginalStartTime(
                    existingTask.getStartTime()
            );
        }

        if (existingTask.getOriginalEndTime() == null) {

            existingTask.setOriginalEndTime(
                    existingTask.getEndTime()
            );
        }

        // =========================================
        // ORIGINAL SCHEDULE
        // =========================================

        LocalDateTime originalStartDateTime =
                LocalDateTime.of(
                        existingTask.getOriginalReadingDate(),
                        existingTask.getOriginalStartTime()
                );

        LocalDateTime originalEndDateTime =
                LocalDateTime.of(
                        existingTask.getOriginalReadingDate(),
                        existingTask.getOriginalEndTime()
                );

        LocalDateTime now = LocalDateTime.now();

        // =========================================
        // ONE-HOUR EDIT LOCK
        // =========================================

        LocalDateTime editLockTime =
                originalStartDateTime.minusHours(1);

        /*
         * Timing is locked:
         *
         * From 1 hour before original start
         * until original session ends.
         *
         * Example:
         *
         * Original: 1:00 PM - 2:00 PM
         *
         * Before 12:00 PM → EDIT ALLOWED
         * 12:00 PM       → LOCKED
         * 1:00 PM        → LOCKED
         * 2:00 PM        → EDIT ALLOWED
         */

        if (!now.isBefore(editLockTime)
                && now.isBefore(originalEndDateTime)) {

            throw new RuntimeException(
                    "Study timing is locked. "
                    + "You can edit the timing only until "
                    + "1 hour before the original start time."
            );
        }

        // =========================================
        // UPDATE NORMAL TASK DETAILS
        // =========================================

        existingTask.setSubject(
                updatedTask.getSubject()
        );

        existingTask.setTopic(
                updatedTask.getTopic()
        );

        existingTask.setDescription(
                updatedTask.getDescription()
        );

        existingTask.setPriority(
                updatedTask.getPriority()
        );

        existingTask.setReadingDate(
                updatedTask.getReadingDate()
        );

        existingTask.setDeadline(
                updatedTask.getDeadline()
        );

        // =========================================
        // UPDATE TIMING
        // =========================================

        existingTask.setStartTime(
                updatedTask.getStartTime()
        );

        existingTask.setEndTime(
                updatedTask.getEndTime()
        );

        /*
         * IMPORTANT:
         *
         * We DO NOT change:
         *
         * originalReadingDate
         * originalStartTime
         * originalEndTime
         *
         * These must remain unchanged.
         */

        return studyTaskRepository.save(existingTask);
    }

    // =========================
// DELETE STUDY TASK
// =========================
public void deleteTask(Long id, String userEmail) {

    // Find task belonging to logged-in user
    StudyTask existingTask =
            studyTaskRepository
                    .findByIdAndUserEmail(id, userEmail)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Task not found."
                            )
                    );

    // Delete the task
    studyTaskRepository.delete(existingTask);
}
}