package com.example.banhkem.controller;

import com.example.banhkem.entity.*;
import com.example.banhkem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired private OrderService orderService;
    @Autowired private UserService userService;
    @Autowired private CartService cartService; // Cần bổ sung để lấy dữ liệu giỏ hàng

    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        List<CartItem> cartItems = cartService.getCartByUser(user);

        if (cartItems.isEmpty()) return "redirect:/cart"; // Giỏ trống thì không cho checkout

        double total = cartItems.stream()
                .mapToDouble(i -> i.getCake().getPrice() * i.getQuantity()).sum();

        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", total);
        model.addAttribute("order", new Order());

        return "order/checkout";
    }
    @GetMapping("/history")
    public String history(Model model, Authentication auth) {
        User user = userService.findByUsername(auth.getName());
        model.addAttribute("orders", orderService.getOrdersByUser(user));
        return "order/history"; // Khớp với templates/order/history.html
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return "redirect:/order/history";
        }
        model.addAttribute("order", order);
        return "order/detail"; // Trỏ đến file templates/order/detail.html
    }
    @PostMapping("/create")
    public String createOrder(@ModelAttribute Order order,
                              @RequestParam String phone,
                              @RequestParam String shippingAddress,
                              Authentication auth) {
        User user = userService.findByUsername(auth.getName());

        // Gán thông tin từ form vào object order
        order.setUser(user);
        order.setPhone(phone);
        order.setShippingAddress(shippingAddress);

        // Logic tạo order kèm theo item từ giỏ hàng sẽ được xử lý trong OrderService
        Order savedOrder = orderService.createOrderFromCart(user, order);

        // Chuyển hướng sang VNPay
        return "redirect:/payment/create?orderId=" + savedOrder.getId() + "&amount=" + savedOrder.getTotalAmount();
    }
}