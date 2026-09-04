package com.pravallika.student_preparation_tracker.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class RecurringTaskRequest {

    // =====================================================
    // OCCURRENCE ID
    // =====================================================

    /*
     * The specific occurrence the user wants to edit.
     *
     * Example:
     *
     * Task:
     * Java - Collections
     *
     * Occurrences:
     * 22 Aug
     * 23 Aug
     * 24 Aug
     *
     * If the user selects 24 Aug,
     * this will contain the ID of the 24 Aug occurrence.
     */

    private Long occurrenceId;


    // =====================================================
    // OCCURRENCE DATE
    // =====================================================

    /*
     * The date of the occurrence.
     *
     * Example:
     * 2026-08-24
     */

    private LocalDate occurrenceDate;


    // =====================================================
    // START TIME
    // =====================================================

    /*
     * New start time selected by the user.
     */

    private LocalTime startTime;


    // =====================================================
    // END TIME
    // =====================================================

    /*
     * New end time selected by the user.
     */

    private LocalTime endTime;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public RecurringTaskRequest() {
    }


    // =====================================================
    // GET OCCURRENCE ID
    // =====================================================

    public Long getOccurrenceId() {
        return occurrenceId;
    }


    // =====================================================
    // SET OCCURRENCE ID
    // =====================================================

    public void setOccurrenceId(Long occurrenceId) {
        this.occurrenceId = occurrenceId;
    }


    // =====================================================
    // GET OCCURRENCE DATE
    // =====================================================

    public LocalDate getOccurrenceDate() {
        return occurrenceDate;
    }


    // =====================================================
    // SET OCCURRENCE DATE
    // =====================================================

    public void setOccurrenceDate(
            LocalDate occurrenceDate) {

        this.occurrenceDate = occurrenceDate;
    }


    // =====================================================
    // GET START TIME
    // =====================================================

    public LocalTime getStartTime() {
        return startTime;
    }


    // =====================================================
    // SET START TIME
    // =====================================================

    public void setStartTime(
            LocalTime startTime) {

        this.startTime = startTime;
    }


    // =====================================================
    // GET END TIME
    // =====================================================

    public LocalTime getEndTime() {
        return endTime;
    }


    // =====================================================
    // SET END TIME
    // =====================================================

    public void setEndTime(
            LocalTime endTime) {

        this.endTime = endTime;
    }
}