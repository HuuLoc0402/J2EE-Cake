package com.example.banhkem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private Double amount;
    private LocalDateTime paymentDate;
    private String paymentMethod; // BỔ SUNG TRƯỜNG NÀY ĐỂ FIX LỖI HÌNH 5
    private String status;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
}