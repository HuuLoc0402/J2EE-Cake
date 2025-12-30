package com.example.banhkem.controller;

import com.example.banhkem.service.CakeService;
import com.example.banhkem.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @Autowired private CakeService cakeService;
    @Autowired private CategoryService categoryService;

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("featuredCakes", cakeService.getAllCakes().stream().limit(8).toList());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "home/index";
    }
}