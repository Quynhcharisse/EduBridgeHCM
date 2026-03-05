package com.sp26se041.edubridgehcm.utils;

/**
 * Tài liệu tham khảo VietQR API
 *
 * ========== VietQR API Endpoints ==========
 *
 * 1. LOOKUP TAX CODE
 *    GET https://api.vietqr.io/v2/business?taxCode={taxCode}
 *
 *    Mô tả: Lookup thông tin doanh nghiệp từ mã số thuế
 *
 *    Parameters:
 *    - taxCode (required): Mã số thuế, 10 chữ số
 *      VD: "0311549867"
 *
 *    Response Success (200):
 *    {
 *      "code": "00",
 *      "message": "Success",
 *      "data": {
 *        "taxCode": "0311549867",
 *        "companyName": "CÔNG TY TNHH GIÁO DỤC EDU BRIDGE",
 *        "companyNameEn": "EDU BRIDGE EDUCATION CO., LTD",
 *        "address": "123 Nguyễn Hữu Cảnh, Bình Thạnh, TP.HCM",
 *        "phone": "0283823889",
 *        "email": "contact@edubridge.vn",
 *        "representativeName": "Trần Văn A",
 *        "representativeTitle": "Giám Đốc",
 *        "businessField": "Giáo dục",
 *        "status": "ACTIVE",
 *        "registrationDate": "2015-10-01",
 *        "terminationDate": null
 *      }
 *    }
 *
 *    Response Error (404):
 *    {
 *      "code": "01",
 *      "message": "Data not found",
 *      "data": null
 *    }
 *
 *    Possible HTTP Status Codes:
 *    - 200: OK - Thành công
 *    - 400: Bad Request - Tax code không hợp lệ
 *    - 404: Not Found - Không tìm thấy tax code
 *    - 408: Timeout - Request timeout
 *    - 500: Internal Server Error - Lỗi server VietQR
 *    - 429: Too Many Requests - Quá nhiều request (rate limit)
 *
 * ========== BEST PRACTICES ==========
 *
 * 1. VALIDATION
 *    - Kiểm tra tax code có rỗng không
 *    - Kiểm tra tax code có đúng 10 chữ số không
 *    - Trim() whitespace trước khi gửi
 *
 * 2. ERROR HANDLING
 *    - Luôn xử lý timeout (set timeout = 10s)
 *    - Luôn xử lý network error
 *    - Implement retry mechanism nếu cần
 *
 * 3. CACHING
 *    - Cache result 1-7 ngày tùy nghiệp vụ
 *    - Tránh call API lặp lại cho same tax code
 *
 * 4. LOGGING
 *    - Log success: INFO level
 *    - Log error: ERROR level
 *    - Log request/response có độ dài hợp lý
 *
 * 5. RATE LIMITING
 *    - Nếu VietQR có giới hạn: implement circuit breaker
 *    - Fallback strategy nếu API down
 *
 * ========== USAGE EXAMPLE ==========
 *
 * // Controller
 * @PostMapping("/verify-tax-code")
 * public ResponseEntity<ResponseObject> verifyTaxCode(@RequestBody SchoolRegistrationRequest request) {
 *     VietQRTaxResponse response = taxService.lookupTaxCode(request.getTaxCode());
 *
 *     if (response.getHttpStatusCode() == 200) {
 *         // Thành công - tiếp tục xử lý
 *         // Lưu school info từ response
 *     } else {
 *         // Lỗi - return error
 *         return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tax code không hợp lệ", null);
 *     }
 * }
 *
 * ========== COMMON ISSUES ==========
 *
 * Issue 1: "Connection refused"
 *   → VietQR API down hoặc không thể access
 *   → Fix: Check network, firewall, VPN
 *
 * Issue 2: "Tax code not found" (404)
 *   → Tax code không tồn tại trong database VietQR
 *   → Fix: Kiểm tra lại tax code
 *
 * Issue 3: "Timeout"
 *   → Request mất quá lâu
 *   → Fix: Tăng timeout hoặc check network
 *
 * Issue 4: "Rate limit exceeded" (429)
 *   → Quá nhiều request trong thời gian ngắn
 *   → Fix: Implement caching, rate limiting
 *
 * ========== VietQR API Documentation ==========
 * Website: https://vietqr.io/
 * API Docs: https://api.vietqr.io/docs
 * Support: support@vietqr.io
 */
public class VietQRDocumentation {
    // Không dùng class này trực tiếp
    // Chỉ để tham khảo cách sử dụng VietQR API
}

