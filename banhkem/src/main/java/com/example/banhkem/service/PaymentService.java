package com.example.banhkem.service;

import com.example.banhkem.entity.Payment;
import com.example.banhkem.entity.Order;
import com.example.banhkem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PaymentService {
    @Autowired private PaymentRepository paymentRepository;

    public void savePayment(Order order, String transactionId, String status) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setTransactionId(transactionId);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod("VNPAY");
        payment.setStatus(status);
        paymentRepository.save(payment);
    }
}