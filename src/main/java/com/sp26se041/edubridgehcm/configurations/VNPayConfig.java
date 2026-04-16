package com.sp26se041.edubridgehcm.configurations;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.regex.Pattern;

@Component
public class VNPayConfig {

    private static final Pattern IPV4_PATTERN = Pattern.compile("^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$");

    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    public static String vnp_ReturnUrl = "https://edubridgehcm-fe.onrender.com/payment/vnpay-result";

    public static String vnp_TmnCode;

    public static String vnp_HashSecret;

    public static String vnp_apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

    @Value("${vnp.tmn-code:ZDT0KK8Q}")
    public void setVnpTmnCode(String tmnCode) {
        vnp_TmnCode = tmnCode;
    }

    @Value("${vnp.hash-secret:0ANJ6LZDK4J3HX20MU176DGK5J19D5WV}")
    public void setVnpHashSecret(String hashSecret) {
        vnp_HashSecret = hashSecret;
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    //get the user's real IP when they send request
    // getLocalAddr() = IP server
    // getRemoteAddr() = IP local
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }

            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getHeader("X-REAL-IP");
            }

            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getRemoteAddr();
            }

            if (ipAddress == null || ipAddress.isBlank()) {
                return "127.0.0.1";
            }

            ipAddress = ipAddress.trim();

            // VNPay expects a valid IPv4 format; normalize local IPv6 loopback.
            if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
                return "127.0.0.1";
            }

            if (!IPV4_PATTERN.matcher(ipAddress).matches()) {
                return "127.0.0.1";
            }

            return ipAddress;
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    // create a string of random numbers
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789"; // character sets
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length()))); //get a random number
        }
        return sb.toString();
    }
}