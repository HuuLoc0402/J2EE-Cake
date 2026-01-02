package com.example.banhkem.service;

import com.example.banhkem.entity.Order;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendOrderEmail(Order order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);

            // Xử lý file template invoice.html thành chuỗi String
            String htmlContent = templateEngine.process("mail/invoice", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Bunny Bakery - Hóa đơn #" + order.getId());
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
        }
    }
}