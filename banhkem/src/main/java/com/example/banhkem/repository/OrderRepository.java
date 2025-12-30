package com.example.banhkem.repository;

import com.example.banhkem.entity.Order;
import com.example.banhkem.entity.User;
import com.example.banhkem.entity.OrderStatus; // Import Enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);

    // Sử dụng tham số :status để truyền Enum từ Service vào
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    Double calculateTotalRevenue(@Param("status") OrderStatus status);

    @Query("SELECT SUM(i.quantity) FROM OrderItem i JOIN i.order o WHERE o.status = :status")
    Long countTotalCakesSold(@Param("status") OrderStatus status);

    @Query("SELECT MONTH(o.orderDate), SUM(o.totalAmount) FROM Order o " +
            "WHERE o.status = :status AND YEAR(o.orderDate) = :year " +
            "GROUP BY MONTH(o.orderDate)")
    List<Object[]> getRevenueByMonth(@Param("year") int year, @Param("status") OrderStatus status);

    @Query("SELECT MONTH(o.orderDate), SUM(i.quantity) FROM OrderItem i JOIN i.order o " +
            "WHERE o.status = :status AND YEAR(o.orderDate) = :year " +
            "GROUP BY MONTH(o.orderDate)")
    List<Object[]> getCakeCountByMonth(@Param("year") int year, @Param("status") OrderStatus status);
}