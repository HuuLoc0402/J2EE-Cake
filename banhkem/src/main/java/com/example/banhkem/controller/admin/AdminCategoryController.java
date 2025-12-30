package com.example.banhkem.controller.admin;

import com.example.banhkem.entity.Category;
import com.example.banhkem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/category")
public class AdminCategoryController {
    @Autowired private CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        return "admin/category";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Category category, RedirectAttributes ra) {
        categoryService.saveCategory(category);
        ra.addFlashAttribute("message", "Cập nhật danh mục thành công!");
        return "redirect:/admin/category";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public Category getEdit(@PathVariable Long id) {
        Category cat = categoryService.getCategoryById(id);
        if (cat != null) {
            // Cắt đứt vòng lặp vô hạn JSON cho Java 25
            cat.setCakes(null);
        }
        return cat;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteCategory(id);
            ra.addFlashAttribute("message", "Xóa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Danh mục đang có sản phẩm, không thể xóa!");
        }
        return "redirect:/admin/category";
    }
}