package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.PushSubscription;
import com.pravallika.student_preparation_tracker.repository.PushSubscriptionRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    public PushSubscriptionService(
            PushSubscriptionRepository pushSubscriptionRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
    }

    // Save a new push subscription
    public PushSubscription saveSubscription(PushSubscription subscription) {
        return pushSubscriptionRepository.save(subscription);
    }

    // Get all push subscriptions
    public List<PushSubscription> getAllSubscriptions() {
        return pushSubscriptionRepository.findAll();
    }

    // Get subscription by ID
    public Optional<PushSubscription> getSubscriptionById(Long id) {
        return pushSubscriptionRepository.findById(id);
    }

    // Delete subscription by ID
    public void deleteSubscription(Long id) {
        pushSubscriptionRepository.deleteById(id);
    }
}