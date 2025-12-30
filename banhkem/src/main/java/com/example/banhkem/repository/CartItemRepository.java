package com.example.banhkem.repository;

import com.example.banhkem.entity.CartItem;
import com.example.banhkem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndCakeId(User user, Long cakeId);
    void deleteByUser(User user);
}