package com.example.banhkem.controller;

import com.example.banhkem.entity.Message;
import com.example.banhkem.entity.User;
import com.example.banhkem.service.ChatService;
import com.example.banhkem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class ChatWebSocketController {

    @Autowired private ChatService chatService;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private UserRepository userRepository;

    @MessageMapping("/chat.send")
    public void processMessage(@Payload Message message, Principal principal) {
        if (principal == null || message == null || message.getContent() == null) return;

        try {
            // LUỒNG ADMIN PHẢN HỒI CHO KHÁCH HÀNG
            if ("ROLE_ADMIN".equals(message.getSenderRole())) {
                if (message.getChat() == null || message.getChat().getUser() == null || message.getChat().getUser().getId() == null) {
                    System.err.println("LỖI: Admin gửi tin thiếu ID khách hàng!");
                    return;
                }

                Long targetUserId = message.getChat().getUser().getId();
                User targetUser = userRepository.findById(targetUserId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + targetUserId));

                // Lưu Database
                Message savedMsg = chatService.saveMessage(targetUserId, message.getContent(), "ROLE_ADMIN");

                // Gửi Real-time tới đúng hàng đợi của User
                messagingTemplate.convertAndSendToUser(targetUser.getUsername(), "/queue/messages", savedMsg);
                System.out.println("ADMIN SUCCESS: Đã gửi tới " + targetUser.getUsername());
            }
            // LUỒNG USER GỬI TIN CHO ADMIN
            else {
                User currentUser = userRepository.findByUsername(principal.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Lưu Database
                Message savedMsg = chatService.saveMessage(currentUser.getId(), message.getContent(), "ROLE_USER");

                // Gửi tới Topic Admin để Admin nhận tin trực tiếp ngay lập tức
                messagingTemplate.convertAndSend("/topic/admin/chats", savedMsg);
                System.out.println("USER SUCCESS: Đã gửi tới Admin từ " + principal.getName());
            }
        } catch (Exception e) {
            System.err.println("LỖI HỆ THỐNG: " + e.getMessage());
            e.printStackTrace();
        }
    }
}