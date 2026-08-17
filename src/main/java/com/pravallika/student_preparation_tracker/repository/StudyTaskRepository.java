package com.pravallika.student_preparation_tracker.repository;

import com.pravallika.student_preparation_tracker.entity.StudyTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudyTaskRepository
        extends JpaRepository<StudyTask, Long> {

    // Get all tasks of the logged-in user
    List<StudyTask> findByUserEmail(String userEmail);

    // Find a specific task belonging to the logged-in user
    Optional<StudyTask> findByIdAndUserEmail(
            Long id,
            String userEmail
    );

    // Get only today's tasks whose reminder is not yet sent
    List<StudyTask> findByReadingDateAndReminderSentFalse(
            LocalDate readingDate
    );
}
