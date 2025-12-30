package com.example.banhkem.controller.admin;

import com.example.banhkem.entity.Chat;
import com.example.banhkem.entity.Message;
import com.example.banhkem.entity.User;
import com.example.banhkem.repository.UserRepository;
import com.example.banhkem.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/chats")
public class AdminChatController {

    @Autowired private ChatService chatService;
    @Autowired private UserRepository userRepository;

    @GetMapping
    public String listChats(Model model) {
        model.addAttribute("openChats", chatService.getOpenChats());
        return "admin/chatlist";
    }

    // LẤY LỊCH SỬ THEO ID (Dùng cho Admin)
    @GetMapping("/history/{userId}")
    @ResponseBody
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getMessagesByUserId(userId));
    }

    @GetMapping("/history/current")
    @ResponseBody
    public ResponseEntity<List<Message>> getUserHistory(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // Trả về Unauthorized nếu chưa login
        }
        // Sử dụng orElseThrow để tránh lỗi NullPointerException khi không tìm thấy user
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(chatService.getMessagesByUserId(user.getId()));
    }
}