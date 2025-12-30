package com.example.banhkem.repository;

import com.example.banhkem.entity.Cake;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CakeRepository extends JpaRepository<Cake, Long> {
    List<Cake> findByCategoryId(Long categoryId);
    List<Cake> findByNameContainingIgnoreCase(String name);
}