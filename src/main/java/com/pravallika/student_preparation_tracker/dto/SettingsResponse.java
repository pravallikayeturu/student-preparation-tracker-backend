package com.pravallika.student_preparation_tracker.dto;

public class SettingsResponse {

    private Long id;
    private String name;
    private String email;
    private boolean darkMode;
    private boolean emailNotifications;
    private boolean taskReminders;


    public SettingsResponse(
            Long id,
            String name,
            String email,
            boolean darkMode,
            boolean emailNotifications,
            boolean taskReminders) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.darkMode = darkMode;
        this.emailNotifications = emailNotifications;
        this.taskReminders = taskReminders;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public boolean isTaskReminders() {
        return taskReminders;
    }
}