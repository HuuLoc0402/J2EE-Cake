package com.example.banhkem.service;

import com.example.banhkem.entity.*;
import com.example.banhkem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    @Autowired private ChatRepository chatRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;

    public List<Chat> getOpenChats() {
        return chatRepository.findByClosedFalseOrderByLastUpdatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Message> getMessagesByUserId(Long userId) {
        Chat chat = getActiveChatByUserId(userId);
        if (chat.getMessages() != null) chat.getMessages().size();
        return chat.getMessages();
    }

    @Transactional
    public Message saveMessage(Long userId, String content, String senderRole) {
        // 1. Tìm phiên chat của khách hàng theo ID
        Chat chat = getActiveChatByUserId(userId);

        // 2. Tạo đối tượng tin nhắn
        Message msg = new Message();
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        msg.setSenderRole(senderRole);
        msg.setChat(chat);

        chat.setLastUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        Message saved = messageRepository.save(msg);

        // 3. Ép nạp dữ liệu User cho WebSocket
        if (saved.getChat() != null && saved.getChat().getUser() != null) {
            saved.getChat().getUser().getUsername();
        }
        return saved;
    }

    @Transactional
    public Chat getActiveChatByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        return chatRepository.findByUserId(userId).stream()
                .filter(c -> !c.isClosed())
                .findFirst()
                .orElseGet(() -> {
                    Chat newChat = new Chat();
                    newChat.setUser(user);
                    newChat.setClosed(false);
                    newChat.setLastUpdatedAt(LocalDateTime.now());
                    return chatRepository.save(newChat);
                });
    }
}