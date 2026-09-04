
package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.entity.ChatMessage;
import com.pravallika.student_preparation_tracker.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    // =========================================
    // SAVE CHAT MESSAGE
    // =========================================
    public ChatMessage saveChat(
            String userEmail,
            String question,
            String answer) {

        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setUserEmail(userEmail);
        chatMessage.setQuestion(question);
        chatMessage.setAnswer(answer);
        chatMessage.setCreatedAt(LocalDateTime.now());

        return chatMessageRepository.save(chatMessage);
    }

    // =========================================
    // GET USER CHAT HISTORY
    // =========================================
    public List<ChatMessage> getChatHistory(String userEmail) {

        return chatMessageRepository
                .findByUserEmailOrderByCreatedAtAsc(userEmail);
    }
}
