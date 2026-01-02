package com.example.banhkem.controller;

import com.example.banhkem.config.VNPayConfig;
import com.example.banhkem.entity.Order;
import com.example.banhkem.entity.OrderStatus;
import com.example.banhkem.service.MailService;
import com.example.banhkem.service.OrderService;
import com.example.banhkem.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MailService mailService;

    /**
     * Bước 1: Tạo URL để khách hàng chuyển hướng sang cổng thanh toán VNPay
     */
    @GetMapping("/create")
    public String createPayment(HttpServletRequest req, @RequestParam Long orderId, @RequestParam Double amount) {
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");

        // TxnRef bao gồm mã đơn và thời gian để tránh trùng lặp phiên thanh toán
        vnp_Params.put("vnp_TxnRef", orderId + "_" + System.currentTimeMillis());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang Bunny #" + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", VNPayConfig.getIpAddress(req));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        cld.add(Calendar.MINUTE, 15); // Hết hạn thanh toán sau 15p
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        String queryUrl = VNPayConfig.hashAllFields(vnp_Params);
        return "redirect:" + VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    /**
     * Bước 2: VNPay trả kết quả về sau khi khách thanh toán xong
     */
    @GetMapping("/vnpay-payment")
    public String paymentReturn(HttpServletRequest request, Model model) {
        try {
            // Lấy tất cả tham số VNPay gửi về
            Map<String, String> fields = new HashMap<>();
            for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = (String) params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            // Xác thực chữ ký để đảm bảo dữ liệu không bị giả mạo
            String signValue = VNPayConfig.hmacSHA512(VNPayConfig.vnp_HashSecret, buildHashData(fields));

            if (signValue.equals(vnp_SecureHash)) {
                String responseCode = fields.get("vnp_ResponseCode");
                String txnRef = fields.get("vnp_TxnRef");
                Long orderId = Long.parseLong(txnRef.split("_")[0]);
                Order order = orderService.getOrderById(orderId);

                if ("00".equals(responseCode)) {
                    // Logic nghiệp vụ chính
                    orderService.updateStatus(orderId, OrderStatus.CONFIRMED); // Chốt đơn
                    orderService.reduceStock(order); // Trừ số lượng trong kho
                    paymentService.savePayment(order, fields.get("vnp_TransactionNo"), "SUCCESS"); // Lưu giao dịch

                    // GỬI MAIL HÓA ĐƠN CHO KHÁCH (CHỨC NĂNG MỚI)
                    mailService.sendOrderEmail(order);

                    model.addAttribute("message", "Thanh toán thành công!");
                    model.addAttribute("orderId", orderId);
                } else {
                    model.addAttribute("message", "Thanh toán không thành công hoặc đã bị hủy.");
                }
            } else {
                model.addAttribute("message", "Lỗi xác thực chữ ký VNPay.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Lỗi xử lý hệ thống.");
        }
        return "order/payment-result";
    }

    private String buildHashData(Map<String, String> fields) {
        List<String> keys = new ArrayList<>(fields.keySet());
        Collections.sort(keys);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = keys.iterator();
        while (itr.hasNext()) {
            String key = itr.next();
            String value = fields.get(key);
            if (value != null && !value.isEmpty()) {
                try {
                    hashData.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) hashData.append("&");
                } catch (UnsupportedEncodingException e) { e.printStackTrace(); }
            }
        }
        return hashData.toString();
    }
}