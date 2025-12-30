package com.example.banhkem.controller;

import com.example.banhkem.entity.Cake;
import com.example.banhkem.service.CakeService;
import com.example.banhkem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cake")
public class CakeController {

    @Autowired
    private CakeService cakeService;

    @Autowired
    private CategoryService categoryService;

    // 1. Hiển thị danh sách bánh kem (Có lọc theo danh mục)
    @GetMapping
    public String list(Model model, @RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            model.addAttribute("cakes", cakeService.getCakesByCategory(categoryId));
        } else {
            model.addAttribute("cakes", cakeService.getAllCakes());
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        return "cake/list";
    }

    // 2. HIỂN THỊ CHI TIẾT SẢN PHẨM (Mapping cho file detail.html)
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Cake cake = cakeService.getCakeById(id);

        // Nếu không tìm thấy bánh, quay lại danh sách tránh lỗi
        if (cake == null) {
            return "redirect:/cake";
        }

        model.addAttribute("cake", cake);
        // Trả về file: src/main/resources/templates/cake/detail.html
        return "cake/detail";
    }
}