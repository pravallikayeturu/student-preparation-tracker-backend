package com.pravallika.student_preparation_tracker.dto;

public class AuthResponse {

    private Long id;
    private String name;
    private String email;
    private String message;
    private String token;

    // ==============================
    // SETTINGS
    // ==============================

    private Boolean darkMode;
    private Boolean emailNotifications;
    private Boolean taskReminders;


    // ==============================
    // DEFAULT CONSTRUCTOR
    // ==============================

    public AuthResponse() {
    }


    // ==============================
    // CONSTRUCTOR FOR SIGNUP / RESET PASSWORD
    // ==============================

    public AuthResponse(
            Long id,
            String name,
            String email,
            String message) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
    }


    // ==============================
    // CONSTRUCTOR FOR LOGIN WITH JWT
    // ==============================

    public AuthResponse(
            Long id,
            String name,
            String email,
            String message,
            String token) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
        this.token = token;
    }


    // ==============================
    // CONSTRUCTOR WITH SETTINGS
    // ==============================

    public AuthResponse(
            Long id,
            String name,
            String email,
            String message,
            Boolean darkMode,
            Boolean emailNotifications,
            Boolean taskReminders) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
        this.darkMode = darkMode;
        this.emailNotifications = emailNotifications;
        this.taskReminders = taskReminders;
    }


    // ==============================
    // ID
    // ==============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // ==============================
    // NAME
    // ==============================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    // ==============================
    // EMAIL
    // ==============================

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    // ==============================
    // MESSAGE
    // ==============================

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    // ==============================
    // TOKEN
    // ==============================

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    // ==============================
    // DARK MODE
    // ==============================

    public Boolean getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(Boolean darkMode) {
        this.darkMode = darkMode;
    }


    // ==============================
    // EMAIL NOTIFICATIONS
    // ==============================

    public Boolean getEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }


    // ==============================
    // TASK REMINDERS
    // ==============================

    public Boolean getTaskReminders() {
        return taskReminders;
    }

    public void setTaskReminders(Boolean taskReminders) {
        this.taskReminders = taskReminders;
    }
}