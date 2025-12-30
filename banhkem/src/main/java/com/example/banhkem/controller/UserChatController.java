package com.example.banhkem.controller;

import com.example.banhkem.entity.User;
import com.example.banhkem.repository.UserRepository;
import com.example.banhkem.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class UserChatController {

    @Autowired private ChatService chatService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/chat")
    public String showChatPage(Principal principal, Model model) {
        if (principal == null) return "redirect:/auth/login";

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Truyền lịch sử tin nhắn ra giao diện
        model.addAttribute("messages", chatService.getMessagesByUserId(user.getId()));

        // Trỏ đúng tới src/main/resources/templates/chat/chat.html
        return "chat/chat";
    }
}