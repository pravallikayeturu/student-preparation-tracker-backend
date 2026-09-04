package com.pravallika.student_preparation_tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
    name = "study_task_occurrences",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_study_task_occurrence_date",
            columnNames = {"study_task_id", "occurrence_date"}
        )
    }
)
public class StudyTaskOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // PARENT STUDY TASK
    // =====================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "study_task_id",
        nullable = false
    )
    private StudyTask studyTask;


    // =====================================================
    // OCCURRENCE DATE
    // =====================================================

    @Column(
        name = "occurrence_date",
        nullable = false
    )
    private LocalDate occurrenceDate;


    // =====================================================
    // START TIME
    // =====================================================

    @Column(
        name = "start_time",
        nullable = false
    )
    private LocalTime startTime;


    // =====================================================
    // END TIME
    // =====================================================

    @Column(
        name = "end_time",
        nullable = false
    )
    private LocalTime endTime;


    // =====================================================
    // STATUS
    // =====================================================

    @Column(
        name = "status",
        nullable = false
    )
    private String status = "PENDING";


    // =====================================================
    // REMINDER SENT
    // =====================================================

    @Column(
        name = "reminder_sent",
        nullable = false
    )
    private boolean reminderSent = false;


    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public StudyTaskOccurrence() {
    }


    // =====================================================
    // ID
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // =====================================================
    // STUDY TASK
    // =====================================================

    public StudyTask getStudyTask() {
        return studyTask;
    }

    public void setStudyTask(StudyTask studyTask) {
        this.studyTask = studyTask;
    }


    // =====================================================
    // OCCURRENCE DATE
    // =====================================================

    public LocalDate getOccurrenceDate() {
        return occurrenceDate;
    }

    public void setOccurrenceDate(LocalDate occurrenceDate) {
        this.occurrenceDate = occurrenceDate;
    }


    // =====================================================
    // START TIME
    // =====================================================

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }


    // =====================================================
    // END TIME
    // =====================================================

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }


    // =====================================================
    // STATUS
    // =====================================================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    // =====================================================
    // REMINDER SENT
    // =====================================================

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }
}