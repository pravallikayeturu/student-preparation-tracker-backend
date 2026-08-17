package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.service.StudyTaskService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class StudyTaskController {

    private final StudyTaskService studyTaskService;

    public StudyTaskController(
            StudyTaskService studyTaskService) {

        this.studyTaskService = studyTaskService;
    }

    // =========================
    // CREATE STUDY TASK
    // =========================

    @PostMapping
    public ResponseEntity<StudyTask> createTask(
            @RequestBody StudyTask task,
            Authentication authentication) {

        String userEmail =
                authentication.getName();

        StudyTask savedTask =
                studyTaskService.createTask(
                        task,
                        userEmail
                );

        return ResponseEntity.ok(savedTask);
    }

    // =========================
    // GET ALL STUDY TASKS
    // =========================

    @GetMapping
    public ResponseEntity<List<StudyTask>> getTasks(
            Authentication authentication) {

        String userEmail =
                authentication.getName();

        List<StudyTask> tasks =
                studyTaskService.getTasks(
                        userEmail
                );

        return ResponseEntity.ok(tasks);
    }

    // =========================
    // UPDATE STUDY TASK
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<StudyTask> updateTask(
            @PathVariable Long id,
            @RequestBody StudyTask task,
            Authentication authentication) {

        String userEmail =
                authentication.getName();

        StudyTask updatedTask =
                studyTaskService.updateTask(
                        id,
                        task,
                        userEmail
                );

        return ResponseEntity.ok(updatedTask);
    }
    // =========================
// DELETE STUDY TASK
// =========================
@DeleteMapping("/{id}")
public ResponseEntity<String> deleteTask(
        @PathVariable Long id,
        Authentication authentication) {

    String userEmail = authentication.getName();

    studyTaskService.deleteTask(id, userEmail);

    return ResponseEntity.ok(
            "Study task deleted successfully."
    );
}
}