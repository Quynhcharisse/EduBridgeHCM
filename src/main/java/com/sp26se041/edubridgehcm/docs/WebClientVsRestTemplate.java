package com.sp26se041.edubridgehcm.docs;

/**
 * ========== WebClient vs RestTemplate - SO SÁNH CHI TIẾT ==========
 *
 * Bạn có 2 lựa chọn để gọi API từ Spring Boot.
 * Đây là chi tiết so sánh để bạn chọn cách phù hợp.
 *
 * ===========================================================================
 *
 * 1️⃣ WebClient (RECOMMENDED) - Modern, Non-blocking, Reactive
 *
 * Ưu điểm:
 * ✅ Non-blocking (async)
 * ✅ Reactive Streams (Mono, Flux)
 * ✅ Better performance (fewer threads)
 * ✅ Connection pooling built-in
 * ✅ Automatic JSON serialization/deserialization
 * ✅ Better error handling
 * ✅ Modern Spring approach
 * ✅ Built for microservices
 *
 * Nhược điểm:
 * ❌ Learning curve (need understand reactive)
 * ❌ Extra dependency (webflux)
 * ❌ Code complexity
 * ❌ Debugging harder
 *
 * Khi sử dụng:
 * ✅ Project mới (Spring Boot 5.0+)
 * ✅ High traffic
 * ✅ Nhiều concurrent requests
 * ✅ Microservices
 * ✅ Performance critical
 *
 * Code Example:
 *
 *     VietQRTaxResponse response = webClient
 *         .get()
 *         .uri("/business?taxCode={code}", taxCode)
 *         .retrieve()
 *         .bodyToMono(VietQRTaxResponse.class)
 *         .timeout(Duration.ofSeconds(10))
 *         .block();  // Make it sync
 *
 * Performance Characteristics:
 * - 1 request:  1ms (very fast)
 * - 10 requests: ~5ms (parallel)
 * - 1000 requests: ~50ms (efficient)
 * - Thread usage: 10-20 threads
 * - Memory: Low
 *
 * ===========================================================================
 *
 * 2️⃣ RestTemplate (LEGACY) - Traditional, Blocking, Simple
 *
 * Ưu điểm:
 * ✅ Simple to understand
 * ✅ No extra dependencies (already in webmvc)
 * ✅ Straightforward code
 * ✅ Easy debugging
 * ✅ Works with old Spring versions
 * ✅ Less learning curve
 *
 * Nhược điểm:
 * ❌ Blocking (wait for response)
 * ❌ Not reactive
 * ❌ Thread-per-request model
 * ❌ Slower performance
 * ❌ More threads = more memory
 * ❌ Deprecated in Spring 6.0+
 * ❌ Not suitable for high concurrency
 *
 * Khi sử dụng:
 * ⚠️ Legacy projects (Spring Boot 4.x)
 * ⚠️ Simple integrations
 * ⚠️ Low traffic
 * ⚠️ Few concurrent requests
 * ⚠️ Team not familiar with reactive
 *
 * Code Example:
 *
 *     RestTemplate restTemplate = new RestTemplateBuilder()
 *         .setConnectTimeout(Duration.ofSeconds(5))
 *         .build();
 *
 *     VietQRTaxResponse response = restTemplate.getForObject(
 *         "https://api.vietqr.io/v2/business?taxCode=" + taxCode,
 *         VietQRTaxResponse.class
 *     );
 *
 * Performance Characteristics:
 * - 1 request:  10ms (slower)
 * - 10 requests: ~100ms (sequential)
 * - 1000 requests: ~10000ms = 10 seconds (very slow)
 * - Thread usage: 1000 threads (heavy)
 * - Memory: High
 *
 * ===========================================================================
 *
 * VISUAL COMPARISON - Handling 10 Requests
 *
 * WebClient (Non-blocking, Parallel):
 * Time: ═════════════════════ 1000ms total
 *
 * Req 1: ████ (100ms)
 * Req 2: ████ (100ms)  ← Overlap with Req 1
 * Req 3: ████ (100ms)  ← Overlap with Req 1,2
 * ...
 * Req 10: ████ (100ms)
 *
 * All run in parallel! Total ≈ 100ms
 *
 * ---
 *
 * RestTemplate (Blocking, Sequential):
 * Time: ════════════════════════════════════════════════ 1000ms
 *
 * Req 1: ████─────────────────────────────────── (100ms, rest wait)
 * Req 2:      ████─────────────────────────────── (100ms, rest wait)
 * Req 3:           ████─────────────────────────── (100ms, rest wait)
 * ...
 * Req 10:                                         ████ (100ms)
 *
 * Each waits for previous! Total = 1000ms
 *
 * ===========================================================================
 *
 * THREAD USAGE COMPARISON
 *
 * Scenario: Handle 1000 concurrent requests
 *
 * WebClient:
 * - Threads needed: ~20 (event-driven)
 * - Memory per thread: minimal
 * - Total memory: ~100MB
 * - Throughput: 10,000 req/s
 * - Scalability: Excellent
 *
 * RestTemplate:
 * - Threads needed: ~1000 (one per request)
 * - Memory per thread: ~1MB
 * - Total memory: ~1000MB = 1GB
 * - Throughput: 1,000 req/s
 * - Scalability: Poor
 *
 * Result: WebClient uses 10x less memory, 10x better throughput!
 *
 * ===========================================================================
 *
 * DEPENDENCY SIZE
 *
 * WebClient:
 * - spring-boot-starter-webflux: ~500KB
 * - reactor-core: ~1MB
 * - netty: ~2MB
 * - Total added: ~3.5MB
 *
 * RestTemplate:
 * - Already in spring-boot-starter-webmvc
 * - No added dependencies
 * - Total added: 0MB
 *
 * ===========================================================================
 *
 * DECISION MATRIX - Choose One:
 *
 * Use WebClient if:
 * □ Spring Boot 5.0+
 * □ High traffic (1000+ req/s)
 * □ Microservices architecture
 * □ Limited resources (memory/threads)
 * □ New project
 * □ Performance critical
 * → Use WebClient ✅
 *
 * Use RestTemplate if:
 * □ Spring Boot 4.x or lower
 * □ Low traffic (<100 req/s)
 * □ Simple monolithic app
 * □ Plenty of resources
 * □ Legacy project
 * □ Quick integration
 * → Use RestTemplate ⚠️
 *
 * For Your Project (EduBridgeHCM):
 * - Spring Boot version? 4.0.0 ✅ (supports both)
 * - Traffic expected? Medium (school verification)
 * - Architecture? Microservices-like ✅
 * - Performance important? Yes ✅
 *
 * → RECOMMENDATION: Use WebClient ✅✅✅
 *
 * ===========================================================================
 *
 * CODE COMPARISON - Same Task
 *
 * Task: Lookup tax code "0311549867" from VietQR API
 *
 * ─── WebClient (Recommended) ───
 *
 * @Service
 * @RequiredArgsConstructor
 * public class TaxServiceImpl implements TaxService {
 *     private final WebClient vietQrWebClient;
 *
 *     @Override
 *     public VietQRTaxResponse lookupTaxCode(String taxCode) {
 *         return vietQrWebClient
 *                 .get()
 *                 .uri(uriBuilder -> uriBuilder
 *                         .queryParam("taxCode", taxCode)
 *                         .build())
 *                 .retrieve()
 *                 .bodyToMono(VietQRTaxResponse.class)
 *                 .timeout(Duration.ofSeconds(10))
 *                 .onErrorResume(this::handleError)
 *                 .block();
 *     }
 * }
 *
 * Advantages:
 * ✅ Fluent API (readable chain)
 * ✅ Built-in timeout
 * ✅ Built-in error handling
 * ✅ Auto JSON mapping
 * ✅ Async capable (.block() optional)
 *
 * ─── RestTemplate (Legacy) ───
 *
 * @Service
 * public class TaxServiceRestTemplateImpl {
 *     private final RestTemplate restTemplate;
 *
 *     public VietQRTaxResponse lookupTaxCode(String taxCode) {
 *         try {
 *             String url = "https://api.vietqr.io/v2/business?taxCode=" + taxCode;
 *             return restTemplate.getForObject(url, VietQRTaxResponse.class);
 *         } catch (RestClientException e) {
 *             // Handle error
 *             return VietQRTaxResponse.builder()
 *                     .errorMessage(e.getMessage())
 *                     .build();
 *         }
 *     }
 * }
 *
 * Disadvantages:
 * ❌ String concatenation (error-prone)
 * ❌ Manual error handling
 * ❌ Manual timeout handling
 * ❌ Try-catch needed
 * ❌ Always blocking
 *
 * ===========================================================================
 *
 * MIGRATION FROM RestTemplate TO WebClient
 *
 * If you have existing RestTemplate code:
 *
 * Before (RestTemplate):
 * ---------------------
 * String url = "https://api.vietqr.io/v2/business?taxCode=" + taxCode;
 * VietQRTaxResponse response = restTemplate.getForObject(url, VietQRTaxResponse.class);
 *
 * After (WebClient):
 * ------------------
 * VietQRTaxResponse response = webClient
 *         .get()
 *         .uri("/business", uriBuilder -> uriBuilder.queryParam("taxCode", taxCode).build())
 *         .retrieve()
 *         .bodyToMono(VietQRTaxResponse.class)
 *         .block();
 *
 * Benefits:
 * ✅ Type-safe URI building
 * ✅ Better error handling
 * ✅ Can remove .block() for async
 * ✅ Better performance
 *
 * ===========================================================================
 *
 * SPRING BOOT VERSION SUPPORT
 *
 * Spring Boot 4.0.0:
 * ✅ RestTemplate: Fully supported
 * ✅ WebClient: Fully supported
 * ✅ Can use either
 * ✅ Recommended: WebClient (modern)
 *
 * Spring Boot 5.x:
 * ✅ RestTemplate: Supported but deprecated
 * ✅ WebClient: Fully supported
 * ✅ Recommended: WebClient
 *
 * Spring Boot 6.0+:
 * ❌ RestTemplate: Removed
 * ✅ WebClient: Only option
 * ✅ Must use: WebClient
 *
 * ===========================================================================
 *
 * FINAL RECOMMENDATION FOR EduBridgeHCM
 *
 * We chose: WebClient ✅
 *
 * Reasons:
 * 1. Modern Spring Boot 4.0 (supports both, recommend WebClient)
 * 2. Scalability (school registration can spike)
 * 3. Microservices-ready architecture
 * 4. Better resource usage
 * 5. Future-proof (RestTemplate being deprecated)
 * 6. Performance critical (reduce latency)
 * 7. Non-blocking async capability
 *
 * Implementation:
 * ✅ WebClient in TaxServiceImpl.java
 * ✅ Optional RestTemplate in TaxServiceRestTemplateImpl.java (reference)
 * ✅ Easy to switch if needed
 *
 * ===========================================================================
 */
public class WebClientVsRestTemplate {
    // Documentation only - not for execution
}

