package com.pravallika.student_preparation_tracker.repository;

import com.pravallika.student_preparation_tracker.entity.StudyTaskOccurrence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface StudyTaskOccurrenceRepository
        extends JpaRepository<StudyTaskOccurrence, Long> {

    // =====================================================
    // GET ALL OCCURRENCES FOR A TASK
    // =====================================================

    /*
     * Returns all occurrences of a particular
     * StudyTask ordered by date.
     *
     * Example:
     *
     * 22 Aug
     * 23 Aug
     * 24 Aug
     * ...
     * 04 Sep
     */

    List<StudyTaskOccurrence>
    findByStudyTaskIdOrderByOccurrenceDateAsc(
            Long studyTaskId
    );


    // =====================================================
    // GET OCCURRENCE BY ID + USER
    // =====================================================

    /*
     * Used when editing an occurrence.
     *
     * This prevents one user from editing
     * another user's occurrence.
     */

    Optional<StudyTaskOccurrence>
    findByIdAndStudyTaskUserEmail(
            Long id,
            String userEmail
    );


    // =====================================================
    // GET OCCURRENCE BY TASK + DATE
    // =====================================================

    /*
     * Finds the occurrence belonging to a task
     * on a particular date.
     *
     * Example:
     *
     * Task ID = 10
     * Date    = 2026-08-24
     */

    Optional<StudyTaskOccurrence>
    findByStudyTaskIdAndOccurrenceDate(
            Long studyTaskId,
            LocalDate occurrenceDate
    );


    // =====================================================
    // FIND OVERLAPPING OCCURRENCES
    // =====================================================

    /*
     * Used when changing the timing of one occurrence.
     *
     * Example:
     *
     * Existing:
     * 08:00 - 10:00
     *
     * User tries:
     * 09:00 - 11:00
     *
     * This detects the conflict.
     */

    List<StudyTaskOccurrence>
    findByStudyTaskUserEmailAndOccurrenceDateAndStartTimeLessThanAndEndTimeGreaterThan(
            String userEmail,
            LocalDate occurrenceDate,
            LocalTime endTime,
            LocalTime startTime
    );


    // =====================================================
    // DELETE ALL OCCURRENCES FOR A TASK
    // =====================================================

    /*
     * Used when deleting a StudyTask.
     *
     * Parent task must be deleted only after
     * its occurrences are deleted.
     */

    void deleteByStudyTaskId(
            Long studyTaskId
    );
}