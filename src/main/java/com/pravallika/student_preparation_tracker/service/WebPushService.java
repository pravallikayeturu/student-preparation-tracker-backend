package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.PushSubscription;
import com.pravallika.student_preparation_tracker.repository.PushSubscriptionRepository;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class WebPushService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    private final String publicKey;
    private final String privateKey;
    private final String subject;

    public WebPushService(
            PushSubscriptionRepository pushSubscriptionRepository,
            @Value("${vapid.public.key}") String publicKey,
            @Value("${vapid.private.key}") String privateKey,
            @Value("${vapid.subject}") String subject) {

        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.subject = subject;
    }

    public void sendPushNotification(
            String userEmail,
            String title,
            String message) {

        if (userEmail == null
                || userEmail.trim().isEmpty()) {

            return;
        }

        List<PushSubscription> subscriptions =
                pushSubscriptionRepository.findAll()
                        .stream()
                        .filter(subscription ->
                                userEmail.equalsIgnoreCase(
                                        subscription.getUserEmail()))
                        .toList();

        if (subscriptions.isEmpty()) {

            System.out.println(
                    "No push subscription found for user: "
                            + userEmail
            );

            return;
        }

        for (PushSubscription subscription : subscriptions) {

            try {

                String payload =
                        "{"
                                + "\"title\":\""
                                + escapeJson(title)
                                + "\","
                                + "\"message\":\""
                                + escapeJson(message)
                                + "\""
                                + "}";

                Notification notification =
                        new Notification(
                                subscription.getEndpoint(),
                                subscription.getP256dh(),
                                subscription.getAuth(),
                                payload.getBytes(
                                        StandardCharsets.UTF_8)
                        );

                PushService pushService =
                        new PushService();

                pushService.setPublicKey(publicKey);
                pushService.setPrivateKey(privateKey);
                pushService.setSubject(subject);

                HttpResponse response =
                        pushService.send(notification);

                System.out.println(
                        "Push notification sent to: "
                                + userEmail
                                + " | HTTP status: "
                                + response.getStatusLine()
                );

            } catch (Exception e) {

                System.err.println(
                        "Failed to send push notification to: "
                                + userEmail
                );

                e.printStackTrace();
            }
        }
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}