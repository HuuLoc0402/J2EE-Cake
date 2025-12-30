package com.example.banhkem.service;

import com.example.banhkem.entity.Role;
import com.example.banhkem.entity.User;
import com.example.banhkem.repository.RoleRepository;
import com.example.banhkem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Transactional
    public String register(User user) { // Đổi từ void sang String
        // Kiểm tra trùng lặp dựa trên Repository đã thống nhất
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Tên đăng nhập đã tồn tại!";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email đã được sử dụng!";
        }

        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Gán quyền mặc định (ROLE_USER)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
        return "SUCCESS"; // Trả về SUCCESS để Controller nhận biết thành công
    }
}