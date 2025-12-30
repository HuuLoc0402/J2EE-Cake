package com.example.banhkem.entity;

public enum OrderStatus {
    PENDING,    // Chờ xử lý
    CONFIRMED,  // Đã xác nhận (đã thanh toán VNPay)
    SHIPPING,   // Đang giao
    COMPLETED,  // Hoàn thành
    CANCELLED   // Đã hủy
}