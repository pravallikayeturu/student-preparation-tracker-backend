package com.pravallika.student_preparation_tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    // ==============================
    // SETTINGS
    // ==============================

    @Column(name = "dark_mode", nullable = false)
    private Boolean darkMode = false;

    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications = true;

    @Column(name = "task_reminders", nullable = false)
    private Boolean taskReminders = true;


    // ==============================
    // CONSTRUCTOR
    // ==============================

    public User() {
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
    // PASSWORD
    // ==============================

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    // ==============================
    // DARK MODE
    // ==============================

    public Boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(Boolean darkMode) {
        this.darkMode = darkMode;
    }


    // ==============================
    // EMAIL NOTIFICATIONS
    // ==============================

    public Boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(Boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }


    // ==============================
    // TASK REMINDERS
    // ==============================

    public Boolean isTaskReminders() {
        return taskReminders;
    }

    public void setTaskReminders(Boolean taskReminders) {
        this.taskReminders = taskReminders;
    }
}