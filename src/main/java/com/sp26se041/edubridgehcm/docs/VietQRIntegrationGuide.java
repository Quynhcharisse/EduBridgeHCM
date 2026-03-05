/**
 * ========== HƯỚNG DẪN TÍCH HỢP VietQR TAX API VÀO SPRING BOOT ==========
 *
 * GỌI API CHECK TAX CODE TỪ BACKEND (SPRING BOOT)
 * Cách hiện đại và sạch sẽ nhất
 *
 * ===========================================================================
 *
 * PART 1: SETUP DEPENDENCIES
 * ===========================================================================
 *
 * Đã thêm vào pom.xml:
 * 1. spring-boot-starter-webflux (WebClient)
 * 2. jackson-databind (JSON parsing)
 * 3. httpclient5 (optional, for debugging)
 *
 * ===========================================================================
 *
 * PART 2: CONFIGURATION (VietQRConfig.java)
 * ===========================================================================
 *
 * ✅ Tạo WebClient bean trong configuration
 * ✅ Cấu hình default headers
 * ✅ Thêm API key vào header nếu cần
 * ✅ Set timeout
 *
 * File: src/main/java/.../configurations/VietQRConfig.java
 *
 * Tại sao cần config riêng?
 * - Centralize configuration
 * - Tái sử dụng WebClient
 * - Dễ thay đổi API endpoint
 * - Quản lý secret key an toàn
 *
 * ===========================================================================
 *
 * PART 3: RESPONSE DTO (VietQRTaxResponse.java)
 * ===========================================================================
 *
 * ✅ Ánh xạ response từ API
 * ✅ Thêm fields cho error handling
 * ✅ @JsonIgnoreProperties để bỏ qua unknown fields
 *
 * File: src/main/java/.../responses/VietQRTaxResponse.java
 *
 * Tại sao cần DTO?
 * - Type-safe
 * - Auto serialize/deserialize
 * - Validation
 * - Documentation qua code
 *
 * ===========================================================================
 *
 * PART 4: SERVICE IMPLEMENTATION (TaxServiceImpl.java)
 * ===========================================================================
 *
 * ✅ Implement TaxService interface
 * ✅ Sử dụng WebClient để call API
 * ✅ Error handling (timeout, network error, API error)
 * ✅ Logging
 * ✅ Input validation
 *
 * File: src/main/java/.../services/implementors/TaxServiceImpl.java
 *
 * 2 Methods:
 * 1. lookupTaxCode(String) - Synchronous (blocking)
 *    - Dùng trong controller
 *    - Gọi .block() để wait response
 *
 * 2. lookupTaxCodeAsync(String) - Asynchronous (reactive)
 *    - Dùng khi integrate với reactive stream
 *    - Return Mono<VietQRTaxResponse>
 *
 * ===========================================================================
 *
 * PART 5: PROPERTIES CONFIGURATION (application.properties)
 * ===========================================================================
 *
 * Thêm vào application.properties:
 *
 * vietqr.api.url=https://api.vietqr.io/v2/business
 * vietqr.api.key=${VIETQR_API_KEY:}     # Optional
 * vietqr.api.timeout=10000              # 10 seconds
 *
 * Tại sao externalize config?
 * - Khác config cho dev/prod
 * - Bảo mật secret key
 * - Dễ thay đổi mà không rebuild
 * - Environment-specific (staging, production)
 *
 * ===========================================================================
 *
 * PART 6: CONTROLLER INTEGRATION
 * ===========================================================================
 *
 * Ví dụ sử dụng trong controller:
 *
 * @RestController
 * @RequiredArgsConstructor
 * public class SchoolController {
 *
 *     private final TaxService taxService;
 *
 *     @PostMapping("/verify-tax-code")
 *     public ResponseEntity<ResponseObject> verifyTaxCode(
 *             @RequestBody SchoolRegistrationRequest request) {
 *
 *         // 1. Lookup tax code
 *         VietQRTaxResponse taxResponse = taxService.lookupTaxCode(request.getTaxCode());
 *
 *         // 2. Check if success
 *         if (taxResponse.getHttpStatusCode() == 200) {
 *             // 3. Save company info từ response
 *             School school = School.builder()
 *                 .name(taxResponse.getCompanyName())
 *                 .address(taxResponse.getAddress())
 *                 .phone(taxResponse.getPhone())
 *                 .email(taxResponse.getEmail())
 *                 .taxCode(taxResponse.getTaxCode())
 *                 .build();
 *
 *             schoolRepo.save(school);
 *
 *             return ResponseBuilder.build(
 *                 HttpStatus.OK,
 *                 "Xác minh thành công",
 *                 taxResponse
 *             );
 *         } else {
 *             return ResponseBuilder.build(
 *                 HttpStatus.BAD_REQUEST,
 *                 "Tax code không hợp lệ: " + taxResponse.getErrorMessage(),
 *                 null
 *             );
 *         }
 *     }
 * }
 *
 * ===========================================================================
 *
 * PART 7: ERROR HANDLING & BEST PRACTICES
 * ===========================================================================
 *
 * ✅ DO:
 * 1. Always validate input (null, empty, format)
 * 2. Set timeout (không chờ vô hạn)
 * 3. Handle all exception types
 * 4. Log both success và error
 * 5. Return meaningful error messages
 * 6. Use @Transactional khi save DB
 * 7. Implement caching (1-7 days)
 * 8. Add retry mechanism nếu cần
 * 9. Monitor API response time
 * 10. Implement circuit breaker khi API thường down
 *
 * ❌ DON'T:
 * 1. Không validate input
 * 2. Chờ response vô hạn (no timeout)
 * 3. Swallow exception (.block() mà không handle)
 * 4. Không log
 * 5. Return generic error message
 * 6. Direct DB query từ API response (validate trước)
 * 7. Call API mỗi lần (implement caching)
 * 8. Không test error scenario
 * 9. Hardcode API URL
 * 10. Không monitor performance
 *
 * ===========================================================================
 *
 * PART 8: CACHING (Optional nhưng RECOMMENDED)
 * ===========================================================================
 *
 * Tại sao cần cache?
 * - Tax code không thay đổi (immutable)
 * - Giảm call API
 * - Tăng response time
 * - Giảm load cho VietQR server
 *
 * Cách implement caching:
 *
 * @Service
 * @RequiredArgsConstructor
 * public class TaxServiceImpl implements TaxService {
 *
 *     private final TaxRepository taxRepository;  // Cache to DB
 *
 *     @Override
 *     public VietQRTaxResponse lookupTaxCode(String taxCode) {
 *
 *         // 1. Check cache first
 *         Optional<TaxCache> cached = taxRepository.findByTaxCode(taxCode);
 *         if (cached.isPresent() && !isExpired(cached.get())) {
 *             log.info("💾 Cache hit: {}", taxCode);
 *             return cached.get().toResponse();
 *         }
 *
 *         // 2. Call API nếu cache miss
 *         VietQRTaxResponse response = callVietQRAPI(taxCode);
 *
 *         // 3. Save to cache
 *         if (response.getHttpStatusCode() == 200) {
 *             TaxCache cache = TaxCache.builder()
 *                 .taxCode(taxCode)
 *                 .data(response)
 *                 .cachedAt(LocalDateTime.now())
 *                 .build();
 *             taxRepository.save(cache);
 *         }
 *
 *         return response;
 *     }
 * }
 *
 * ===========================================================================
 *
 * PART 9: TESTING
 * ===========================================================================
 *
 * Unit Test Example:
 *
 * @SpringBootTest
 * class TaxServiceTest {
 *
 *     @MockBean
 *     private WebClient vietQrWebClient;
 *
 *     @Autowired
 *     private TaxService taxService;
 *
 *     @Test
 *     void testLookupTaxCodeSuccess() {
 *         // Given
 *         String taxCode = "0311549867";
 *         VietQRTaxResponse expected = VietQRTaxResponse.builder()
 *             .taxCode(taxCode)
 *             .companyName("EDU BRIDGE")
 *             .httpStatusCode(200)
 *             .build();
 *
 *         when(vietQrWebClient.get()...)
 *             .thenReturn(expected);
 *
 *         // When
 *         VietQRTaxResponse result = taxService.lookupTaxCode(taxCode);
 *
 *         // Then
 *         assertEquals(200, result.getHttpStatusCode());
 *         assertEquals("EDU BRIDGE", result.getCompanyName());
 *     }
 *
 *     @Test
 *     void testLookupTaxCodeNotFound() {
 *         // Test 404 case
 *     }
 *
 *     @Test
 *     void testLookupTaxCodeTimeout() {
 *         // Test timeout case
 *     }
 * }
 *
 * ===========================================================================
 *
 * PART 10: MONITORING & LOGGING
 * ===========================================================================
 *
 * Logback Configuration (logback-spring.xml):
 *
 * <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
 *     <file>logs/tax-service.log</file>
 *     <encoder>
 *         <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
 *     </encoder>
 * </appender>
 *
 * <logger name="com.sp26se041.edubridgehcm.services.TaxService" level="INFO">
 *     <appender-ref ref="FILE" />
 * </logger>
 *
 * Metrics:
 * - Response time (< 2s là tốt)
 * - Success rate (> 95%)
 * - Error breakdown (404, timeout, 5xx)
 * - Cache hit rate
 *
 * ===========================================================================
 *
 * PART 11: DEPLOYMENT CHECKLIST
 * ===========================================================================
 *
 * ✅ Dependencies added to pom.xml
 * ✅ Configuration class created (VietQRConfig)
 * ✅ Response DTO created (VietQRTaxResponse)
 * ✅ Service interface created (TaxService)
 * ✅ Service implementation created (TaxServiceImpl)
 * ✅ application.properties configured
 * ✅ Error handling implemented
 * ✅ Logging implemented
 * ✅ Input validation implemented
 * ✅ Tests written
 * ✅ Documentation written (this file)
 * ✅ Code review completed
 * ✅ Performance tested
 * ✅ Security reviewed (no secret leak)
 * ✅ API endpoint documented in Swagger
 *
 * ===========================================================================
 *
 * PART 12: TROUBLESHOOTING
 * ===========================================================================
 *
 * Issue 1: WebClient not autowired
 * Solution:
 * - Check @Bean("vietQrWebClient") in VietQRConfig
 * - Verify configuration is scanned by @SpringBootApplication
 * - Check spring-boot-starter-webflux is in pom.xml
 *
 * Issue 2: Connection timeout
 * Solution:
 * - Increase timeout value
 * - Check network connectivity
 * - Verify VietQR API is accessible
 * - Check firewall rules
 *
 * Issue 3: JSON deserialization error
 * Solution:
 * - Add @JsonIgnoreProperties(ignoreUnknown = true)
 * - Check VietQRTaxResponse fields match API response
 * - Add debug logging to see actual response
 *
 * Issue 4: API returns 401 Unauthorized
 * Solution:
 * - Check API key in configuration
 * - Verify Authorization header format
 * - Check credentials expiration
 *
 * Issue 5: Rate limiting (429 Too Many Requests)
 * Solution:
 * - Implement caching
 * - Add delay between requests
 * - Implement circuit breaker
 * - Contact VietQR for higher limit
 *
 * ===========================================================================
 *
 * REFERENCES
 * ===========================================================================
 *
 * VietQR:
 * - Website: https://vietqr.io/
 * - API Docs: https://api.vietqr.io/docs
 *
 * Spring WebFlux:
 * - Docs: https://spring.io/projects/spring-webflux
 * - WebClient Guide: https://spring.io/guides/gs/consuming-rest-reactive/
 *
 * Spring Boot:
 * - Docs: https://spring.io/projects/spring-boot
 * - Externalize Config: https://docs.spring.io/spring-boot/docs/current/guide-html/features.html#features.external-config
 *
 * ===========================================================================
 */
package com.sp26se041.edubridgehcm.docs;

public class VietQRIntegrationGuide {
    // Chỉ để documentation, không dùng class này
}

