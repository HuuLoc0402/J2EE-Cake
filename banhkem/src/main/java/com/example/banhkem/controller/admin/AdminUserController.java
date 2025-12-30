package com.example.banhkem.controller.admin;

import com.example.banhkem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users") // Luôn dùng số nhiều để nhất quán với Sidebar
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String list(Model model) {
        // Gửi danh sách người dùng vào Model
        model.addAttribute("users", userService.getAllUsers());
        // Trả về file HTML tại: templates/admin/user.html
        return "admin/user";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}