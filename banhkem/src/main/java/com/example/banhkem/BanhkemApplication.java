package com.example.banhkem;

import com.example.banhkem.entity.Role;
import com.example.banhkem.entity.User;
import com.example.banhkem.repository.RoleRepository;
import com.example.banhkem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@EnableScheduling
public class BanhkemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BanhkemApplication.class, args);
    }

    @Bean
    @Transactional // Thêm Transactional để các Role tìm được luôn ở trạng thái Managed
    public CommandLineRunner initialData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Khởi tạo hoặc lấy Role từ DB
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("ROLE_ADMIN");
                        return roleRepository.save(r);
                    });

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("ROLE_USER");
                        return roleRepository.save(r);
                    });

            // 2. Tạo Admin User nếu chưa có
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setEmail("admin@banhkem.com");
                admin.setFullName("Admin Banh Kem");

                // Gán Role (Lúc này adminRole đã chắc chắn được Hibernate quản lý)
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                admin.setRoles(roles);

                userRepository.save(admin);
                System.out.println(">>> Đã khởi tạo thành công tài khoản Admin (123456)");
            }
        };
    }
}