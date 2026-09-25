package com.pravallika.student_preparation_tracker.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================
    // USER EMAIL
    // =========================================

    @Column(nullable = false)
    private String userEmail;

    // =========================================
    // NOTIFICATION TYPE
    // study / progress / deadline
    // =========================================

    @Column(nullable = false)
    private String type;

    // =========================================
    // TITLE
    // =========================================

    @Column(nullable = false)
    private String title;

    // =========================================
    // MESSAGE
    // =========================================

    @Column(nullable = false, length = 1000)
    private String message;

    // =========================================
    // READ STATUS
    // =========================================

    @Column(nullable = false)
    private boolean read = false;

    // =========================================
    // CREATED TIME
    // =========================================

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // =========================================
    // STUDY TASK ID
    // =========================================

    private Long studyTaskId;

    // =========================================
    // DEADLINE
    // =========================================

    private LocalDate deadline;

    // =========================================
    // OCCURRENCE DATE
    //
    // Important for recurring tasks.
    //
    // Example:
    //
    // DAILY task
    // Sep 25 -> occurrenceDate = Sep 25
    // Sep 26 -> occurrenceDate = Sep 26
    //
    // WEEKLY task
    // Sep 25 -> occurrenceDate = Sep 25
    // Oct 02 -> occurrenceDate = Oct 02
    // =========================================

    private LocalDate occurrenceDate;

    // =========================================
    // CONSTRUCTOR
    // =========================================

    public Notification() {
    }

    public Notification(
            String userEmail,
            String type,
            String title,
            String message,
            Long studyTaskId
    ) {

        this.userEmail = userEmail;
        this.type = type;
        this.title = title;
        this.message = message;
        this.studyTaskId = studyTaskId;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    // =========================================
    // GET ID
    // =========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // =========================================
    // GET USER EMAIL
    // =========================================

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // =========================================
    // GET TYPE
    // =========================================

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // =========================================
    // GET TITLE
    // =========================================

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // =========================================
    // GET MESSAGE
    // =========================================

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // =========================================
    // GET READ
    // =========================================

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    // =========================================
    // GET CREATED AT
    // =========================================

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // =========================================
    // GET STUDY TASK ID
    // =========================================

    public Long getStudyTaskId() {
        return studyTaskId;
    }

    public void setStudyTaskId(Long studyTaskId) {
        this.studyTaskId = studyTaskId;
    }

    // =========================================
    // GET DEADLINE
    // =========================================

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    // =========================================
    // GET OCCURRENCE DATE
    // =========================================

    public LocalDate getOccurrenceDate() {
        return occurrenceDate;
    }

    public void setOccurrenceDate(LocalDate occurrenceDate) {
        this.occurrenceDate = occurrenceDate;
    }
}