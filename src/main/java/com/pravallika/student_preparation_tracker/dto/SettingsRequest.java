package com.pravallika.student_preparation_tracker.dto;

public class SettingsRequest {

    private String name;
    private String email;
    private boolean darkMode;
    private boolean emailNotifications;
    private boolean taskReminders;


    public SettingsRequest() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }


    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }


    public boolean isTaskReminders() {
        return taskReminders;
    }

    public void setTaskReminders(boolean taskReminders) {
        this.taskReminders = taskReminders;
    }
}