package com.pravallika.student_preparation_tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "study_tasks")
public class StudyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    private String topic;

    private String description;

    // =========================================
    // ORIGINAL SCHEDULE
    // =========================================

    private LocalDate originalReadingDate;

    private LocalTime originalStartTime;

    private LocalTime originalEndTime;

    // =========================================
    // TASK DETAILS
    // =========================================

    @Enumerated(EnumType.STRING)
    private Priority priority;

    private LocalDate readingDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate deadline;

    private String status;

    private String userEmail;

    // =========================================
    // NOTIFICATION
    // =========================================

    /*
     * false = reminder has not been sent
     * true  = reminder has already been sent
     *
     * This prevents duplicate reminder emails.
     */
    private boolean reminderSent = false;

    public enum Priority {
        HIGH,
        MEDIUM,
        LOW
    }

    // =========================================
    // CONSTRUCTOR
    // =========================================

    public StudyTask() {
    }

    // =========================================
    // GETTERS AND SETTERS
    // =========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // =========================================
    // ORIGINAL READING DATE
    // =========================================

    public LocalDate getOriginalReadingDate() {
        return originalReadingDate;
    }

    public void setOriginalReadingDate(
            LocalDate originalReadingDate) {

        this.originalReadingDate = originalReadingDate;
    }

    // =========================================
    // ORIGINAL START TIME
    // =========================================

    public LocalTime getOriginalStartTime() {
        return originalStartTime;
    }

    public void setOriginalStartTime(
            LocalTime originalStartTime) {

        this.originalStartTime = originalStartTime;
    }

    // =========================================
    // ORIGINAL END TIME
    // =========================================

    public LocalTime getOriginalEndTime() {
        return originalEndTime;
    }

    public void setOriginalEndTime(
            LocalTime originalEndTime) {

        this.originalEndTime = originalEndTime;
    }

    // =========================================
    // PRIORITY
    // =========================================

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    // =========================================
    // READING DATE
    // =========================================

    public LocalDate getReadingDate() {
        return readingDate;
    }

    public void setReadingDate(
            LocalDate readingDate) {

        this.readingDate = readingDate;
    }

    // =========================================
    // START TIME
    // =========================================

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(
            LocalTime startTime) {

        this.startTime = startTime;
    }

    // =========================================
    // END TIME
    // =========================================

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(
            LocalTime endTime) {

        this.endTime = endTime;
    }

    // =========================================
    // DEADLINE
    // =========================================

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(
            LocalDate deadline) {

        this.deadline = deadline;
    }

    // =========================================
    // STATUS
    // =========================================

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }

    // =========================================
    // USER EMAIL
    // =========================================

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(
            String userEmail) {

        this.userEmail = userEmail;
    }

    // =========================================
    // REMINDER SENT
    // =========================================

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(
            boolean reminderSent) {

        this.reminderSent = reminderSent;
    }
}
