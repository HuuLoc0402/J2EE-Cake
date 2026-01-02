package com.example.banhkem.controller;

import com.example.banhkem.entity.User;
import com.example.banhkem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
public class ProfileController {

    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;

    // Hiển thị trang Profile
    @GetMapping("/profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "auth/profile";
    }

    // API Cập nhật thông tin cá nhân
    @PutMapping("/api/auth/profile/update")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@RequestBody User updatedData, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        currentUser.setFullName(updatedData.getFullName());
        currentUser.setEmail(updatedData.getEmail());
        currentUser.setPhone(updatedData.getPhone());
        currentUser.setAddress(updatedData.getAddress());
        userService.save(currentUser); // Bạn cần đảm bảo hàm save này thực hiện update
        return ResponseEntity.ok().build();
    }

    // API Đổi mật khẩu
    @PostMapping("/api/auth/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        // Kiểm tra mật khẩu cũ có khớp với DB không
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu hiện tại không đúng!"));
        }

        // Mã hóa mật khẩu mới và lưu
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userService.save(currentUser);
        return ResponseEntity.ok().build();
    }
}