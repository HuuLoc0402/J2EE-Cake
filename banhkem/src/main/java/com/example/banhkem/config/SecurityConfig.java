package com.example.banhkem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. MỞ KHÓA TUYỆT ĐỐI các tài nguyên này để không bị 403 hệ thống
                        .requestMatchers("/favicon.ico", "/error", "/css/**", "/js/**", "/images/**").permitAll()

                        // 2. Các trang view và api đăng ký/đăng nhập
                        .requestMatchers("/", "/auth/**", "/api/auth/**").permitAll()

                        // --- PHẦN THÊM MỚI CHO CHAT (KHÔNG SỬA CODE CŨ) ---
                        // Cho phép User lấy lịch sử của chính họ (Phải đặt trước dòng /admin/**)
                        .requestMatchers("/admin/chats/history/current").authenticated()
                        // Cho phép kết nối WebSocket
                        .requestMatchers("/ws-chat/**").permitAll()
                        // ------------------------------------------------

                        // 3. Bảo vệ Admin - Khớp chính xác ROLE_ADMIN trong Database của bạn
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}