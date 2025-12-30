package com.example.banhkem.controller;

import com.example.banhkem.entity.CartItem;
import com.example.banhkem.entity.User;
import com.example.banhkem.service.CartService;
import com.example.banhkem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    // Hiển thị giỏ hàng
    @GetMapping
    public String viewCart(Model model, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        List<CartItem> cartItems = cartService.getCartByUser(user);

        // Tính tổng tiền trực tiếp để hiển thị
        double total = cartItems.stream()
                .mapToDouble(i -> i.getCake().getPrice() * i.getQuantity())
                .sum();

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", total);

        // Trỏ đúng vào file templates/cart/index.html
        return "cart/index";
    }

    // Thêm sản phẩm vào giỏ hàng (Xử lý từ trang detail hoặc trang list)
    @PostMapping("/add")
    public String add(@RequestParam Long cakeId, @RequestParam(defaultValue = "1") Integer quantity, Principal principal) {
        if (principal == null) return "redirect:/auth/login";

        User user = userService.findByUsername(principal.getName());
        cartService.addToCart(user, cakeId, quantity);
        return "redirect:/cart";
    }

    // Cập nhật số lượng trong giỏ hàng (Gọi khi thay đổi input số lượng)
    @PostMapping("/update")
    public String update(@RequestParam Long itemId, @RequestParam Integer quantity) {
        if (quantity > 0) {
            cartService.updateQuantity(itemId, quantity);
        } else {
            cartService.removeFromCart(itemId);
        }
        return "redirect:/cart";
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @GetMapping("/remove/{id}")
    public String remove(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return "redirect:/cart";
    }
}