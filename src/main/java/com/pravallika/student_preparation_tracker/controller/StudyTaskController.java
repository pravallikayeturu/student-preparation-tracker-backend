
package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.entity.StudyTaskOccurrence;
import com.pravallika.student_preparation_tracker.service.StudyTaskService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class StudyTaskController {

    private final StudyTaskService studyTaskService;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public StudyTaskController(
            StudyTaskService studyTaskService) {

        this.studyTaskService = studyTaskService;
    }


    // =====================================================
    // CREATE STUDY TASK
    // =====================================================

    /*
     * POST
     * /api/tasks
     *
     * Creates the parent StudyTask and
     * generates its occurrences.
     *
     * Supported recurrence types:
     *
     * ONE_TIME
     * DAILY
     * WEEKLY
     * MONTHLY
     */

    @PostMapping
    public ResponseEntity<StudyTask> createTask(
            @RequestBody StudyTask task,
            Authentication authentication) {

        String userEmail = authentication.getName();

        StudyTask savedTask =
                studyTaskService.createTask(
                        task,
                        userEmail
                );

        return ResponseEntity.ok(savedTask);
    }


    // =====================================================
    // GET ALL STUDY TASKS
    // =====================================================

    /*
     * GET
     * /api/tasks
     *
     * Returns all tasks belonging to
     * the logged-in user.
     */

    @GetMapping
    public ResponseEntity<List<StudyTask>> getTasks(
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<StudyTask> tasks =
                studyTaskService.getTasks(
                        userEmail
                );

        return ResponseEntity.ok(tasks);
    }


    // =====================================================
    // UPDATE ENTIRE STUDY TASK / SERIES
    // =====================================================

    /*
     * PUT
     * /api/tasks/{id}
     *
     * Updates the complete parent task.
     *
     * For recurring tasks:
     *
     * - Parent task is updated.
     * - Existing occurrences are synchronized.
     * - Missing occurrences are created.
     * - Completed occurrences are preserved.
     * - Existing occurrence IDs are preserved.
     *
     * Supported recurrence types:
     *
     * ONE_TIME
     * DAILY
     * WEEKLY
     * MONTHLY
     */

    @PutMapping("/{id}")
    public ResponseEntity<StudyTask> updateTask(
            @PathVariable Long id,
            @RequestBody StudyTask task,
            Authentication authentication) {

        String userEmail = authentication.getName();

        StudyTask updatedTask =
                studyTaskService.updateTask(
                        id,
                        task,
                        userEmail
                );

        return ResponseEntity.ok(updatedTask);
    }


    // =====================================================
    // GET ALL OCCURRENCES OF A TASK
    // =====================================================

    /*
     * GET
     * /api/tasks/{taskId}/occurrences
     *
     * Returns all occurrences belonging to
     * the selected task.
     */

    @GetMapping("/{taskId}/occurrences")
    public ResponseEntity<List<StudyTaskOccurrence>>
    getOccurrences(
            @PathVariable Long taskId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<StudyTaskOccurrence> occurrences =
                studyTaskService.getOccurrences(
                        taskId,
                        userEmail
                );

        return ResponseEntity.ok(occurrences);
    }


    // =====================================================
    // GET FUTURE OCCURRENCES
    // =====================================================

    /*
     * GET
     * /api/tasks/{taskId}/occurrences/future
     *
     * Returns today's and future occurrences.
     *
     * Past occurrences are excluded.
     */

    @GetMapping("/{taskId}/occurrences/future")
    public ResponseEntity<List<StudyTaskOccurrence>>
    getFutureOccurrences(
            @PathVariable Long taskId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<StudyTaskOccurrence> occurrences =
                studyTaskService.getFutureOccurrences(
                        taskId,
                        userEmail
                );

        return ResponseEntity.ok(occurrences);
    }


    // =====================================================
    // UPDATE SINGLE OCCURRENCE
    // =====================================================

    /*
     * PUT
     * /api/tasks/occurrences/{occurrenceId}
     *
     * Changes ONLY one occurrence.
     *
     * Example:
     *
     * Parent:
     *
     * 30 Aug -> 08:00 - 10:00
     * 31 Aug -> 08:00 - 10:00
     * 01 Sep -> 08:00 - 10:00
     *
     * User changes 31 Aug to:
     *
     * 09:00 - 11:00
     *
     * Result:
     *
     * 30 Aug -> 08:00 - 10:00
     * 31 Aug -> 09:00 - 11:00
     * 01 Sep -> 08:00 - 10:00
     *
     * Only that occurrence changes.
     */

    @PutMapping("/occurrences/{occurrenceId}")
    public ResponseEntity<StudyTaskOccurrence>
    updateOccurrence(
            @PathVariable Long occurrenceId,
            @RequestBody OccurrenceTimeRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        if (request == null) {

            throw new RuntimeException(
                    "Occurrence timing data cannot be empty."
            );
        }

        if (request.getStartTime() == null) {

            throw new RuntimeException(
                    "Start time is required."
            );
        }

        if (request.getEndTime() == null) {

            throw new RuntimeException(
                    "End time is required."
            );
        }

        StudyTaskOccurrence updatedOccurrence =
                studyTaskService.updateOccurrence(
                        occurrenceId,
                        request.getStartTime(),
                        request.getEndTime(),
                        userEmail
                );

        return ResponseEntity.ok(
                updatedOccurrence
        );
    }


    // =====================================================
    // MARK SINGLE OCCURRENCE AS COMPLETED
    // =====================================================

    /*
     * PUT
     * /api/tasks/occurrences/{occurrenceId}/complete
     *
     * Marks ONLY the selected occurrence as COMPLETED.
     *
     * The service will verify:
     *
     * 1. The occurrence belongs to the logged-in user.
     * 2. The occurrence date is TODAY.
     * 3. The occurrence is not already completed.
     *
     * Example:
     *
     * Today:
     * 29 Aug -> COMPLETED
     *
     * Future:
     * 30 Aug -> PENDING
     * 31 Aug -> PENDING
     *
     * Completing today's occurrence will NOT
     * complete tomorrow's occurrence.
     */

    @PutMapping("/occurrences/{occurrenceId}/complete")
    public ResponseEntity<StudyTaskOccurrence>
    completeOccurrence(
            @PathVariable Long occurrenceId,
            Authentication authentication) {

        String userEmail = authentication.getName();

        StudyTaskOccurrence completedOccurrence =
                studyTaskService.completeOccurrence(
                        occurrenceId,
                        userEmail
                );

        return ResponseEntity.ok(
                completedOccurrence
        );
    }


    // =====================================================
    // MARK TASK AS COMPLETED
    // =====================================================

    /*
     * PUT
     * /api/tasks/{id}/complete
     *
     * Existing parent-task completion endpoint.
     *
     * This is kept for compatibility with the
     * existing application.
     *
     * The new MyTasks.jsx occurrence-based
     * Complete button should use:
     *
     * /api/tasks/occurrences/{occurrenceId}/complete
     */

    @PutMapping("/{id}/complete")
    public ResponseEntity<StudyTask> completeTask(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();

        StudyTask completedTask =
                studyTaskService.completeTask(
                        id,
                        userEmail
                );

        return ResponseEntity.ok(
                completedTask
        );
    }


    // =====================================================
    // DELETE STUDY TASK
    // =====================================================

    /*
     * DELETE
     * /api/tasks/{id}
     *
     * Deletes:
     *
     * 1. All occurrences
     * 2. Parent StudyTask
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long id,
            Authentication authentication) {

        String userEmail = authentication.getName();

        studyTaskService.deleteTask(
                id,
                userEmail
        );

        return ResponseEntity.ok(
                "Study task deleted successfully."
        );
    }


    // =====================================================
    // OCCURRENCE TIME REQUEST DTO
    // =====================================================

    /*
     * Request body for changing one occurrence.
     *
     * Example:
     *
     * {
     *     "startTime": "09:00",
     *     "endTime": "11:00"
     * }
     */

    public static class OccurrenceTimeRequest {

        private LocalTime startTime;

        private LocalTime endTime;


        // =================================================
        // CONSTRUCTOR
        // =================================================

        public OccurrenceTimeRequest() {
        }


        // =================================================
        // GET START TIME
        // =================================================

        public LocalTime getStartTime() {
            return startTime;
        }


        // =================================================
        // SET START TIME
        // =================================================

        public void setStartTime(
                LocalTime startTime) {

            this.startTime = startTime;
        }


        // =================================================
        // GET END TIME
        // =================================================

        public LocalTime getEndTime() {
            return endTime;
        }


        // =================================================
        // SET END TIME
        // =================================================

        public void setEndTime(
                LocalTime endTime) {

            this.endTime = endTime;
        }
    }
}
