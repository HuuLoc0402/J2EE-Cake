package com.example.banhkem.service;

import com.example.banhkem.entity.*;
import com.example.banhkem.repository.OrderRepository;
import com.example.banhkem.repository.CakeRepository; // Đã thêm để xử lý kho hàng
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CakeRepository cakeRepository; // Inject CakeRepository để cập nhật số lượng

    @Autowired
    private CartService cartService;

    /**
     * Tạo đơn hàng từ giỏ hàng
     */
    @Transactional
    public Order createOrderFromCart(User user, Order orderInfo) {
        List<CartItem> cartItems = cartService.getCartByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể tạo đơn hàng.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPhone(orderInfo.getPhone());
        order.setShippingAddress(orderInfo.getShippingAddress());

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setCake(cartItem.getCake());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getCake().getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
            totalAmount += (orderItem.getPrice() * orderItem.getQuantity());
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(user);

        return savedOrder;
    }

    /**
     * Cập nhật số lượng bánh trong kho khi thanh toán thành công
     */
    @Transactional
    public void reduceStock(Order order) {
        if (order == null || order.getItems() == null) return;

        for (OrderItem item : order.getItems()) {
            Cake cake = item.getCake();
            int newStock = cake.getStockQuantity() - item.getQuantity();

            if (newStock < 0) {
                throw new RuntimeException("Sản phẩm " + cake.getName() + " không đủ số lượng trong kho!");
            }

            cake.setStockQuantity(newStock);
            cakeRepository.save(cake); // Lưu cập nhật vào Database
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
        order.setStatus(status);
        orderRepository.save(order);
    }

    // --- CÁC PHƯƠNG THỨC THỐNG KÊ DASHBOARD (ĐÃ SỬA LỖI 500) ---

    public Double getTotalRevenue() {
        try {
            // Sử dụng Enum OrderStatus.COMPLETED để thống kê chính xác
            Double total = orderRepository.calculateTotalRevenue(OrderStatus.COMPLETED);
            return (total != null) ? total : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public Long getTotalCakesSold() {
        try {
            Long count = orderRepository.countTotalCakesSold(OrderStatus.COMPLETED);
            return (count != null) ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<Double> getMonthlyRevenueData(int year) {
        Double[] monthlyData = new Double[12];
        Arrays.fill(monthlyData, 0.0);

        try {
            List<Object[]> results = orderRepository.getRevenueByMonth(year, OrderStatus.COMPLETED);
            for (Object[] row : results) {
                if (row != null && row[0] != null) {
                    int month = ((Number) row[0]).intValue();
                    Double total = (row[1] != null) ? ((Number) row[1]).doubleValue() : 0.0;
                    if (month >= 1 && month <= 12) {
                        monthlyData[month - 1] = total;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList(monthlyData);
    }

    public List<Long> getMonthlyCakeCountData(int year) {
        Long[] monthlyData = new Long[12];
        Arrays.fill(monthlyData, 0L);

        try {
            List<Object[]> results = orderRepository.getCakeCountByMonth(year, OrderStatus.COMPLETED);
            for (Object[] row : results) {
                if (row != null && row[0] != null) {
                    int month = ((Number) row[0]).intValue();
                    Long count = (row[1] != null) ? ((Number) row[1]).longValue() : 0L;
                    if (month >= 1 && month <= 12) {
                        monthlyData[month - 1] = count;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList(monthlyData);
    }
}