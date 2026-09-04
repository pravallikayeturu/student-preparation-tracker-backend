
package com.pravallika.student_preparation_tracker.repository;

import com.pravallika.student_preparation_tracker.entity.StudyTask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyTaskRepository
        extends JpaRepository<StudyTask, Long> {

    // =========================================
    // GET ALL TASKS OF A USER
    // =========================================

    List<StudyTask> findByUserEmail(
            String userEmail
    );


    // =========================================
    // FIND TASK BY ID AND USER
    // =========================================

    Optional<StudyTask> findByIdAndUserEmail(
            Long id,
            String userEmail
    );


    // =========================================
    // CHECK OVERLAPPING TASKS
    // =========================================

    List<StudyTask>
    findByUserEmailAndReadingDateAndStartTimeLessThanAndEndTimeGreaterThan(
            String userEmail,
            LocalDate readingDate,
            LocalTime endTime,
            LocalTime startTime
    );


    // =========================================
    // FIND TASKS BY DATE
    // =========================================

    List<StudyTask> findByReadingDate(
            LocalDate readingDate
    );


    // =========================================
    // FIND TODAY'S ORIGINAL TASKS
    // WHOSE REMINDER HAS NOT BEEN SENT
    // =========================================

    List<StudyTask>
    findByOriginalReadingDateAndReminderSentFalse(
            LocalDate originalReadingDate
    );


    // =========================================
    // FIND USER'S TASKS BY DATE
    // =========================================

    List<StudyTask> findByUserEmailAndReadingDate(
            String userEmail,
            LocalDate readingDate
    );


    // =========================================
    // FIND TASKS BY RECURRENCE TYPE
    // =========================================

    List<StudyTask> findByUserEmailAndRecurrenceType(
            String userEmail,
            String recurrenceType
    );
}