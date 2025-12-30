package com.example.banhkem.controller.admin;

import com.example.banhkem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.Year;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired private OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(required = false) Integer year) {
        int targetYear = (year == null) ? Year.now().getValue() : year;

        // Dữ liệu thống kê
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("totalCakesSold", orderService.getTotalCakesSold());
        model.addAttribute("currentYear", targetYear);

        // Dữ liệu biểu đồ (Đã được xử lý an toàn tại Service)
        model.addAttribute("revenueData", orderService.getMonthlyRevenueData(targetYear));
        model.addAttribute("cakeCountData", orderService.getMonthlyCakeCountData(targetYear));

        return "admin/dashboard";
    }
}