package com.pravallika.student_preparation_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class BrevoEmailService {

    private final RestClient restClient;
    private final String senderEmail;
    private final String senderName;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public BrevoEmailService(
            @Value("${brevo.api.key}") String apiKey,
            @Value("${brevo.sender.email}") String senderEmail,
            @Value("${brevo.sender.name}") String senderName) {

        this.senderEmail = senderEmail;
        this.senderName = senderName;

        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .defaultHeader(
                        "api-key",
                        apiKey
                )
                .build();
    }

    // =====================================================
    // SEND SIGNUP OTP EMAIL
    // =====================================================

    public void sendSignupOtpEmail(
            String userEmail,
            String otpCode) {

        String htmlContent =
                "<div style='font-family: Arial, sans-serif;'>"
                        + "<h2>📚 Student Preparation Tracker</h2>"
                        + "<p>Hello!</p>"
                        + "<p>"
                        + "Use the following OTP to complete "
                        + "your registration:"
                        + "</p>"
                        + "<h1 style='letter-spacing: 6px;'>"
                        + otpCode
                        + "</h1>"
                        + "<p>"
                        + "This OTP is valid for "
                        + "<strong>5 minutes</strong>."
                        + "</p>"
                        + "<p>"
                        + "If you did not request this OTP, "
                        + "please ignore this email."
                        + "</p>"
                        + "<p>"
                        + "Good luck with your preparation! 💪"
                        + "</p>"
                        + "</div>";

        sendEmail(
                userEmail,
                "Your Student Preparation Tracker Signup OTP",
                htmlContent,
                "Signup OTP"
        );
    }

    // =====================================================
    // SEND LOGIN OTP EMAIL
    // =====================================================

    public void sendLoginOtpEmail(
            String userEmail,
            String otpCode) {

        String htmlContent =
                "<div style='font-family: Arial, sans-serif;'>"
                        + "<h2>🔐 Student Preparation Tracker</h2>"
                        + "<p>Hello!</p>"
                        + "<p>"
                        + "Use the following OTP to complete "
                        + "your login:"
                        + "</p>"
                        + "<h1 style='letter-spacing: 6px;'>"
                        + otpCode
                        + "</h1>"
                        + "<p>"
                        + "This OTP is valid for "
                        + "<strong>5 minutes</strong>."
                        + "</p>"
                        + "<p>"
                        + "If you did not request this login OTP, "
                        + "please ignore this email."
                        + "</p>"
                        + "<p>"
                        + "Good luck with your preparation! 💪"
                        + "</p>"
                        + "</div>";

        sendEmail(
                userEmail,
                "Your Student Preparation Tracker Login OTP",
                htmlContent,
                "Login OTP"
        );
    }

    // =====================================================
    // SEND FORGOT PASSWORD OTP EMAIL
    // =====================================================

    public void sendForgotPasswordOtpEmail(
            String userEmail,
            String otpCode) {

        String htmlContent =
                "<div style='font-family: Arial, sans-serif;'>"
                        + "<h2>🔐 Student Preparation Tracker</h2>"
                        + "<p>Hello!</p>"
                        + "<p>"
                        + "Use the following OTP to reset your password:"
                        + "</p>"
                        + "<h1 style='letter-spacing: 6px;'>"
                        + otpCode
                        + "</h1>"
                        + "<p>"
                        + "This OTP is valid for "
                        + "<strong>5 minutes</strong>."
                        + "</p>"
                        + "<p>"
                        + "If you did not request a password reset, "
                        + "please ignore this email."
                        + "</p>"
                        + "<p>"
                        + "Good luck with your preparation! 💪"
                        + "</p>"
                        + "</div>";

        sendEmail(
                userEmail,
                "Password Reset OTP - Student Preparation Tracker",
                htmlContent,
                "Forgot Password OTP"
        );
    }

    // =====================================================
    // COMMON EMAIL SENDER
    // =====================================================

    private void sendEmail(
            String userEmail,
            String subject,
            String htmlContent,
            String emailType) {

        try {

            Map<String, Object> requestBody =
                    Map.of(
                            "sender",
                            Map.of(
                                    "name",
                                    senderName,
                                    "email",
                                    senderEmail
                            ),
                            "to",
                            List.of(
                                    Map.of(
                                            "email",
                                            userEmail
                                    )
                            ),
                            "subject",
                            subject,
                            "htmlContent",
                            htmlContent
                    );

            Map<?, ?> response =
                    restClient.post()
                            .uri("/smtp/email")
                            .body(requestBody)
                            .retrieve()
                            .body(Map.class);

            System.out.println(
                    emailType
                            + " email sent successfully to: "
                            + userEmail
            );

            if (response != null) {

                System.out.println(
                        "Brevo response: "
                                + response
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Failed to send "
                            + emailType
                            + " email to: "
                            + userEmail
            );

            e.printStackTrace();

            throw new RuntimeException(
                    "Failed to send email",
                    e
            );
        }
    }

    // =====================================================
    // SEND STUDY REMINDER
    // =====================================================

    public void sendStudyReminder(
            String userEmail,
            String subject,
            String topic,
            LocalDate readingDate,
            LocalTime startTime,
            LocalTime endTime) {

        String htmlContent =
                "<div style='font-family: Arial, sans-serif;'>"
                        + "<h2>📚 Study Reminder</h2>"
                        + "<p>Hello!</p>"
                        + "<p>"
                        + "Your study session starts in "
                        + "<strong>10 minutes</strong>."
                        + "</p>"
                        + "<p><strong>Subject:</strong> "
                        + subject
                        + "</p>"
                        + "<p><strong>Topic:</strong> "
                        + topic
                        + "</p>"
                        + "<p><strong>Date:</strong> "
                        + readingDate
                        + "</p>"
                        + "<p><strong>Start Time:</strong> "
                        + startTime
                        + "</p>"
                        + "<p><strong>End Time:</strong> "
                        + endTime
                        + "</p>"
                        + "<p>"
                        + "Good luck with your preparation! 💪"
                        + "</p>"
                        + "</div>";

        sendEmail(
                userEmail,
                "⏰ Study Reminder - Your session starts in 10 minutes",
                htmlContent,
                "Study Reminder"
        );
    }
}