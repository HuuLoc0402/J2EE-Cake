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
                        // 1. Tài nguyên tĩnh và hệ thống
                        .requestMatchers("/favicon.ico", "/error", "/css/**", "/js/**", "/images/**").permitAll()

                        // 2. Trang chủ và Xác thực
                        .requestMatchers("/", "/auth/**", "/api/auth/**").permitAll()

                        // 3. MỞ KHÓA: Xem danh sách bánh và chi tiết bánh cho tất cả mọi người
                        .requestMatchers("/cake/**", "/contact").permitAll()

                        // 4. WebSocket Chat (Mở cho kết nối ban đầu)
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers("/admin/chats/history/current").authenticated()

                        // 5. Quản trị viên
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // 6. TẤT CẢ CÁC TRANG CÒN LẠI (bao gồm /cart): Phải đăng nhập
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}