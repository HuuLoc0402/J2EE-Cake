package com.example.banhkem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cakes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String name;
    private Double price;
    private String image; // URL ảnh

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}