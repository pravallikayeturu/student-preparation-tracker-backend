package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.entity.ChatMessage;
import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.repository.StudyTaskRepository;
import com.pravallika.student_preparation_tracker.service.ChatbotService;
import com.pravallika.student_preparation_tracker.service.ChatMessageService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final StudyTaskRepository studyTaskRepository;
    private final ChatMessageService chatMessageService;

    public ChatbotController(
            ChatbotService chatbotService,
            StudyTaskRepository studyTaskRepository,
            ChatMessageService chatMessageService) {

        this.chatbotService = chatbotService;
        this.studyTaskRepository = studyTaskRepository;
        this.chatMessageService = chatMessageService;
    }

    // =====================================================
    // ASK AI
    // =====================================================

    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askQuestion(
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String question = request.get("question");

        // Check whether question is empty
        if (question == null
                || question.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Please enter a question."
                    ));
        }

        // =================================================
        // GET LOGGED-IN USER
        // =================================================

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login to use the chatbot."
                    ));
        }

        String userEmail =
                authentication.getName();

        // =================================================
        // GET AI ANSWER
        // =================================================

        String answer =
                chatbotService.getAnswer(
                        question,
                        userEmail
                );

        // =================================================
        // SAVE CHAT HISTORY
        // =================================================

        chatMessageService.saveChat(
                userEmail,
                question,
                answer
        );

        // =================================================
        // RETURN RESPONSE
        // =================================================

        return ResponseEntity.ok(
                Map.of(
                        "question",
                        question,
                        "answer",
                        answer
                )
        );
    }

    // =====================================================
    // CHAT HISTORY
    // =====================================================

    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login first."
                    ));
        }

        String userEmail =
                authentication.getName();

        List<ChatMessage> history =
                chatMessageService
                        .getChatHistory(userEmail);

        return ResponseEntity.ok(history);
    }

    // =====================================================
    // TODAY TASKS
    // =====================================================

    @GetMapping("/tasks/today")
    public ResponseEntity<?> getTodayTasks(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login first."
                    ));
        }

        String userEmail =
                authentication.getName();

        LocalDate today =
                LocalDate.now();

        List<StudyTask> tasks =
                studyTaskRepository
                        .findByUserEmailAndReadingDate(
                                userEmail,
                                today
                        );

        return ResponseEntity.ok(tasks);
    }

    // =====================================================
    // TOMORROW TASKS
    // =====================================================

    @GetMapping("/tasks/tomorrow")
    public ResponseEntity<?> getTomorrowTasks(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login first."
                    ));
        }

        String userEmail =
                authentication.getName();

        LocalDate tomorrow =
                LocalDate.now().plusDays(1);

        List<StudyTask> tasks =
                studyTaskRepository
                        .findByUserEmailAndReadingDate(
                                userEmail,
                                tomorrow
                        );

        return ResponseEntity.ok(tasks);
    }

    // =====================================================
    // PENDING TASKS
    // =====================================================

    @GetMapping("/tasks/pending")
    public ResponseEntity<?> getPendingTasks(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login first."
                    ));
        }

        String userEmail =
                authentication.getName();

        List<StudyTask> allTasks =
                studyTaskRepository
                        .findByUserEmail(userEmail);

        List<StudyTask> pendingTasks =
                allTasks.stream()
                        .filter(task ->
                                task.getStatus() != null
                                        && task.getStatus()
                                        .equalsIgnoreCase("PENDING")
                        )
                        .toList();

        return ResponseEntity.ok(pendingTasks);
    }

    // =====================================================
    // COMPLETED TASKS
    // =====================================================

    @GetMapping("/tasks/completed")
    public ResponseEntity<?> getCompletedTasks(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login first."
                    ));
        }

        String userEmail =
                authentication.getName();

        List<StudyTask> allTasks =
                studyTaskRepository
                        .findByUserEmail(userEmail);

        List<StudyTask> completedTasks =
                allTasks.stream()
                        .filter(task ->
                                task.getStatus() != null
                                        && task.getStatus()
                                        .equalsIgnoreCase("COMPLETED")
                        )
                        .toList();

        return ResponseEntity.ok(completedTasks);
    }

    // =====================================================
    // UPCOMING TASKS
    // =====================================================

    @GetMapping("/tasks/upcoming")
    public ResponseEntity<?> getUpcomingTasks(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login first."
                    ));
        }

        String userEmail =
                authentication.getName();

        LocalDate today =
                LocalDate.now();

        List<StudyTask> allTasks =
                studyTaskRepository
                        .findByUserEmail(userEmail);

        List<StudyTask> upcomingTasks =
                allTasks.stream()
                        .filter(task ->
                                task.getReadingDate() != null
                                        && task.getReadingDate()
                                        .isAfter(today)
                        )
                        .toList();

        return ResponseEntity.ok(upcomingTasks);
    }

    // =====================================================
    // OVERDUE TASKS
    // =====================================================

    @GetMapping("/tasks/overdue")
    public ResponseEntity<?> getOverdueTasks(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login first."
                    ));
        }

        String userEmail =
                authentication.getName();

        LocalDate today =
                LocalDate.now();

        List<StudyTask> allTasks =
                studyTaskRepository
                        .findByUserEmail(userEmail);

        List<StudyTask> overdueTasks =
                allTasks.stream()
                        .filter(task ->
                                task.getDeadline() != null
                                        && task.getDeadline()
                                        .isBefore(today)
                                        && (task.getStatus() == null
                                        || !task.getStatus()
                                        .equalsIgnoreCase("COMPLETED"))
                        )
                        .toList();

        return ResponseEntity.ok(overdueTasks);
    }

    // =====================================================
    // ALL TASKS
    // =====================================================

    @GetMapping("/tasks/all")
    public ResponseEntity<?> getAllTasks(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(401)
                    .body(Map.of(
                            "message",
                            "Please login first."
                    ));
        }

        String userEmail =
                authentication.getName();

        List<StudyTask> tasks =
                studyTaskRepository
                        .findByUserEmail(userEmail);

        return ResponseEntity.ok(tasks);
    }
}