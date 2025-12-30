package com.example.banhkem.service;

import com.example.banhkem.entity.CartItem;
import com.example.banhkem.entity.User;
import com.example.banhkem.entity.Cake;
import com.example.banhkem.repository.CartItemRepository;
import com.example.banhkem.repository.CakeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CartService {
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CakeRepository cakeRepository;

    public List<CartItem> getCartByUser(User user) {
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public void addToCart(User user, Long cakeId, Integer quantity) {
        if (quantity < 1) return;

        Cake cake = cakeRepository.findById(cakeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bánh"));

        CartItem item = cartItemRepository.findByUserAndCakeId(user, cakeId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUser(user);
                    newItem.setCake(cake);
                    newItem.setQuantity(0);
                    return newItem;
                });

        int newQuantity = item.getQuantity() + quantity;

        // KIỂM TRA TỒN KHO: Không cho phép vượt quá stockQuantity
        if (newQuantity > cake.getStockQuantity()) {
            newQuantity = cake.getStockQuantity();
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void updateQuantity(Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục trong giỏ"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }

        // KIỂM TRA TỒN KHO KHI CẬP NHẬT
        int stock = item.getCake().getStockQuantity();
        if (quantity > stock) {
            quantity = stock;
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long itemId) {
        cartItemRepository.deleteById(itemId);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}