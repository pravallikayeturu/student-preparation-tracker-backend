package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.repository.StudyTaskRepository;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatbotService {

    private final ChatClient chatClient;
    private final StudyTaskRepository studyTaskRepository;

    public ChatbotService(
            ChatClient.Builder chatClientBuilder,
            StudyTaskRepository studyTaskRepository) {

        this.chatClient = chatClientBuilder.build();
        this.studyTaskRepository = studyTaskRepository;
    }


    // =====================================================
    // MAIN CHATBOT METHOD
    // =====================================================

    public String getAnswer(
            String question,
            String userEmail) {

        if (question == null || question.trim().isEmpty()) {

            return "Please enter a question.";
        }

        String lowerQuestion =
                question.toLowerCase().trim();


        // =================================================
        // CHECK WHETHER QUESTION IS TASK RELATED
        // =================================================

        boolean taskRelated =
                isTaskRelated(lowerQuestion);


        // =================================================
        // TASK RELATED QUESTION
        // =================================================

        if (taskRelated) {

            return answerTaskRelatedQuestion(
                    question,
                    userEmail
            );
        }


        // =================================================
        // GENERAL QUESTION
        // =================================================

        return answerGeneralQuestion(question);
    }


    // =====================================================
    // CHECK TASK RELATED QUESTION
    // =====================================================

    private boolean isTaskRelated(String question) {

        String[] taskKeywords = {

                // Personal task questions
                "my task",
                "my tasks",
                "my schedule",
                "my deadline",
                "my deadlines",
                "my study",
                "my subject",
                "my topic",
                "my progress",

                // Task information
                "task",
                "tasks",
                "schedule",
                "scheduled",
                "deadline",
                "deadlines",

                // Dates
                "today",
                "tomorrow",
                "yesterday",

                // Task status
                "pending",
                "completed",
                "complete",
                "finished",
                "upcoming",
                "overdue",

                // Study planning
                "what should i study",
                "what do i study",
                "what should i learn",
                "what do i learn",

                "next task",
                "next study",
                "next session",

                // Task properties
                "priority",
                "subject",
                "topic",
                "revision",
                "reading",
                "session",

                // Planning questions
                "study plan",
                "study schedule",
                "preparation plan"
        };


        for (String keyword : taskKeywords) {

            if (question.contains(keyword)) {

                return true;
            }
        }


        return false;
    }


    // =====================================================
    // TASK RELATED ANSWER
    // =====================================================

    private String answerTaskRelatedQuestion(
            String question,
            String userEmail) {


        // =================================================
        // SECURITY
        // =================================================

        List<StudyTask> tasks =
                studyTaskRepository.findByUserEmail(userEmail);


        // =================================================
        // BUILD USER TASK CONTEXT
        // =================================================

        StringBuilder taskContext =
                new StringBuilder();

        taskContext.append(
                "IMPORTANT SECURITY RULE:\n"
                        + "The following task information belongs "
                        + "ONLY to the currently logged-in user.\n"
                        + "Never reveal information from another user.\n"
                        + "Never invent missing task information.\n\n"
        );


        if (tasks.isEmpty()) {

            taskContext.append(
                    "The user currently has no study tasks.\n"
            );

        } else {

            for (StudyTask task : tasks) {

                taskContext.append(
                        "Subject: "
                                + safeValue(task.getSubject())
                                + "\n"
                );

                taskContext.append(
                        "Topic: "
                                + safeValue(task.getTopic())
                                + "\n"
                );

                taskContext.append(
                        "Description: "
                                + safeValue(task.getDescription())
                                + "\n"
                );

                taskContext.append(
                        "Reading Date: "
                                + safeValue(task.getReadingDate())
                                + "\n"
                );

                taskContext.append(
                        "Start Time: "
                                + safeValue(task.getStartTime())
                                + "\n"
                );

                taskContext.append(
                        "End Time: "
                                + safeValue(task.getEndTime())
                                + "\n"
                );

                taskContext.append(
                        "Deadline: "
                                + safeValue(task.getDeadline())
                                + "\n"
                );

                taskContext.append(
                        "Priority: "
                                + safeValue(task.getPriority())
                                + "\n"
                );

                taskContext.append(
                        "Status: "
                                + safeValue(task.getStatus())
                                + "\n"
                );

                taskContext.append(
                        "Recurrence Type: "
                                + safeValue(task.getRecurrenceType())
                                + "\n"
                );


                if (task.getRecurrenceDays() != null) {

                    taskContext.append(
                            "Recurrence Days: "
                                    + task.getRecurrenceDays()
                                    + "\n"
                    );
                }


                if (task.getRecurrenceDayOfMonth() != null) {

                    taskContext.append(
                            "Recurrence Day Of Month: "
                                    + task.getRecurrenceDayOfMonth()
                                    + "\n"
                    );
                }


                if (task.getRecurrenceEndDate() != null) {

                    taskContext.append(
                            "Recurrence End Date: "
                                    + task.getRecurrenceEndDate()
                                    + "\n"
                    );
                }


                taskContext.append(
                        "--------------------------------\n"
                );
            }
        }


        // =================================================
        // ASK AI USING ONLY CURRENT USER'S TASKS
        // =================================================

        return chatClient
                .prompt()

                .system("""
                        You are a helpful AI Study Assistant
                        for a Student Preparation Tracker.

                        The user is asking about their personal
                        study tasks, schedule, deadlines,
                        subjects, progress, or preparation.


                        =================================================
                        SECURITY RULES
                        =================================================

                        1. The task information provided below
                           belongs ONLY to the currently
                           authenticated user.

                        2. NEVER reveal, use, or guess information
                           belonging to another user.

                        3. NEVER invent tasks, subjects, topics,
                           dates, times, deadlines, priorities,
                           statuses, or recurrence information.

                        4. Use ONLY the task information provided
                           below when answering task-related questions.

                        5. If the requested information is not
                           available in the provided task data,
                           say:

                           "I don't have that information in your tasks."

                        6. Do not show unrelated tasks.

                        7. If the user asks for today's tasks,
                           show only today's relevant tasks.

                        8. If the user asks for tomorrow's tasks,
                           show only tomorrow's relevant tasks.

                        9. If the user asks for pending tasks,
                           show only pending tasks.

                        10. If the user asks for completed tasks,
                            show only completed tasks.

                        11. If the user asks for upcoming tasks,
                            show only upcoming tasks.

                        12. If the user asks for overdue tasks,
                            show only overdue tasks.

                        13. If the user asks for all tasks,
                            show all available tasks belonging
                            to this authenticated user.


                        =================================================
                        RESPONSE LENGTH RULES
                        =================================================

                        IMPORTANT:
                        Follow the user's requested answer length.

                        NORMAL QUESTION:
                        If the user does NOT ask for a detailed,
                        deep, thorough, or step-by-step explanation:

                        - Give a SHORT answer.
                        - Maximum 3 lines.
                        - Give only the information necessary
                          to answer the question.
                        - Do NOT add extra explanations.
                        - Do NOT add unrelated examples.
                        - Do NOT repeat the question.

                        BRIEF QUESTION:
                        If the user says:
                        "briefly", "brief", "short", "in short",
                        or "shortly":

                        - Give a very short answer.
                        - Maximum 1-2 lines.

                        DETAILED QUESTION:
                        If the user says:
                        "deeply", "in detail", "detailed",
                        "explain deeply", "explain thoroughly",
                        "complete explanation", or
                        "step by step":

                        - Give a detailed explanation.
                        - Include important details needed
                          to understand the topic.
                        - Use sections or bullet points when useful.
                        - Do not make the answer longer than
                          necessary.

                        EXAMPLE REQUEST:
                        If the user asks for an example:

                        - Give the requested example.
                        - Keep the explanation short unless
                          the user also asks for detail.

                        CODE REQUEST:
                        If the user asks for code:

                        - Provide the required code.
                        - Explain it briefly unless the user
                          explicitly asks for a detailed explanation.


                        =================================================
                        LANGUAGE RULES
                        =================================================

                        1. Answer in the same language used
                           by the user.

                        2. If the user asks in Telugu,
                           answer in Telugu.

                        3. If the user asks in English,
                           answer in English.

                        4. If the user uses Telugu + English,
                           naturally respond using the same
                           Telugu + English style.

                        5. Do not translate unnecessarily.


                        =================================================
                        TASK INFORMATION
                        =================================================

                        USER TASK INFORMATION:
                        """
                        + taskContext)

                .user(
                        "User Question:\n"
                                + question
                )

                .call()

                .content();
    }


    // =====================================================
    // GENERAL QUESTION ANSWER
    // =====================================================

    private String answerGeneralQuestion(
            String question) {

        return chatClient
                .prompt()

                .system("""
                        You are a helpful AI Study Assistant.

                        Answer the user's question according
                        to exactly what they are asking.


                        =================================================
                        RESPONSE LENGTH RULES
                        =================================================

                        IMPORTANT:
                        Follow the user's requested answer length.

                        NORMAL QUESTION:

                        If the user does NOT ask for:
                        - detailed
                        - deeply
                        - in detail
                        - thoroughly
                        - complete explanation
                        - step by step

                        then:

                        - Give a SHORT answer.
                        - Maximum 3 lines.
                        - Answer directly.
                        - Give only necessary information.
                        - Do NOT add unnecessary explanations.
                        - Do NOT add unrelated examples.
                        - Do NOT repeat the question.


                        BRIEF QUESTION:

                        If the user says:
                        "briefly", "brief", "short",
                        "in short", or "shortly":

                        - Give a very short answer.
                        - Maximum 1-2 lines.


                        DETAILED QUESTION:

                        If the user says:
                        "deeply", "in detail", "detailed",
                        "explain deeply", "explain thoroughly",
                        "complete explanation", or
                        "step by step":

                        - Give a detailed explanation.
                        - Include important details,
                          examples, and reasoning when useful.
                        - Use bullet points or sections
                          when appropriate.
                        - Do not add irrelevant information.


                        EXAMPLE REQUEST:

                        If the user asks for an example:

                        - Give a clear example.
                        - Keep the explanation short unless
                          the user asks for more detail.


                        CODE REQUEST:

                        If the user asks for code:

                        - Provide suitable code.
                        - Give only a short explanation unless
                          the user asks for a detailed explanation.


                        =================================================
                        LANGUAGE RULES
                        =================================================

                        1. Answer in the same language used
                           by the user.

                        2. If the user asks in Telugu,
                           answer in Telugu.

                        3. If the user asks in English,
                           answer in English.

                        4. If the user uses Telugu + English,
                           naturally respond using the same
                           Telugu + English style.

                        5. Do not translate unnecessarily.


                        =================================================
                        EDUCATIONAL QUESTIONS
                        =================================================

                        You can answer questions about:

                        - Java
                        - Python
                        - DSA
                        - DBMS
                        - Operating Systems
                        - Computer Networks
                        - Computer Science
                        - Programming
                        - Mathematics
                        - Technology
                        - Other educational topics
                        - General knowledge questions


                        =================================================
                        TASK PRIVACY
                        =================================================

                        Do NOT mention or use the user's personal
                        study tasks for a general question.

                        Do NOT invent personal task information.

                        Only discuss personal tasks when the user
                        explicitly asks about their own tasks.
                        """)

                .user(question)

                .call()

                .content();
    }


    // =====================================================
    // SAFE VALUE
    // =====================================================

    private String safeValue(Object value) {

        if (value == null) {

            return "Not available";
        }

        return String.valueOf(value);
    }
}