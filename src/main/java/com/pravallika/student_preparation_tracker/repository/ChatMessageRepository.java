package com.pravallika.student_preparation_tracker.repository;

import com.pravallika.student_preparation_tracker.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserEmailOrderByCreatedAtAsc(String userEmail);
}