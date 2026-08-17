package com.pravallika.student_preparation_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ResendEmailService {

    private final Resend resend;

    public ResendEmailService(
            @Value("${resend.api.key}") String apiKey) {

        this.resend = new Resend(apiKey);
    }

    public void sendStudyReminder(
            String userEmail,
            String subject,
            String topic,
            LocalDate readingDate,
            LocalTime startTime,
            LocalTime endTime) {

        CreateEmailOptions email =
                CreateEmailOptions.builder()
                        .from("Student Preparation Tracker <onboarding@resend.dev>")
                        .to(userEmail)
                        .subject(
                                "⏰ Study Reminder - "
                                        + "Your session starts in 10 minutes"
                        )
                        .html(
                                "<h2>📚 Study Reminder</h2>"
                                        + "<p>Hello!</p>"
                                        + "<p>Your study session starts in "
                                        + "<strong>10 minutes</strong>.</p>"

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

                                        + "<p>Good luck with your preparation! 💪</p>"
                        )
                        .build();

        try {

            resend.emails().send(email);

            System.out.println(
                    "Study reminder email sent successfully to: "
                            + userEmail
            );

        } catch (Exception e) {

            System.out.println(
                    "Failed to send study reminder to "
                            + userEmail
                            + ": "
                            + e.getMessage()
            );
                e.printStackTrace();

            throw new RuntimeException("Failed to send study reminder email",e);
        }
    }
}
