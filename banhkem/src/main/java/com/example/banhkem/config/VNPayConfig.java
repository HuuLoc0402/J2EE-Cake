package com.example.banhkem.config;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayConfig {

    public static final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String vnp_Returnurl = "http://localhost:8080/payment/vnpay-payment";

    // =========================================================================
    // ⚠️ QUAN TRỌNG: HÃY KIỂM TRA LẠI 2 MÃ NÀY TRONG EMAIL VNPAY CỦA BẠN
    // Nếu copy từ tutorial trên mạng thì 100% sẽ lỗi "Sai chữ ký"
    // =========================================================================
    public static final String vnp_TmnCode = "Y1GA01VO";
    public static final String vnp_HashSecret = "BE2MYFAFOL67J403TMR3AYB3ANLAICRD";
    // =========================================================================

    /**
     * Tạo URL thanh toán + Chữ ký bảo mật
     */
    public static String hashAllFields(Map<String, String> inputFields) {
        List<String> fieldNames = new ArrayList<>(inputFields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = inputFields.get(fieldName);

            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    // 1. Build Hash Data: Dữ liệu để tạo chữ ký
                    // Chú ý: VNPay yêu cầu encode cả giá trị trong hashData
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    // 2. Build Query URL: Dữ liệu để gửi trên thanh địa chỉ
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        // Tạo chữ ký bảo mật
        String queryUrl = query.toString();
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());

        return queryUrl + "&vnp_SecureHash=" + vnp_SecureHash;
    }

    /**
     * Mã hóa HMAC-SHA512 chuẩn VNPay
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            Mac mac = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8); // Dùng UTF-8 cho Key
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            mac.init(secretKey);

            // Dùng UTF-8 cho Data
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = mac.doFinal(dataBytes);

            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Lấy IP Address (Đã fix lỗi IPv6 Localhost)
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "Invalid IP";
        }

        // FIX CỨNG: Nếu chạy local thì trả về IPv4 này để VNPay không bị lỗi hash
        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "127.0.0.1".equals(ipAddress)) {
            return "127.0.0.1";
        }
        return ipAddress;
    }
}