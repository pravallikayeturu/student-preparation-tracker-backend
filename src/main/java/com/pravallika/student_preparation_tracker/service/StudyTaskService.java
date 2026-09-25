package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.StudyTask;
import com.pravallika.student_preparation_tracker.entity.StudyTaskOccurrence;
import com.pravallika.student_preparation_tracker.repository.StudyTaskOccurrenceRepository;
import com.pravallika.student_preparation_tracker.repository.StudyTaskRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class StudyTaskService {

    private final StudyTaskRepository studyTaskRepository;
    private final StudyTaskOccurrenceRepository occurrenceRepository;

    /*
     * All task dates and times are handled using
     * Indian Standard Time.
     */
    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    /*
     * For recurring tasks without an end date,
     * we maintain occurrences for the next 365 days.
     *
     * This allows reminders to work continuously without
     * creating an infinite number of database rows.
     */
    private static final int ONGOING_RECURRENCE_DAYS = 365;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public StudyTaskService(
            StudyTaskRepository studyTaskRepository,
            StudyTaskOccurrenceRepository occurrenceRepository) {

        this.studyTaskRepository = studyTaskRepository;
        this.occurrenceRepository = occurrenceRepository;
    }


    // =====================================================
    // CREATE STUDY TASK
    // =====================================================

    public StudyTask createTask(
            StudyTask task,
            String userEmail) {

        if (task == null) {
            throw new RuntimeException(
                    "Task data cannot be empty."
            );
        }

        validateUserEmail(userEmail);

        validateBasicTaskDetails(task);

        validateReadingDate(
                task.getReadingDate()
        );

        validateTime(
                task.getStartTime(),
                task.getEndTime()
        );

        validateRecurrence(task);

        task.setUserEmail(userEmail);

        if (task.getStatus() == null
                || task.getStatus().trim().isEmpty()) {

            task.setStatus("PENDING");
        }


        // -------------------------------------------------
        // STORE ORIGINAL SCHEDULE
        // -------------------------------------------------

        task.setOriginalReadingDate(
                task.getReadingDate()
        );

        task.setOriginalStartTime(
                task.getStartTime()
        );

        task.setOriginalEndTime(
                task.getEndTime()
        );

        /*
         * Parent reminder flag is retained for compatibility.
         *
         * Actual recurring reminder tracking is done by
         * StudyTaskOccurrence.reminderSent.
         */
        task.setReminderSent(false);


        // -------------------------------------------------
        // CHECK OVERLAP
        // -------------------------------------------------

        checkRecurringTaskOverlap(
                task,
                userEmail,
                null
        );


        // -------------------------------------------------
        // SAVE PARENT TASK
        // -------------------------------------------------

        StudyTask savedTask =
                studyTaskRepository.save(task);


        // -------------------------------------------------
        // GENERATE OCCURRENCES
        // -------------------------------------------------

        generateOccurrences(savedTask);

        return savedTask;
    }


    // =====================================================
    // GENERATE OCCURRENCES
    // =====================================================

    private void generateOccurrences(
            StudyTask task) {

        List<LocalDate> occurrenceDates =
                generateOccurrenceDates(task);

        for (LocalDate occurrenceDate :
                occurrenceDates) {

            createOccurrenceIfMissing(
                    task,
                    occurrenceDate
            );
        }
    }


    // =====================================================
    // GENERATE OCCURRENCE DATES
    // =====================================================

    private List<LocalDate> generateOccurrenceDates(
            StudyTask task) {

        List<LocalDate> occurrenceDates =
                new ArrayList<>();

        if (task == null) {
            return occurrenceDates;
        }

        LocalDate startDate =
                task.getReadingDate();

        if (startDate == null) {
            return occurrenceDates;
        }


        String recurrenceType =
                task.getRecurrenceType();

        if (recurrenceType == null
                || recurrenceType.trim().isEmpty()) {

            recurrenceType = "ONE_TIME";
        }

        recurrenceType =
                recurrenceType
                        .trim()
                        .toUpperCase();


        // =================================================
        // ONE TIME
        // =================================================

        if (recurrenceType.equals("ONE_TIME")) {

            occurrenceDates.add(startDate);

            return occurrenceDates;
        }


        // =================================================
        // DETERMINE END DATE FOR RECURRING TASK
        // =================================================

        LocalDate endDate =
                task.getRecurrenceEndDate();

        /*
         * If the user supplied an end date,
         * use that exact date.
         *
         * If no end date was supplied, maintain a
         * rolling one-year occurrence window.
         */
        if (endDate == null) {

            endDate =
                    LocalDate.now(INDIA_ZONE)
                            .plusDays(
                                    ONGOING_RECURRENCE_DAYS
                            );

            /*
             * Never allow the generated range to end
             * before the task's starting date.
             */
            if (endDate.isBefore(startDate)) {
                endDate = startDate
                        .plusDays(
                                ONGOING_RECURRENCE_DAYS
                        );
            }
        }


        if (endDate.isBefore(startDate)) {

            throw new RuntimeException(
                    "Recurrence end date cannot be before "
                            + "the study start date."
            );
        }


        // =================================================
        // DAILY
        // =================================================

        if (recurrenceType.equals("DAILY")) {

            LocalDate currentDate =
                    startDate;

            while (!currentDate.isAfter(endDate)) {

                occurrenceDates.add(
                        currentDate
                );

                currentDate =
                        currentDate.plusDays(1);
            }

            return occurrenceDates;
        }


        // =================================================
        // WEEKLY
        // =================================================

        if (recurrenceType.equals("WEEKLY")) {

            Set<DayOfWeek> selectedDays =
                    parseWeeklyDays(
                            task.getRecurrenceDays()
                    );

            if (selectedDays.isEmpty()) {

                throw new RuntimeException(
                        "Please select at least one day "
                                + "for weekly recurrence."
                );
            }

            LocalDate currentDate =
                    startDate;

            while (!currentDate.isAfter(endDate)) {

                if (selectedDays.contains(
                        currentDate.getDayOfWeek()
                )) {

                    occurrenceDates.add(
                            currentDate
                    );
                }

                currentDate =
                        currentDate.plusDays(1);
            }

            return occurrenceDates;
        }


        // =================================================
        // MONTHLY
        // =================================================

        if (recurrenceType.equals("MONTHLY")) {

            Integer requestedDay =
                    task.getRecurrenceDayOfMonth();

            if (requestedDay == null) {

                throw new RuntimeException(
                        "Monthly recurrence day is required."
                );
            }

            if (requestedDay < 1
                    || requestedDay > 31) {

                throw new RuntimeException(
                        "Monthly recurrence day must be "
                                + "between 1 and 31."
                );
            }

            LocalDate currentDate =
                    startDate.withDayOfMonth(1);

            while (!currentDate.isAfter(endDate)) {

                YearMonth yearMonth =
                        YearMonth.from(currentDate);

                /*
                 * Example:
                 *
                 * requestedDay = 31
                 *
                 * February -> 28/29
                 * April    -> 30
                 * June     -> 30
                 */
                int actualDay =
                        Math.min(
                                requestedDay,
                                yearMonth.lengthOfMonth()
                        );

                LocalDate occurrenceDate =
                        yearMonth.atDay(actualDay);

                if (!occurrenceDate.isBefore(startDate)
                        && !occurrenceDate.isAfter(endDate)) {

                    occurrenceDates.add(
                            occurrenceDate
                    );
                }

                currentDate =
                        currentDate.plusMonths(1);
            }

            return occurrenceDates;
        }


        // =================================================
        // CUSTOM NOT SUPPORTED
        // =================================================

        if (recurrenceType.equals("CUSTOM")) {

            throw new RuntimeException(
                    "CUSTOM recurrence is not supported. "
                            + "Use ONE_TIME, DAILY, WEEKLY or MONTHLY."
            );
        }


        // =================================================
        // INVALID
        // =================================================

        throw new RuntimeException(
                "Invalid recurrence type. "
                        + "Allowed values are ONE_TIME, DAILY, "
                        + "WEEKLY or MONTHLY."
        );
    }


    // =====================================================
    // CREATE OCCURRENCE IF MISSING
    // =====================================================

    private void createOccurrenceIfMissing(
            StudyTask task,
            LocalDate occurrenceDate) {

        if (task == null
                || task.getId() == null
                || occurrenceDate == null) {

            return;
        }


        StudyTaskOccurrence existing =
                occurrenceRepository
                        .findByStudyTaskIdAndOccurrenceDate(
                                task.getId(),
                                occurrenceDate
                        )
                        .orElse(null);


        /*
         * Unique occurrence:
         *
         * task + date
         *
         * Never insert a duplicate.
         */
        if (existing != null) {
            return;
        }


        StudyTaskOccurrence occurrence =
                new StudyTaskOccurrence();

        occurrence.setStudyTask(task);

        occurrence.setOccurrenceDate(
                occurrenceDate
        );

        occurrence.setStartTime(
                task.getStartTime()
        );

        occurrence.setEndTime(
                task.getEndTime()
        );

        occurrence.setStatus(
                "PENDING"
        );

        /*
         * Every occurrence has its own reminder state.
         */
        occurrence.setReminderSent(false);


        occurrenceRepository.save(
                occurrence
        );
    }


    // =====================================================
    // MAINTAIN ONGOING RECURRING OCCURRENCES
    // =====================================================

    /*
     * Runs every day at approximately 12:05 AM IST.
     *
     * For recurring tasks without an end date,
     * this extends the occurrence window forward.
     *
     * Existing occurrences are never duplicated.
     */
    @Scheduled(
            cron = "0 5 0 * * *",
            zone = "Asia/Kolkata"
    )
    public void maintainRecurringOccurrences() {

        List<StudyTask> tasks =
                studyTaskRepository.findAll();

        if (tasks == null
                || tasks.isEmpty()) {

            return;
        }

        for (StudyTask task : tasks) {

            if (task == null
                    || task.getId() == null) {

                continue;
            }

            String recurrenceType =
                    task.getRecurrenceType();

            if (recurrenceType == null
                    || recurrenceType.trim().isEmpty()) {

                continue;
            }

            recurrenceType =
                    recurrenceType
                            .trim()
                            .toUpperCase();

            /*
             * Only recurring tasks need extension.
             */
            if (recurrenceType.equals("ONE_TIME")
                    || recurrenceType.equals("CUSTOM")) {

                continue;
            }

            /*
             * If an explicit end date exists,
             * all required occurrences were already
             * generated according to that end date.
             */
            if (task.getRecurrenceEndDate() != null) {

                continue;
            }

            generateOccurrences(task);
        }
    }


    // =====================================================
    // PARSE WEEKLY DAYS
    // =====================================================

    private Set<DayOfWeek> parseWeeklyDays(
            String recurrenceDays) {

        Set<DayOfWeek> selectedDays =
                new HashSet<>();

        if (recurrenceDays == null
                || recurrenceDays.trim().isEmpty()) {

            return selectedDays;
        }

        String[] days =
                recurrenceDays
                        .toUpperCase()
                        .split(",");

        for (String day : days) {

            String trimmedDay =
                    day.trim();

            if (!trimmedDay.isEmpty()) {

                try {

                    selectedDays.add(
                            DayOfWeek.valueOf(
                                    trimmedDay
                            )
                    );

                } catch (IllegalArgumentException e) {

                    throw new RuntimeException(
                            "Invalid weekly day: "
                                    + trimmedDay
                    );
                }
            }
        }

        return selectedDays;
    }


    // =====================================================
    // BASIC TASK VALIDATION
    // =====================================================

    private void validateBasicTaskDetails(
            StudyTask task) {

        if (task.getSubject() == null
                || task.getSubject().trim().isEmpty()) {

            throw new RuntimeException(
                    "Please enter the subject."
            );
        }

        if (task.getTopic() == null
                || task.getTopic().trim().isEmpty()) {

            throw new RuntimeException(
                    "Please enter the topic."
            );
        }

        if (task.getReadingDate() == null) {

            throw new RuntimeException(
                    "Please select a study date."
            );
        }

        if (task.getStartTime() == null) {

            throw new RuntimeException(
                    "Please enter the start time."
            );
        }

        if (task.getEndTime() == null) {

            throw new RuntimeException(
                    "Please enter the end time."
            );
        }
    }


    // =====================================================
    // USER EMAIL VALIDATION
    // =====================================================

    private void validateUserEmail(
            String userEmail) {

        if (userEmail == null
                || userEmail.trim().isEmpty()) {

            throw new RuntimeException(
                    "User email is required."
            );
        }
    }


    // =====================================================
    // READING DATE VALIDATION
    // =====================================================

    private void validateReadingDate(
            LocalDate readingDate) {

        if (readingDate == null) {

            throw new RuntimeException(
                    "Please select a study date."
            );
        }

        LocalDate today =
                LocalDate.now(INDIA_ZONE);

        if (readingDate.isBefore(today)) {

            throw new RuntimeException(
                    "You cannot create or move a study task "
                            + "to a past date."
            );
        }
    }


    // =====================================================
    // TIME VALIDATION
    // =====================================================

    private void validateTime(
            LocalTime startTime,
            LocalTime endTime) {

        if (startTime == null
                || endTime == null) {

            throw new RuntimeException(
                    "Please enter both start time and end time."
            );
        }

        if (!endTime.isAfter(startTime)) {

            throw new RuntimeException(
                    "Please enter a valid timing. "
                            + "End time must be after start time."
            );
        }
    }


    // =====================================================
    // RECURRENCE VALIDATION
    // =====================================================

    private void validateRecurrence(
            StudyTask task) {

        String recurrenceType =
                task.getRecurrenceType();

        if (recurrenceType == null
                || recurrenceType.trim().isEmpty()) {

            recurrenceType = "ONE_TIME";

            task.setRecurrenceType(
                    recurrenceType
            );
        }

        recurrenceType =
                recurrenceType
                        .trim()
                        .toUpperCase();

        task.setRecurrenceType(
                recurrenceType
        );


        // =================================================
        // ONE TIME
        // =================================================

        if (recurrenceType.equals("ONE_TIME")) {

            task.setRecurrenceDays(null);

            task.setRecurrenceDayOfMonth(null);

            task.setRecurrenceEndDate(null);

            return;
        }


        // =================================================
        // DAILY
        // =================================================

        if (recurrenceType.equals("DAILY")) {

            task.setRecurrenceDays(null);

            task.setRecurrenceDayOfMonth(null);

            validateRecurrenceEndDate(task);

            return;
        }


        // =================================================
        // WEEKLY
        // =================================================

        if (recurrenceType.equals("WEEKLY")) {

            if (task.getRecurrenceDays() == null
                    || task.getRecurrenceDays()
                    .trim()
                    .isEmpty()) {

                throw new RuntimeException(
                        "Please select at least one day "
                                + "for weekly recurrence."
                );
            }

            validateWeeklyDays(
                    task.getRecurrenceDays()
            );

            task.setRecurrenceDays(
                    normalizeWeeklyDays(
                            task.getRecurrenceDays()
                    )
            );

            task.setRecurrenceDayOfMonth(null);

            validateRecurrenceEndDate(task);

            return;
        }


        // =================================================
        // MONTHLY
        // =================================================

        if (recurrenceType.equals("MONTHLY")) {

            Integer dayOfMonth =
                    task.getRecurrenceDayOfMonth();

            if (dayOfMonth == null) {

                throw new RuntimeException(
                        "Please select a day of the month "
                                + "for monthly recurrence."
                );
            }

            if (dayOfMonth < 1
                    || dayOfMonth > 31) {

                throw new RuntimeException(
                        "Monthly recurrence day must be "
                                + "between 1 and 31."
                );
            }

            task.setRecurrenceDays(null);

            validateRecurrenceEndDate(task);

            return;
        }


        // =================================================
        // CUSTOM NOT SUPPORTED
        // =================================================

        if (recurrenceType.equals("CUSTOM")) {

            throw new RuntimeException(
                    "CUSTOM recurrence is not supported. "
                            + "Use ONE_TIME, DAILY, WEEKLY or MONTHLY."
            );
        }


        // =================================================
        // INVALID
        // =================================================

        throw new RuntimeException(
                "Invalid recurrence type. "
                        + "Allowed values are ONE_TIME, DAILY, "
                        + "WEEKLY or MONTHLY."
        );
    }


    // =====================================================
    // RECURRENCE END DATE VALIDATION
    // =====================================================

    private void validateRecurrenceEndDate(
            StudyTask task) {

        LocalDate recurrenceEndDate =
                task.getRecurrenceEndDate();

        /*
         * Null means ongoing recurrence.
         */
        if (recurrenceEndDate == null) {
            return;
        }

        LocalDate startDate =
                task.getReadingDate();

        if (recurrenceEndDate.isBefore(
                startDate)) {

            throw new RuntimeException(
                    "Recurrence end date cannot be before "
                            + "the study start date."
            );
        }
    }


    // =====================================================
    // WEEKLY DAY VALIDATION
    // =====================================================

    private void validateWeeklyDays(
            String recurrenceDays) {

        String[] days =
                recurrenceDays
                        .toUpperCase()
                        .split(",");

        Set<String> validDays =
                new HashSet<>();

        for (DayOfWeek day :
                DayOfWeek.values()) {

            validDays.add(
                    day.name()
            );
        }

        Set<String> selectedDays =
                new HashSet<>();

        for (String day : days) {

            String trimmedDay =
                    day.trim();

            if (trimmedDay.isEmpty()) {
                continue;
            }

            if (!validDays.contains(
                    trimmedDay)) {

                throw new RuntimeException(
                        "Invalid weekly day: "
                                + trimmedDay
                                + ". Use MONDAY, TUESDAY, "
                                + "WEDNESDAY, THURSDAY, FRIDAY, "
                                + "SATURDAY or SUNDAY."
                );
            }

            selectedDays.add(
                    trimmedDay
            );
        }

        if (selectedDays.isEmpty()) {

            throw new RuntimeException(
                    "Please select at least one "
                            + "weekly recurrence day."
            );
        }
    }


    // =====================================================
    // NORMALIZE WEEKLY DAYS
    // =====================================================

    private String normalizeWeeklyDays(
            String recurrenceDays) {

        String[] days =
                recurrenceDays
                        .toUpperCase()
                        .split(",");

        Set<String> uniqueDays =
                new LinkedHashSet<>();

        for (String day : days) {

            String trimmedDay =
                    day.trim();

            if (!trimmedDay.isEmpty()) {

                uniqueDays.add(
                        trimmedDay
                );
            }
        }

        return String.join(
                ",",
                uniqueDays
        );
    }


    // =====================================================
    // GET ALL STUDY TASKS
    // =====================================================

    public List<StudyTask> getTasks(
            String userEmail) {

        validateUserEmail(userEmail);

        return studyTaskRepository
                .findByUserEmail(userEmail);
    }


    // =====================================================
    // CHECK RECURRING TASK OVERLAP
    // =====================================================

    private void checkRecurringTaskOverlap(
            StudyTask task,
            String userEmail,
            Long ignoredTaskId) {

        List<LocalDate> dates =
                generateOccurrenceDatesForValidation(
                        task
                );

        if (dates.isEmpty()) {
            return;
        }

        List<StudyTaskOccurrence> existingOccurrences =
                getUserOccurrences(userEmail);

        for (LocalDate date : dates) {

            for (StudyTaskOccurrence existing :
                    existingOccurrences) {

                if (existing == null) {
                    continue;
                }

                if (ignoredTaskId != null
                        && existing.getStudyTask() != null
                        && existing.getStudyTask()
                        .getId()
                        .equals(ignoredTaskId)) {

                    continue;
                }

                if (!date.equals(
                        existing.getOccurrenceDate())) {

                    continue;
                }

                LocalTime existingStart =
                        existing.getStartTime();

                LocalTime existingEnd =
                        existing.getEndTime();

                if (existingStart == null
                        || existingEnd == null) {

                    continue;
                }

                boolean overlaps =
                        task.getStartTime()
                                .isBefore(existingEnd)
                                &&
                        task.getEndTime()
                                .isAfter(existingStart);

                if (overlaps) {

                    throw new RuntimeException(
                            "Another task already exists at this time"
                    );
                }
            }
        }
    }


    // =====================================================
    // GET ALL OCCURRENCES OF USER
    // =====================================================

    private List<StudyTaskOccurrence> getUserOccurrences(
            String userEmail) {

        List<StudyTask> tasks =
                studyTaskRepository
                        .findByUserEmail(userEmail);

        List<StudyTaskOccurrence> allOccurrences =
                new ArrayList<>();

        if (tasks == null
                || tasks.isEmpty()) {

            return allOccurrences;
        }

        for (StudyTask task : tasks) {

            if (task == null
                    || task.getId() == null) {

                continue;
            }

            List<StudyTaskOccurrence> occurrences =
                    occurrenceRepository
                            .findByStudyTaskIdOrderByOccurrenceDateAsc(
                                    task.getId()
                            );

            if (occurrences != null) {

                allOccurrences.addAll(
                        occurrences
                );
            }
        }

        return allOccurrences;
    }


    // =====================================================
    // GENERATE DATES FOR OVERLAP VALIDATION
    // =====================================================

    private List<LocalDate>
    generateOccurrenceDatesForValidation(
            StudyTask task) {

        return generateOccurrenceDates(task);
    }


    // =====================================================
    // UPDATE ENTIRE STUDY TASK / SERIES
    // =====================================================

    public StudyTask updateTask(
            Long id,
            StudyTask updatedTask,
            String userEmail) {

        validateUserEmail(userEmail);

        if (updatedTask == null) {

            throw new RuntimeException(
                    "Updated task data cannot be empty."
            );
        }


        StudyTask existingTask =
                studyTaskRepository
                        .findByIdAndUserEmail(
                                id,
                                userEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found."
                                )
                        );


        validateBasicTaskDetails(
                updatedTask
        );

        validateReadingDate(
                updatedTask.getReadingDate()
        );

        validateTime(
                updatedTask.getStartTime(),
                updatedTask.getEndTime()
        );


        // -------------------------------------------------
        // CHECK EDIT LOCK
        // -------------------------------------------------

        checkParentEditLock(
                existingTask
        );


        // -------------------------------------------------
        // VALIDATE RECURRENCE
        // -------------------------------------------------

        validateRecurrence(
                updatedTask
        );


        // -------------------------------------------------
        // CHECK OVERLAP
        // -------------------------------------------------

        checkRecurringTaskOverlap(
                updatedTask,
                userEmail,
                existingTask.getId()
        );


        // -------------------------------------------------
        // PRESERVE ORIGINAL SCHEDULE
        // -------------------------------------------------

        if (existingTask.getOriginalReadingDate()
                == null) {

            existingTask.setOriginalReadingDate(
                    existingTask.getReadingDate()
            );
        }

        if (existingTask.getOriginalStartTime()
                == null) {

            existingTask.setOriginalStartTime(
                    existingTask.getStartTime()
            );
        }

        if (existingTask.getOriginalEndTime()
                == null) {

            existingTask.setOriginalEndTime(
                    existingTask.getEndTime()
            );
        }


        // -------------------------------------------------
        // UPDATE PARENT
        // -------------------------------------------------

        existingTask.setSubject(
                updatedTask.getSubject()
        );

        existingTask.setTopic(
                updatedTask.getTopic()
        );

        existingTask.setDescription(
                updatedTask.getDescription()
        );

        existingTask.setPriority(
                updatedTask.getPriority()
        );

        existingTask.setReadingDate(
                updatedTask.getReadingDate()
        );

        existingTask.setStartTime(
                updatedTask.getStartTime()
        );

        existingTask.setEndTime(
                updatedTask.getEndTime()
        );

        existingTask.setDeadline(
                updatedTask.getDeadline()
        );

        existingTask.setRecurrenceType(
                updatedTask.getRecurrenceType()
        );

        existingTask.setRecurrenceDays(
                updatedTask.getRecurrenceDays()
        );

        existingTask.setRecurrenceDayOfMonth(
                updatedTask.getRecurrenceDayOfMonth()
        );

        existingTask.setRecurrenceEndDate(
                updatedTask.getRecurrenceEndDate()
        );


        StudyTask savedTask =
                studyTaskRepository.save(
                        existingTask
                );


        // -------------------------------------------------
        // SYNCHRONIZE OCCURRENCES
        // -------------------------------------------------

        synchronizeOccurrences(
                savedTask
        );


        return savedTask;
    }


    // =====================================================
    // SYNCHRONIZE ENTIRE SERIES
    // =====================================================

    private void synchronizeOccurrences(
            StudyTask task) {

        if (task == null
                || task.getId() == null) {

            return;
        }


        List<LocalDate> expectedDates =
                generateOccurrenceDates(task);

        Set<LocalDate> expectedDateSet =
                new HashSet<>(
                        expectedDates
                );


        List<StudyTaskOccurrence> existingOccurrences =
                occurrenceRepository
                        .findByStudyTaskIdOrderByOccurrenceDateAsc(
                                task.getId()
                        );

        if (existingOccurrences == null) {

            existingOccurrences =
                    new ArrayList<>();
        }


        LocalDate today =
                LocalDate.now(INDIA_ZONE);


        // -------------------------------------------------
        // UPDATE EXISTING FUTURE OCCURRENCES
        // -------------------------------------------------

        for (StudyTaskOccurrence occurrence :
                existingOccurrences) {

            if (occurrence == null
                    || occurrence.getOccurrenceDate()
                    == null) {

                continue;
            }

            LocalDate occurrenceDate =
                    occurrence.getOccurrenceDate();


            if (occurrenceDate.isBefore(today)) {
                continue;
            }


            if ("COMPLETED".equalsIgnoreCase(
                    occurrence.getStatus())) {

                continue;
            }


            if (expectedDateSet.contains(
                    occurrenceDate)) {

                occurrence.setStartTime(
                        task.getStartTime()
                );

                occurrence.setEndTime(
                        task.getEndTime()
                );

                /*
                 * Parent-series edit means the occurrence
                 * has a new schedule, so its reminder can
                 * be sent again for the new schedule.
                 */
                occurrence.setReminderSent(false);

                occurrenceRepository.save(
                        occurrence
                );
            }
        }


        // -------------------------------------------------
        // CREATE MISSING OCCURRENCES
        // -------------------------------------------------

        for (LocalDate expectedDate :
                expectedDates) {

            createOccurrenceIfMissing(
                    task,
                    expectedDate
            );
        }
    }


    // =====================================================
    // CHECK PARENT EDIT LOCK
    // =====================================================

    private void checkParentEditLock(
            StudyTask existingTask) {

        if (existingTask.getOriginalReadingDate()
                == null
                || existingTask.getOriginalStartTime()
                == null) {

            return;
        }


        LocalDateTime originalStart =
                LocalDateTime.of(
                        existingTask
                                .getOriginalReadingDate(),

                        existingTask
                                .getOriginalStartTime()
                );


        LocalDateTime lockTime =
                originalStart.minusHours(1);


        LocalDateTime now =
                LocalDateTime.now(INDIA_ZONE);


        if (!now.isBefore(lockTime)) {

            throw new RuntimeException(
                    "Study timing is locked. "
                            + "You can edit the task only until "
                            + "1 hour before the original start time."
            );
        }
    }


    // =====================================================
    // GET ALL OCCURRENCES OF TASK
    // =====================================================

    public List<StudyTaskOccurrence> getOccurrences(
            Long taskId,
            String userEmail) {

        validateUserEmail(userEmail);

        StudyTask task =
                studyTaskRepository
                        .findByIdAndUserEmail(
                                taskId,
                                userEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found."
                                )
                        );

        return occurrenceRepository
                .findByStudyTaskIdOrderByOccurrenceDateAsc(
                        task.getId()
                );
    }


    // =====================================================
    // GET FUTURE OCCURRENCES
    // =====================================================

    public List<StudyTaskOccurrence>
    getFutureOccurrences(
            Long taskId,
            String userEmail) {

        List<StudyTaskOccurrence> occurrences =
                getOccurrences(
                        taskId,
                        userEmail
                );

        LocalDate today =
                LocalDate.now(INDIA_ZONE);

        List<StudyTaskOccurrence>
                futureOccurrences =
                new ArrayList<>();

        for (StudyTaskOccurrence occurrence :
                occurrences) {

            if (occurrence == null
                    || occurrence.getOccurrenceDate()
                    == null) {

                continue;
            }

            if (!occurrence.getOccurrenceDate()
                    .isBefore(today)) {

                futureOccurrences.add(
                        occurrence
                );
            }
        }

        return futureOccurrences;
    }


    // =====================================================
    // UPDATE SINGLE OCCURRENCE
    // =====================================================

    public StudyTaskOccurrence updateOccurrence(
            Long occurrenceId,
            LocalTime newStartTime,
            LocalTime newEndTime,
            String userEmail) {

        validateUserEmail(userEmail);

        if (occurrenceId == null) {

            throw new RuntimeException(
                    "Occurrence ID is required."
            );
        }

        validateTime(
                newStartTime,
                newEndTime
        );


        // -------------------------------------------------
        // FIND OCCURRENCE + USER OWNERSHIP
        // -------------------------------------------------

        StudyTaskOccurrence occurrence =
                occurrenceRepository
                        .findByIdAndStudyTaskUserEmail(
                                occurrenceId,
                                userEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Study occurrence not found."
                                )
                        );


        StudyTask parentTask =
                occurrence.getStudyTask();

        if (parentTask == null) {

            throw new RuntimeException(
                    "Parent study task not found."
            );
        }


        LocalDate occurrenceDate =
                occurrence.getOccurrenceDate();

        if (occurrenceDate == null) {

            throw new RuntimeException(
                    "Occurrence date is missing."
            );
        }


        // -------------------------------------------------
        // DO NOT EDIT PAST
        // -------------------------------------------------

        if (occurrenceDate.isBefore(
                LocalDate.now(INDIA_ZONE))) {

            throw new RuntimeException(
                    "You cannot edit a past study occurrence."
            );
        }


        // -------------------------------------------------
        // DO NOT EDIT COMPLETED
        // -------------------------------------------------

        if ("COMPLETED".equalsIgnoreCase(
                occurrence.getStatus())) {

            throw new RuntimeException(
                    "Completed study occurrences cannot be edited."
            );
        }


        LocalTime originalStartTime =
                occurrence.getStartTime();

        if (originalStartTime == null) {

            throw new RuntimeException(
                    "Original occurrence start time is missing."
            );
        }


        LocalDateTime occurrenceStart =
                LocalDateTime.of(
                        occurrenceDate,
                        originalStartTime
                );


        LocalDateTime editLockTime =
                occurrenceStart.minusHours(1);


        LocalDateTime now =
                LocalDateTime.now(INDIA_ZONE);


        // -------------------------------------------------
        // 1-HOUR EDIT LOCK
        // -------------------------------------------------

        if (!now.isBefore(editLockTime)) {

            throw new RuntimeException(
                    "Editing is locked. You can edit this "
                            + "study occurrence only until 1 hour "
                            + "before its original start time."
            );
        }


        // -------------------------------------------------
        // CHECK OVERLAP
        // -------------------------------------------------

        checkOccurrenceOverlap(
                occurrence,
                newStartTime,
                newEndTime,
                userEmail
        );


        // -------------------------------------------------
        // UPDATE ONLY THIS OCCURRENCE
        // -------------------------------------------------

        occurrence.setStartTime(
                newStartTime
        );

        occurrence.setEndTime(
                newEndTime
        );


        // -------------------------------------------------
        // RESET REMINDER
        // -------------------------------------------------

        occurrence.setReminderSent(
                false
        );


        return occurrenceRepository.save(
                occurrence
        );
    }


    // =====================================================
    // OCCURRENCE OVERLAP CHECK
    // =====================================================

    private void checkOccurrenceOverlap(
            StudyTaskOccurrence currentOccurrence,
            LocalTime newStartTime,
            LocalTime newEndTime,
            String userEmail) {

        LocalDate occurrenceDate =
                currentOccurrence.getOccurrenceDate();


        List<StudyTaskOccurrence>
                overlappingOccurrences =
                occurrenceRepository
                        .findByStudyTaskUserEmailAndOccurrenceDateAndStartTimeLessThanAndEndTimeGreaterThan(
                                userEmail,
                                occurrenceDate,
                                newEndTime,
                                newStartTime
                        );


        if (overlappingOccurrences == null
                || overlappingOccurrences.isEmpty()) {

            return;
        }


        Long currentId =
                currentOccurrence.getId();


        for (StudyTaskOccurrence other :
                overlappingOccurrences) {

            if (other == null) {
                continue;
            }

            if (currentId != null
                    && currentId.equals(
                    other.getId())) {

                continue;
            }


            throw new RuntimeException(
                    "Another task already exists at this time"
            );
        }
    }


    // =====================================================
    // COMPLETE SINGLE OCCURRENCE
    // =====================================================

    public StudyTaskOccurrence completeOccurrence(
            Long occurrenceId,
            String userEmail) {

        validateUserEmail(userEmail);

        if (occurrenceId == null) {

            throw new RuntimeException(
                    "Occurrence ID is required."
            );
        }


        StudyTaskOccurrence occurrence =
                occurrenceRepository
                        .findByIdAndStudyTaskUserEmail(
                                occurrenceId,
                                userEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Study occurrence not found."
                                )
                        );


        StudyTask parentTask =
                occurrence.getStudyTask();

        if (parentTask == null) {

            throw new RuntimeException(
                    "Parent study task not found."
            );
        }


        if (occurrence.getOccurrenceDate() == null) {

            throw new RuntimeException(
                    "Occurrence date is missing."
            );
        }


        if ("COMPLETED".equalsIgnoreCase(
                occurrence.getStatus())) {

            throw new RuntimeException(
                    "This study occurrence is already completed."
            );
        }


        // -------------------------------------------------
        // MARK ONLY THIS OCCURRENCE COMPLETED
        // -------------------------------------------------

        occurrence.setStatus(
                "COMPLETED"
        );


        return occurrenceRepository.save(
                occurrence
        );
    }


    // =====================================================
    // MARK ENTIRE STUDY TASK AS COMPLETED
    // =====================================================

    public StudyTask completeTask(
            Long id,
            String userEmail) {

        validateUserEmail(userEmail);


        StudyTask existingTask =
                studyTaskRepository
                        .findByIdAndUserEmail(
                                id,
                                userEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found."
                                )
                        );


        existingTask.setStatus(
                "COMPLETED"
        );


        List<StudyTaskOccurrence> occurrences =
                occurrenceRepository
                        .findByStudyTaskIdOrderByOccurrenceDateAsc(
                                existingTask.getId()
                        );


        if (occurrences != null) {

            for (StudyTaskOccurrence occurrence :
                    occurrences) {

                if (occurrence != null) {

                    occurrence.setStatus(
                            "COMPLETED"
                    );
                }
            }

            occurrenceRepository.saveAll(
                    occurrences
            );
        }


        return studyTaskRepository.save(
                existingTask
        );
    }


    // =====================================================
    // DELETE STUDY TASK
    // =====================================================

    public void deleteTask(
            Long id,
            String userEmail) {

        validateUserEmail(userEmail);


        StudyTask existingTask =
                studyTaskRepository
                        .findByIdAndUserEmail(
                                id,
                                userEmail
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found."
                                )
                        );


        // -------------------------------------------------
        // DELETE OCCURRENCES FIRST
        // -------------------------------------------------

        occurrenceRepository
                .deleteByStudyTaskId(
                        existingTask.getId()
                );


        // -------------------------------------------------
        // DELETE PARENT TASK
        // -------------------------------------------------

        studyTaskRepository.delete(
                existingTask
        );
    }
}