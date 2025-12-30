package com.example.banhkem.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty; // Thêm import này
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content;

    private LocalDateTime timestamp;
    private String senderRole;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    // SỬA TẠI ĐÂY: Cho phép nhận dữ liệu từ Frontend gửi lên nhưng không in ra JSON trả về
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Chat chat;
}