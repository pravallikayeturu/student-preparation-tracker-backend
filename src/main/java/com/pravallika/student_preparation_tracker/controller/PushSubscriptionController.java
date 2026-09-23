package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.entity.PushSubscription;
import com.pravallika.student_preparation_tracker.service.PushSubscriptionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/push-subscriptions")
@CrossOrigin(origins = "*")
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;

    public PushSubscriptionController(
            PushSubscriptionService pushSubscriptionService) {
        this.pushSubscriptionService = pushSubscriptionService;
    }

    // Save push subscription
    @PostMapping
    public ResponseEntity<PushSubscription> saveSubscription(
            @RequestBody PushSubscription subscription) {

        PushSubscription savedSubscription =
                pushSubscriptionService.saveSubscription(subscription);

        return ResponseEntity.ok(savedSubscription);
    }

    // Get all push subscriptions
    @GetMapping
    public ResponseEntity<List<PushSubscription>> getAllSubscriptions() {

        return ResponseEntity.ok(
                pushSubscriptionService.getAllSubscriptions()
        );
    }

    // Get subscription by ID
    @GetMapping("/{id}")
    public ResponseEntity<PushSubscription> getSubscriptionById(
            @PathVariable Long id) {

        return pushSubscriptionService.getSubscriptionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete subscription
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(
            @PathVariable Long id) {

        pushSubscriptionService.deleteSubscription(id);

        return ResponseEntity.noContent().build();
    }
}