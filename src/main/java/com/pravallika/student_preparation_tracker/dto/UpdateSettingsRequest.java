package com.pravallika.student_preparation_tracker.dto;

public class UpdateSettingsRequest {

    private String name;
    private Boolean darkMode;
    private Boolean emailNotifications;
    private Boolean taskReminders;

    public UpdateSettingsRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(Boolean darkMode) {
        this.darkMode = darkMode;
    }

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public Boolean getTaskReminders() {
        return taskReminders;
    }

    public void setTaskReminders(Boolean taskReminders) {
        this.taskReminders = taskReminders;
    }
}