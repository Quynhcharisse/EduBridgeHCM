package com.sp26se041.edubridgehcm.configurations;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Component
public class VNPayConfig {
    //1.Hard-coded configuration values (configuration constants)
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl;
    public static String vnp_TmnCode = "2AWK2CW3";
    public static String vnp_HashSecret = "BMY1Z1KV9PZCFGENB4DSD3DMGWDO1SS6";
    public static String vnp_apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

    @Value("${server.type}")
    public void setVnpReturnUrl(String serverType) {
        vnp_ReturnUrl = serverType;
    }

    //2. Create security signature to prevent counterfeiting (chống giả mạo)
    public static String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) { // check null
                throw new NullPointerException();
            }

            final Mac hmac512 = Mac.getInstance("HmacSHA512");   // create HmacSHA512 object
            byte[] hmacKeyBytes = key.getBytes();//key convert to byte

            //define the key used for the HmacSHA512 algorithm
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey); //load secret key into the algorithm

            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8); //convert data to byte
            byte[] result = hmac512.doFinal(dataBytes); //create hash

            // convert byte to hex string, (hash is byte) ==> String hex is easy to pass API
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString(); // this is final signature sent to VNPAY
        } catch (Exception ex) {
            return "";
        }
    }

    //get the user's real IP when they send request
    // getLocalAddr() = IP server
    // getRemoteAddr() = IP local
    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR"); // get header proxy
            if (ipAdress == null) { // if not have --> get IP local
                ipAdress = request.getRemoteAddr(); //catch error
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP" + e.getMessage();
        }
        return ipAdress;
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