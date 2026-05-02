package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.PackageType;
import com.sp26se041.edubridgehcm.enums.SupportLevel;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.validations.admin.SubscriptionValidation;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class ConfigSystemUtil {

    public static String normalizeExtension(String ext) {
        if (ext == null) {
            return "";
        }
        String normalized = ext.trim().toLowerCase();
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    public static String validateUrlFormat(Map<String, Object> mediaConfig, String url, boolean isImage) {
        if (url == null || url.isBlank()) {
            return null;
        }

        if (mediaConfig == null) {
            return "Cấu hình media không tồn tại";
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

            String fileExt = normalizeExtension(cleanUrl.substring(cleanUrl.lastIndexOf(".") + 1));

            boolean isValid = allowedFormats.stream().map(f -> normalizeExtension(f.get("format"))).anyMatch(ext -> ext.equals(fileExt));

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
            throw new RuntimeException("Không tìm thấy cấu hình media");
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

        if (allowedFormats == null || allowedFormats.isEmpty()) {
            throw new RuntimeException("Chưa cấu hình định dạng cho phép!");
        }

        List<String> formatList = allowedFormats.stream().map(m -> normalizeExtension(m.get("format"))).toList();

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new RuntimeException("Tên file không hợp lệ!");
        }

        String fileExt = normalizeExtension(originalName.substring(originalName.lastIndexOf(".") + 1));

        boolean isValid = formatList.stream().anyMatch(ext -> ext.equals(fileExt));

        if (!isValid) {
            throw new RuntimeException("Định dạng " + fileExt + " không được hỗ trợ cho " + (isImage ? "ảnh" : "tài liệu"));
        }

        return null;
    }

    //Tách kết quả tính giá để lưu đủ net / phí dịch vụ / thuế / giá cuối lên DB.
    public record SubscriptionPriceBreakdown(BigDecimal basePrice,
                                             BigDecimal totalFeatureAmount,
                                             BigDecimal netPrice,
                                             BigDecimal serviceFee,
                                             BigDecimal taxFee,
                                             BigDecimal finalPrice) {
    }

    //Tờ hóa đơn chi tiết
    public static SubscriptionPriceBreakdown calculateSubscriptionPriceBreakdown(UpsertServicePackageFeeRequest request, Map<String, Object> business) {
        SubscriptionValidation.validatePricingInput(request, business);

        Map<String, Object> pricingConfig = getPricingConfig(business);
        Map<String, Object> basePricing = (Map<String, Object>) pricingConfig.get("basePricing");
        Map<String, Object> featureUnitPricing = (Map<String, Object>) pricingConfig.get("featureUnitPricing");
        Map<String, Object> packageQuotas = (Map<String, Object>) pricingConfig.get("packageQuotas");

        PackageType packageType = SubscriptionValidation.parsePackageType(request.getPackageType());

        //bước xác định giá nền (gốc) của từng gói
        BigDecimal basePrice = resolveBasePrice(basePricing, packageType);

        BigDecimal totalFeatureAmount = calculateTotalAddonFees(request, featureUnitPricing);

        //nit price = Giá nền + Tổng phí tính năng phụ
        BigDecimal netPrice = basePrice.add(totalFeatureAmount);

        // step xử lý policy đối vs gói dùng thử
        SubscriptionValidation.validateTrialPackage(request, business);

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
        BigDecimal finalPrice = finalAmountRaw.divide(new BigDecimal("1000"), 0, RoundingMode.CEILING).multiply(new BigDecimal("1000"));

        return new SubscriptionPriceBreakdown(
                basePrice.setScale(0, RoundingMode.HALF_UP),
                totalFeatureAmount.setScale(0, RoundingMode.HALF_UP),
                netPrice.setScale(0, RoundingMode.HALF_UP),
                serviceFee.setScale(0, RoundingMode.HALF_UP),
                taxAmount.setScale(0, RoundingMode.HALF_UP),
                finalPrice);
    }

    //Ước tính giá gói ==> để đưa giá cuối sau khi lựa chọn các tính năng
    // ==> tùy thuộc apply tính năng
    private static BigDecimal calculateTotalAddonFees(UpsertServicePackageFeeRequest request, Map<String, Object> featureUnitPricing) {

        BigDecimal total = BigDecimal.ZERO;
        if (request.getFeatureData() == null) return total;

        if (Boolean.TRUE.equals(request.getFeatureData().getHasAiAssistant())) {
            total = total.add(getAsBigDecimal(featureUnitPricing.get("aiChatbotMonthlyFee"), "phí trợ lý AI"));
        }

        if (request.getFeatureData().getSupportLevel() != null) {
            SupportLevel level = SubscriptionValidation.parseSupportLevel(request.getFeatureData().getSupportLevel());
            if (level == SupportLevel.PREMIUM_SUPPORT) {
                total = total.add(getAsBigDecimal(featureUnitPricing.getOrDefault("premiumSupportFee", 0), "phí hỗ trợ cao cấp"));
            }
        }

        return total;
    }

    public static Map<String, Object> getPricingConfig(Map<String, Object> business) {

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

    // tùy vào lọai gói sẽ có tương ứng giá khởi điểm
    public static BigDecimal resolveBasePrice(Map<String, Object> basePricing, PackageType packageType) {
        String basePriceKey = switch (packageType) {
            case TRIAL -> "trial";
            case STANDARD -> "standard";
            case ENTERPRISE -> "enterprise";
        };

        return getAsBigDecimal(basePricing.get(basePriceKey), "giá nền " + basePriceKey);
    }

    private static BigDecimal getAsBigDecimal(Object value, String label) {
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        throw new RuntimeException("Thiếu hoặc sai định dạng cấu hình: " + label);
    }
}
