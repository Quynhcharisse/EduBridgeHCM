package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.PackageType;
import com.sp26se041.edubridgehcm.enums.SupportLevel;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class ConfigSystemUtil {

    public static String validateUrlFormat(Map<String, Object> mediaConfig, String url, boolean isImage) {
        if (url == null || url.isBlank()) {
            return null;
        }

        if (mediaConfig == null) {
            return "Cấu hình truyền thông không tồn tại";
        }

        String formatKey = isImage ? "imgFormat" : "docFormat";
        String typeLabel = isImage ? "ảnh" : "tài liệu";

        // 3. Lấy danh sách định dạng cho phép từ Map
        List<Map<String, String>> allowedFormats = (List<Map<String, String>>) mediaConfig.get(formatKey);

        if (allowedFormats == null || allowedFormats.isEmpty()) {
            return "Chưa cấu hình định dạng cho phép cho " + typeLabel;
        }

        try {
            String cleanUrl = url.split("\\?")[0].toLowerCase();

            if (!cleanUrl.contains(".")) {
                return "Đường dẫn " + typeLabel + " không chứa định dạng file hợp lệ";
            }

            String fileExt = cleanUrl.substring(cleanUrl.lastIndexOf("."));

            boolean isValid = allowedFormats.stream().anyMatch(f -> fileExt.equals(f.get("format").toLowerCase()));

            if (!isValid) {
                return "Định dạng file " + fileExt + " không được hỗ trợ cho " + typeLabel;
            }

        } catch (Exception e) {
            return "Lỗi khi kiểm tra định dạng đường dẫn " + typeLabel;
        }

        return null; // Hợp lệ
    }

    public static String validateFileSize(PlatformConfig media, MultipartFile file, boolean isImage) {

        Map<String, Object> mediaConfig = (Map<String, Object>) media.getValue();

        if (mediaConfig == null) {
            throw new RuntimeException("Không tìm thấy cấu hình của truyền thông");
        }
        String keySize = isImage ? "maxImgSize" : "maxDocSize";

        Object value = mediaConfig.get(keySize);

        if (value == null) throw new RuntimeException("Chưa cấu hình dung lượng cho " + (isImage ? "ảnh" : "tài liệu"));

        int maxSizeMb = ((Number) value).intValue();
        long maxSizeBytes = (long) maxSizeMb * 1024 * 1024;

        if (file.getSize() > maxSizeBytes) {
            throw new RuntimeException("Dung lượng file vượt quá giới hạn " + maxSizeMb + "MB");
        }

        return null;
    }

    public static String validateFileFormat(PlatformConfig media, MultipartFile file, boolean isImage) {

        Map<String, Object> mediaConfig = (Map<String, Object>) media.getValue();

        if (mediaConfig == null) {
            throw new RuntimeException("Không tìm thấy cấu hình của truyền thông");
        }

        String formatKey = isImage ? "imgFormat" : "docFormat";

        List<Map<String, String>> allowedFormats = (List<Map<String, String>>) mediaConfig.get(formatKey);

        if (allowedFormats.isEmpty()) {
            throw new RuntimeException("Chưa cấu hình định dạng cho phép!");
        }

        List<String> formatList = allowedFormats.stream().map(m -> m.get("format")).toList();

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new RuntimeException("Tên file không hợp lệ!");
        }

        String fileExt = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();

        boolean isValid = formatList.contains(fileExt);

        if (!isValid) {
            throw new RuntimeException("Định dạng " + fileExt + " không được hỗ trợ cho " + (isImage ? "ảnh" : "tài liệu"));
        }

        return null;
    }

    //Tách kết quả tính giá để lưu đủ net / phí dịch vụ / thuế / giá cuối lên DB.
    public record SubscriptionPriceBreakdown(BigDecimal netPrice, BigDecimal serviceFee, BigDecimal taxFee,
                                             BigDecimal finalPrice) {
    }

    //Tổng cộng giá ==> trả về tổng
    public static BigDecimal calculateSubscriptionPrice(UpsertServicePackageFeeRequest request, Map<String, Object> business) {
        return calculateSubscriptionPriceBreakdown(request, business).finalPrice();
    }

    //Tờ hóa đơn chi tiết
    public static SubscriptionPriceBreakdown calculateSubscriptionPriceBreakdown(UpsertServicePackageFeeRequest request, Map<String, Object> business) {

        validatePricingInput(request, business);

        Map<String, Object> pricingConfig = getPricingConfig(business);
        Map<String, Object> basePricing = (Map<String, Object>) pricingConfig.get("basePricing");
        Map<String, Object> featureUnitPricing = (Map<String, Object>) pricingConfig.get("featureUnitPricing");
        Map<String, Object> packageQuotas = (Map<String, Object>) pricingConfig.get("packageQuotas");

        PackageType packageType = parsePackageType(request.getPackageType());

        //bước xác định giá nền (gốc) của từng gói
        BigDecimal basePrice = resolveBasePrice(basePricing, packageType);

        // lấy con số counsellor mà admin sẽ thiết lập = số lượng mục tiêu tư vấn viên cho gói dịch vụ đang tạo
        int requestedCounsellors = request.getFeatureData().getMaxCounsellors() == null ? 0 : request.getFeatureData().getMaxCounsellors();

        // xd mỗi loại gói sẽ kèm theo
        int includedCounsellors = switch (packageType) {
            case TRIAL -> ((Number) packageQuotas.getOrDefault("trialCounsellor", 0)).intValue();
            case STANDARD -> ((Number) packageQuotas.getOrDefault("standardCounsellor", 0)).intValue();
            case ENTERPRISE -> ((Number) packageQuotas.getOrDefault("enterpriseCounsellor", 0)).intValue();
        };

        // step xử lý policy đối vs gói dùng thử
        validateTrialPolicy(packageType, request);

        //step xử lý khách hàng chỉ chi trả cho phần ngưỡng
        BigDecimal extraCounsellorSlotTotal = calculateExtraCounsellorTotal(packageType, requestedCounsellors, includedCounsellors, featureUnitPricing);

        //bước tính tổng giá nền + giá cho quota counsellor vượt mức 
        BigDecimal netPrice = basePrice.add(extraCounsellorSlotTotal);

        //hệ thống tự động cộng dồn giá của các tính năng
        // mà Admin đã "tick chọn" cho gói đó vào tổng giá trị cuối cùng.
        netPrice = applyAddonFees(netPrice, request, featureUnitPricing);

        // lấy số cấu hình tỷ lệ ra ==> thuế VAT
        BigDecimal taxRate = getAsBigDecimal(business.get("taxRate"), "tỉ lệ thuế xuất");

        // lấy số cấu hình tỷ lệ phí dịch vụ hệ thống
        BigDecimal serviceRate = getAsBigDecimal(business.get("serviceRate"), "tỉ lệ phí dịch vụ");

        // tính phí dịch vụ = gói giá gốc * tỉ lệ phí dịch vụ
        // vd phí dịch vụ gói A = 1.000.000 * 2% = 20.000 VND
        BigDecimal serviceFee = netPrice.multiply(serviceRate);

        // tính tiền thuế / thuế trên tổng giá trị
        // thuế 10% ==> (tiền giá gói + tiền phí dịch vụ) * tỉ lệ % thuế
        // 1.020.000 * 0.1 = 120.000 vnd
        BigDecimal taxAmount = netPrice.add(serviceFee).multiply(taxRate);

        //Cộng tất cả các thành phần lại để ra con số mà khách hàng thấy trên màn hình sẽ trả
        BigDecimal finalAmountRaw = netPrice.add(serviceFee).add(taxAmount);

        //Tổng tiền và làm tròn đến hàng nghìn
        BigDecimal finalPrice = finalAmountRaw
                .divide(new BigDecimal("1000"), 0, RoundingMode.CEILING)
                .multiply(new BigDecimal("1000"));

        return new SubscriptionPriceBreakdown(
                netPrice.setScale(0, RoundingMode.HALF_UP),
                serviceFee.setScale(0, RoundingMode.HALF_UP),
                taxAmount.setScale(0, RoundingMode.HALF_UP),
                finalPrice
        );
    }

    private static SupportLevel parseSupportLevel(String supportLevel) {
        try {
            return SupportLevel.valueOf(supportLevel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Loại hỗ trợ không hợp lệ: " + supportLevel);
        }
    }

    private static PackageType parsePackageType(String packageType) {
        try {
            return PackageType.valueOf(packageType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Loại gói không hợp lệ: " + packageType);
        }
    }

    // tùy vào lọai gói sẽ có tương ứng giá khởi điểm
    private static BigDecimal resolveBasePrice(Map<String, Object> basePricing, PackageType packageType) {
        String basePriceKey = switch (packageType) {
            case TRIAL -> "trial";
            case STANDARD -> "standard";
            case ENTERPRISE -> "enterprise";
        };

        return getAsBigDecimal(basePricing.get(basePriceKey), "giá nền " + basePriceKey);
    }

    private static void validatePricingInput(UpsertServicePackageFeeRequest request, Map<String, Object> business) {
        if (request == null) {
            throw new RuntimeException("Thiếu dữ liệu yêu cầu");
        }
        if (business == null) {
            throw new RuntimeException("Thiếu cấu hình doanh nghiệp");
        }
        if (request.getPackageType() == null || request.getPackageType().isBlank()) {
            throw new RuntimeException("Thiếu loại gói");
        }
        if (request.getFeatureData() == null) {
            throw new RuntimeException("Thiếu tính năng");
        }
    }

    private static Map<String, Object> getPricingConfig(Map<String, Object> business) {

        Object subscriptionPricingObj = business.get("subscriptionPricing");

        if (!(subscriptionPricingObj instanceof Map<?, ?> subscriptionPricingRaw)) {
            throw new RuntimeException("Thiếu cấu hình định giá subscription");
        }

        Map<String, Object> subscriptionPricing = (Map<String, Object>) subscriptionPricingRaw;
        Object basePricingObj = subscriptionPricing.get("basePrices");
        Object featureUnitPricingObj = subscriptionPricing.get("featureUnitPrices");
        Object packageQuotasObj = subscriptionPricing.get("PackageQuotas");

        if (!(packageQuotasObj instanceof Map<?, ?>)) {
            packageQuotasObj = subscriptionPricing.get("packageQuotas");
        }

        if (!(basePricingObj instanceof Map<?, ?> basePricingRaw)) {
            throw new RuntimeException("Thiếu hoặc sai cấu hình giá nền");
        }

        if (!(featureUnitPricingObj instanceof Map<?, ?> featureUnitPricingRaw)) {
            throw new RuntimeException("Thiếu hoặc sai cấu hình đơn giá tính năng");
        }

        if (!(packageQuotasObj instanceof Map<?, ?> packageQuotasRaw)) {
            throw new RuntimeException("Thiếu hoặc sai cấu hình định mức gói");
        }

        return Map.of("basePricing", (Map<String, Object>) basePricingRaw, "featureUnitPricing", (Map<String, Object>) featureUnitPricingRaw, "packageQuotas", (Map<String, Object>) packageQuotasRaw);
    }

    //validate policy của gói dùng thử ==> ko hỗ trợ add-on trả phí các tính năng trả tiền 
    private static void validateTrialPolicy(PackageType packageType, UpsertServicePackageFeeRequest request) {
        if (packageType != PackageType.TRIAL) {
            return;
        }

        boolean hasAi = Boolean.TRUE.equals(request.getFeatureData().getHasAiAssistant());
        boolean hasTopRanking = request.getFeatureData().getTopRanking() != null && request.getFeatureData().getTopRanking() > 0;
        boolean hasPremiumSupport = request.getFeatureData().getSupportLevel() != null && parseSupportLevel(request.getFeatureData().getSupportLevel()) == SupportLevel.PREMIUM_SUPPORT;

        if (hasAi || hasTopRanking || hasPremiumSupport) {
            throw new RuntimeException("Gói dùng thử không hỗ trợ add-on trả phí");
        }
    }

    //tính tổng phí cho quota counsellor vượt mức
    //Cái gì khách được hưởng miễn phí theo gói, và cái gì khách phải móc hầu bao trả thêm.
    private static BigDecimal calculateExtraCounsellorTotal(PackageType packageType,
                                                            int requestedCounsellors,
                                                            int includedCounsellors,
                                                            Map<String, Object> featureUnitPricing) {
        // đối vs gói thử ==> ko đ phí
        if (packageType == PackageType.TRIAL) {
            return BigDecimal.ZERO;
        }

        //xử lý bài toán sài hao
        //includedCounsellors : Hạn mức được tặng
        //requestedCounsellors : Nhu cầu thực tế
        //khách muốn mua 10 --> gói cho 5 --> dư 5
        //Khách hàng chỉ trả tiền cho phần "vượt ngưỡng" (ExtraSlots) ==> mô hình kinh doanh Zoom hay Google Workspace
        int extraSlots = Math.max(0, requestedCounsellors - includedCounsellors);

        BigDecimal unitPrice = getAsBigDecimal(featureUnitPricing.get("extraCounsellorSlot"), "phí counsellor vượt mức");

        return unitPrice.multiply(BigDecimal.valueOf(extraSlots));
    }

    private static BigDecimal calculateExtraPostTotal(PackageType packageType,
                                                      int requestedPosts,
                                                      int includedPosts,
                                                      Map<String, Object> featureUnitPricing) {

        // 1. Nếu là Enterprise hoặc được thiết lập Vô hạn (-1), phí phụ trội bằng 0
        if (packageType == PackageType.TRIAL || includedPosts == -1 || requestedPosts == -1) {
            return BigDecimal.ZERO;
        }

        // 2. Tính số lượng bài đăng vượt định mức (chỉ áp dụng cho gói có giới hạn hữu hạn)
        int extraPosts = Math.max(0, requestedPosts - includedPosts);
        if (extraPosts == 0) return BigDecimal.ZERO;

        // 3. Lấy đơn giá mỗi bài đăng thêm
        BigDecimal unitPrice = getAsBigDecimal(featureUnitPricing.getOrDefault("extraPostFee", 0), "phí bài đăng vượt mức");

        return unitPrice.multiply(BigDecimal.valueOf(extraPosts));
    }

    //Ước tính giá gói ==> để đưa giá cuối sau khi lựa chọn các tính năng
    // ==> tùy thuộc apply tính năng
    private static BigDecimal applyAddonFees(BigDecimal netPrice, UpsertServicePackageFeeRequest request, Map<String, Object> featureUnitPricing) {

        BigDecimal total = netPrice;

        if (Boolean.TRUE.equals(request.getFeatureData().getHasAiAssistant())) {
            total = total.add(getAsBigDecimal(featureUnitPricing.get("aiChatbotMonthlyFee"), "phí trợ lý AI"));
        }

        if (request.getFeatureData().getTopRanking() != null && request.getFeatureData().getTopRanking() > 0) {
            total = total.add(getAsBigDecimal(featureUnitPricing.get("topRankingFee"), "phí top ranking"));
        }

        if (request.getFeatureData().getSupportLevel() != null && parseSupportLevel(request.getFeatureData().getSupportLevel()) == SupportLevel.PREMIUM_SUPPORT) {
            total = total.add(getAsBigDecimal(featureUnitPricing.get("premiumSupportFee"), "phí hỗ trợ cao cấp"));
        }

        return netPrice;
    }

    private static BigDecimal getAsBigDecimal(Object value, String label) {
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        throw new RuntimeException("Thiếu hoặc sai định dạng cấu hình: " + label);
    }
}
