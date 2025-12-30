package com.example.banhkem.repository;

import com.example.banhkem.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUserId(Long userId);

    // Đảm bảo thuộc tính closed và lastUpdatedAt tồn tại trong Entity Chat
    List<Chat> findByClosedFalseOrderByLastUpdatedAtDesc();
}