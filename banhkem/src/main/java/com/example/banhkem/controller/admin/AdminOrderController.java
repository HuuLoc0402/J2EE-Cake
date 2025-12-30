package com.example.banhkem.controller.admin;

import com.example.banhkem.entity.OrderStatus;
import com.example.banhkem.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders") // Đường dẫn gốc cho quản lý đơn hàng
public class AdminOrderController {
    @Autowired private OrderService orderService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("statuses", OrderStatus.values());
        // Trỏ chính xác vào templates/admin/order.html
        return "admin/order";
    }

    // Đã chỉnh sửa path thành /updateStatus để khớp yêu cầu
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam Long orderId, @RequestParam String status) {
        orderService.updateStatus(orderId, OrderStatus.valueOf(status));
        return "redirect:/admin/orders";
    }
}