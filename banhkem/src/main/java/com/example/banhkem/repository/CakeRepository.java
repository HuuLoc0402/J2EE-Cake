package com.example.banhkem.repository;

import com.example.banhkem.entity.Cake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CakeRepository extends JpaRepository<Cake, Long> {
    List<Cake> findByCategoryId(Long categoryId);
    List<Cake> findByNameContainingIgnoreCase(String name);
    @Query("SELECT c FROM Cake c WHERE " +
            "(:categoryId IS NULL OR c.category.id = :categoryId) AND " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:minPrice IS NULL OR c.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR c.price <= :maxPrice) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'priceAsc' THEN c.price END ASC, " +
            "CASE WHEN :sort = 'priceDesc' THEN c.price END DESC, " +
            "CASE WHEN :sort = 'nameAsc' THEN c.name END ASC, " +
            "CASE WHEN :sort = 'nameDesc' THEN c.name END DESC, " +
            "c.id DESC")
    List<Cake> findByFilters(@Param("categoryId") Long categoryId,
                             @Param("keyword") String keyword,
                             @Param("minPrice") Double minPrice,
                             @Param("maxPrice") Double maxPrice,
                             @Param("sort") String sort);
}