package com.example.banhkem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Cấu hình cho ảnh sản phẩm (Cũ của bạn)
        Path imageDir = Paths.get("src/main/resources/static/images/");
        String imagePath = imageDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/" + imagePath + "/");

        // 2. Cấu hình cho ảnh Đánh giá (Mới - Để hiển thị ảnh review khách upload)
        Path uploadDir = Paths.get("src/main/resources/static/uploads/");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
}