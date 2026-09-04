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
    // RECURRENCE
    // =========================================

    /*
     * ONE_TIME = task occurs only once
     *
     * DAILY = task occurs every day
     *
     * WEEKLY = task occurs on selected weekdays
     *
     * MONTHLY = task occurs on selected day of each month
     */
    private String recurrenceType = "ONE_TIME";

    /*
     * Used for WEEKLY recurrence.
     *
     * Example:
     *
     * MONDAY,WEDNESDAY,FRIDAY
     *
     * For ONE_TIME, DAILY and MONTHLY this can be null.
     */
    @Column(length = 200)
    private String recurrenceDays;

    /*
     * Used for MONTHLY recurrence.
     *
     * Example:
     *
     * 23 = 23rd of every month
     *
     * 31 = 31st of every month
     *
     * If the selected day does not exist in a month,
     * the occurrence will use the last day of that month.
     *
     * Example:
     *
     * Monthly day = 31
     *
     * February -> February 28/29
     * April    -> April 30
     * June     -> June 30
     */
    private Integer recurrenceDayOfMonth;

    /*
     * Optional date on which recurrence should stop.
     *
     * Example:
     *
     * Start: August 23
     * End:   December 23
     *
     * Monthly occurrences:
     *
     * August 23
     * September 23
     * October 23
     * November 23
     * December 23
     */
    private LocalDate recurrenceEndDate;

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

    // =========================================
    // PRIORITY ENUM
    // =========================================

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
    // ID
    // =========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // =========================================
    // SUBJECT
    // =========================================

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    // =========================================
    // TOPIC
    // =========================================

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    // =========================================
    // DESCRIPTION
    // =========================================

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
    // RECURRENCE TYPE
    // =========================================

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(
            String recurrenceType) {

        this.recurrenceType = recurrenceType;
    }

    // =========================================
    // RECURRENCE DAYS
    // =========================================

    public String getRecurrenceDays() {
        return recurrenceDays;
    }

    public void setRecurrenceDays(
            String recurrenceDays) {

        this.recurrenceDays = recurrenceDays;
    }

    // =========================================
    // RECURRENCE DAY OF MONTH
    // =========================================

    public Integer getRecurrenceDayOfMonth() {
        return recurrenceDayOfMonth;
    }

    public void setRecurrenceDayOfMonth(
            Integer recurrenceDayOfMonth) {

        this.recurrenceDayOfMonth = recurrenceDayOfMonth;
    }

    // =========================================
    // RECURRENCE END DATE
    // =========================================

    public LocalDate getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(
            LocalDate recurrenceEndDate) {

        this.recurrenceEndDate = recurrenceEndDate;
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