package com.example.banhkem.controller;

import com.example.banhkem.entity.Cake;
import com.example.banhkem.entity.Review;
import com.example.banhkem.entity.User;
import com.example.banhkem.service.CakeService;
import com.example.banhkem.service.CategoryService;
import com.example.banhkem.service.ReviewService;
import com.example.banhkem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cake")
public class CakeController {

    @Autowired
    private CakeService cakeService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    // 1. Hiển thị danh sách bánh kem (Có lọc theo danh mục)
    @GetMapping
    public String list(Model model,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Double minPrice,
                       @RequestParam(required = false) Double maxPrice,
                       @RequestParam(required = false) String sort) {

        List<Cake> cakes = cakeService.searchCakes(categoryId, keyword, minPrice, maxPrice, sort);
        model.addAttribute("cakes", cakes);
        model.addAttribute("categories", categoryService.getAllCategories());

        model.addAttribute("categoryId", categoryId);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "cake/list";
    }

    // 2. HIỂN THỊ CHI TIẾT SẢN PHẨM & ĐÁNH GIÁ
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Cake cake = cakeService.getCakeById(id);

        if (cake == null) {
            return "redirect:/cake";
        }

        // Lấy danh sách đánh giá của sản phẩm này
        List<Review> reviews = reviewService.getReviewsByCake(id);

        // Tính điểm trung bình sao
        Double avgRating = reviewService.getAverageRating(id);

        // Kiểm tra xem User hiện tại đã mua và có quyền đánh giá chưa
        boolean canReview = false;
        if (principal != null) {
            User currentUser = userService.findByUsername(principal.getName());
            canReview = reviewService.canUserReview(currentUser.getId(), id);
        }

        model.addAttribute("cake", cake);
        model.addAttribute("reviews", reviews);
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("canReview", canReview);

        return "cake/detail";
    }
}