package com.pravallika.student_preparation_tracker.repository;

import com.pravallika.student_preparation_tracker.entity.PushSubscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PushSubscriptionRepository
        extends JpaRepository<PushSubscription, Long> {
}