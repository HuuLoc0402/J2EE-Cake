package com.example.banhkem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String receiverName;

    private LocalDateTime orderDate;
    private Double totalAmount;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String shippingAddress;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note;
    private String phone;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
}