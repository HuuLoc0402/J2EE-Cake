package com.example.banhkem.controller;

import com.example.banhkem.config.VNPayConfig;
import com.example.banhkem.entity.Order;
import com.example.banhkem.entity.OrderStatus;
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

    /**
     * Tạo URL thanh toán VNPay
     */
    @GetMapping("/create")
    public String createPayment(
            HttpServletRequest req,
            @RequestParam Long orderId,
            @RequestParam Double amount
    ) {

        Map<String, String> vnp_Params = new HashMap<>();

        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);

        // VNPay yêu cầu nhân 100
        vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100)));
        vnp_Params.put("vnp_CurrCode", "VND");

        // TxnRef: orderId_timestamp (an toàn & dễ tách)
        vnp_Params.put("vnp_TxnRef", orderId + "_" + System.currentTimeMillis());

        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");

        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", VNPayConfig.getIpAddress(req));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));

        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        // Hết lỗi 99 – thời hạn thanh toán 15 phút
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        String queryUrl = VNPayConfig.hashAllFields(vnp_Params);
        return "redirect:" + VNPayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    /**
     * VNPay redirect về sau thanh toán
     */
    @GetMapping("/vnpay-payment")
    public String paymentReturn(HttpServletRequest request, Model model) {

        try {
            /* ================= XÁC THỰC CHỮ KÝ ================= */

            Map<String, String> fields = new HashMap<>();
            request.getParameterMap().forEach((key, value) -> {
                if (value != null && value.length > 0) {
                    fields.put(key, value[0]);
                }
            });

            String vnp_SecureHash = fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            String signValue = VNPayConfig.hmacSHA512(
                    VNPayConfig.vnp_HashSecret,
                    buildHashData(fields)
            );

            if (!signValue.equals(vnp_SecureHash)) {
                model.addAttribute("message", "Chữ ký không hợp lệ.");
                return "order/payment-result";
            }

            /* ================= XỬ LÝ KẾT QUẢ ================= */

            String responseCode = fields.get("vnp_ResponseCode");
            String txnRef = fields.get("vnp_TxnRef");

            // Tách orderId từ TxnRef
            Long orderId = Long.parseLong(txnRef.split("_")[0]);
            Order order = orderService.getOrderById(orderId);

            if ("00".equals(responseCode) && order != null) {
                orderService.updateStatus(orderId, OrderStatus.CONFIRMED);
                orderService.reduceStock(order);
                paymentService.savePayment(
                        order,
                        fields.get("vnp_TransactionNo"),
                        "SUCCESS"
                );
                model.addAttribute("message", "Thanh toán thành công!");
            } else {
                model.addAttribute("message", "Thanh toán thất bại hoặc bị hủy.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", "Lỗi xử lý hệ thống.");
        }

        return "order/payment-result";
    }

    /**
     * Build chuỗi hash data cho xác thực chữ ký khi return
     * (KHÔNG encode – chuẩn VNPay)
     */
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
                    // FIX: Append key
                    hashData.append(key);
                    hashData.append("=");
                    // FIX: Encode value
                    hashData.append(URLEncoder.encode(value, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        hashData.append("&");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return hashData.toString();
    }

}
