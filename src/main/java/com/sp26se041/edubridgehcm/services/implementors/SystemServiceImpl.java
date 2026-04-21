package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final PlatformConfigRepo platformConfigRepo;

    private final SchoolRepo schoolRepo;

    @Override
    public ResponseEntity<ResponseObject> getConfigData() {

        List<PlatformConfig> platformConfigList = platformConfigRepo.findAll();

        Map<String, Object> data = new HashMap<>();

        for (PlatformConfig config : platformConfigList) {
            String key = config.getKey();
            Map<String, Object> value = (Map<String, Object>) config.getValue();
            data.put(key, value);
        }

        return ResponseBuilder.build(HttpStatus.OK, "", data);
    }

    @Override
    public ResponseEntity<ResponseObject> getConfigDataByKey(String k) {

        Map<String, Object> data = getConfigByKey(k);

        if (data == null) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu không hợp lệ",
                    null
            );
        }

        return ResponseBuilder.build(HttpStatus.OK, "", data);
    }

    private Map<String, Object> getConfigByKey(String key) {

        PlatformConfig config = platformConfigRepo.findByKey(key).orElse(null);

        if (config == null) return null;

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> value = (Map<String, Object>) config.getValue();
        data.put(key, value);

        return data;
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateConfigData(CreateConfigDataRequest request) {

        if (request.getBusinessData() == null
                && request.getMediaData() == null
                && request.getAdmissionQuotaData() == null
                && request.getAdmissionSettingsData() == null
        ) {
            return ResponseBuilder.build(
                    HttpStatus.BAD_REQUEST,
                    "Thiếu dữ liệu",
                    null
            );
        }

        updateConfig(request);
        return ResponseBuilder.build(
                HttpStatus.OK,
                "Cập nhật thành công",
                null
        );
    }

    @Override
    public ResponseEntity<ResponseObject> getQuotaByYear(String year) {
        PlatformConfig config = platformConfigRepo.findByKey("admissionQuota").orElse(null);

        if (config == null || config.getValue() == null)
            return ResponseBuilder.build(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy cấu hình chỉ tiêu",
                    null
            );

        Map<String, Object> allYearsData = (Map<String, Object>) config.getValue();

        Map<String, Object> yearData = (Map<String, Object>) allYearsData.get(year);

        if (yearData == null)
            return ResponseBuilder.build(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy dữ liệu cho năm " + year,
                    null
            );

        Map<String, Integer> idQuotas = (Map<String, Integer>) yearData.get("quotas");

        Map<String, Integer> displayQuotas = new HashMap<>();

        Set<Integer> targetIds = idQuotas.keySet().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

        Map<Integer, String> schoolNames = schoolRepo.findAllById(targetIds).stream()
                .collect(Collectors.toMap(School::getId, School::getName));

        idQuotas.forEach((id, val) -> {
            String name = schoolNames.get(Integer.parseInt(id)); // chuyển id là string thành id để dò tương ứng id đó sẽ có name là gì?
            displayQuotas.put(name, val);
        });

        Map<String, Object> response = new HashMap<>(yearData);
        response.put("quotas", displayQuotas);

        return ResponseBuilder.build(HttpStatus.OK, "", response);
    }

    @Transactional
    public void updateConfig(CreateConfigDataRequest request) {
        if (request.getBusinessData() != null) updateBusiness(request);
        if (request.getMediaData() != null) updateMedia(request);
        if (request.getAdmissionQuotaData() != null) updateAdmissionQuota(request);
        if (request.getAdmissionSettingsData() != null) updateAdmissionSettingsTemplate(request);
    }

    @Transactional
    public void updateBusiness(CreateConfigDataRequest request) {
        CreateConfigDataRequest.BusinessData business = request.getBusinessData();
        Map<String, Object> businessJson = new HashMap<>();

        businessJson.put("taxRate", business.getTaxRate());
        businessJson.put("serviceRate", business.getServiceRate());
        businessJson.put("minPay", business.getMinPay());
        businessJson.put("maxPay", business.getMaxPay());

        Map<String, Object> basePrices = new HashMap<>();
        basePrices.put("trial", business.getSubscriptionPricing().getBasePrices().getTrial());
        basePrices.put("standard", business.getSubscriptionPricing().getBasePrices().getStandard());
        basePrices.put("enterprise", business.getSubscriptionPricing().getBasePrices().getEnterprise());

        Map<String, Object> featureUnitPrices = new HashMap<>();
        featureUnitPrices.put("extraCounsellorSlot", business.getSubscriptionPricing().getFeatureUnitPrices().getExtraCounsellorSlot());
        featureUnitPrices.put("aiChatbotMonthlyFee", business.getSubscriptionPricing().getFeatureUnitPrices().getAiChatbotMonthlyFee());
        featureUnitPrices.put("premiumSupportFee", business.getSubscriptionPricing().getFeatureUnitPrices().getPremiumSupportFee());
        featureUnitPrices.put("topRankingFee", business.getSubscriptionPricing().getFeatureUnitPrices().getTopRankingFee());

        Map<String, Object> packageQuotas = new HashMap<>();
        packageQuotas.put("durationDays", business.getSubscriptionPricing().getPackageQuotas().getDurationDays());
        packageQuotas.put("trialCounsellor", business.getSubscriptionPricing().getPackageQuotas().getTrialCounsellor());
        packageQuotas.put("standardCounsellor", business.getSubscriptionPricing().getPackageQuotas().getStandardCounsellor());
        packageQuotas.put("enterpriseCounsellor", business.getSubscriptionPricing().getPackageQuotas().getEnterpriseCounsellor());

        Map<String, Object> subscriptionPricing = new HashMap<>();
        subscriptionPricing.put("basePrices", basePrices);
        subscriptionPricing.put("featureUnitPrices", featureUnitPrices);
        subscriptionPricing.put("packageQuotas", packageQuotas);

        businessJson.put("subscriptionPricing", subscriptionPricing);

        PlatformConfig config = platformConfigRepo.findByKey("business").orElse(
                PlatformConfig.builder()
                        .key("business")
                        .creationDate(LocalDateTime.now())
                        .build()
        );

        config.setValue(businessJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Transactional
    public void updateMedia(CreateConfigDataRequest request) {
        CreateConfigDataRequest.MediaData media = request.getMediaData();

        validateMediaConfigRequest(media);

        Map<String, Object> mediaJson = new HashMap<>();

        mediaJson.put("maxImgSize", media.getMaxImgSize());
        mediaJson.put("maxDocSize", media.getMaxDocSize());

        mediaJson.put("imgFormat", mapFormats(media.getImgFormats()));
        mediaJson.put("docFormat", mapFormats(media.getDocFormats()));

        PlatformConfig config = platformConfigRepo.findByKey("media").orElse(
                PlatformConfig.builder()
                        .key("media")
                        .creationDate(LocalDateTime.now())
                        .build()
        );

        config.setValue(mediaJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    private List<Map<String, String>> mapFormats(List<CreateConfigDataRequest.MediaFormat> formats) {
        if (formats == null) return Collections.emptyList();

        return formats.stream()
                .map(f -> {
                    String ext = f.getFormat().trim().toLowerCase();
                    // Nếu Admin quên nhập dấu chấm, mình sẽ tự thêm vào (ví dụ: "pdf" -> ".pdf")
                    if (!ext.startsWith(".")) {
                        ext = "." + ext;
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put("format", ext);
                    return map;
                })
                .toList();
    }

    private void validateMediaConfigRequest(CreateConfigDataRequest.MediaData media) {
        if (media == null) {
            throw new RuntimeException("Dữ liệu cấu hình không được để trống");
        }

        if (media.getMaxImgSize() <= 0 || media.getMaxImgSize() > 100) {
            throw new RuntimeException("Dung lượng ảnh tối đa phải từ 1MB đến 100MB");
        }
        if (media.getMaxDocSize() <= 0 || media.getMaxDocSize() > 100) {
            throw new RuntimeException("Dung lượng tài liệu tối đa phải từ 1MB đến 100MB");
        }

        if (media.getImgFormats() == null || media.getImgFormats().isEmpty()) {
            throw new RuntimeException("Danh sách định dạng ảnh không được để trống");
        }
        if (media.getDocFormats() == null || media.getDocFormats().isEmpty()) {
            throw new RuntimeException("Danh sách định dạng tài liệu không được để trống");
        }

        validateFormatStrings(media.getImgFormats(), "Ảnh");
        validateFormatStrings(media.getDocFormats(), "Tài liệu");
    }

    private void validateFormatStrings(List<CreateConfigDataRequest.MediaFormat> formats, String typeLabel) {
        for (CreateConfigDataRequest.MediaFormat format : formats) {
            if (format.getFormat() == null) {
                throw new RuntimeException("Định dạng " + typeLabel + " không được để trống");
            }

            String fmt = format.getFormat().trim();
            if (fmt.isEmpty() || !fmt.matches("^[a-zA-Z0-9.]+$")) {
                throw new RuntimeException("Định dạng " + typeLabel + " '" + fmt + "' không hợp lệ");
            }
        }
    }

    @Transactional
    public void updateAdmissionQuota(CreateConfigDataRequest request) {
        CreateConfigDataRequest.AdmissionQuotaData admissionQuota = request.getAdmissionQuotaData();

        List<Map<String, Object>> quotaAIData = admissionQuota.getQuotas().stream()
                .filter(q -> schoolRepo.existsByName(q.getSchoolName()))
                .map(q -> {
                    School school = schoolRepo.findByName(q.getSchoolName()).orElse(null);
                    assert school != null;

                    Map<String, Object> data = new HashMap<>();
                    data.put("schoolId", school.getId());
                    data.put("value", q.getValue());
                    return data;
                })
                .toList();

        Map<String, Object> quotaData = new HashMap<>();
        quotaData.put("year", admissionQuota.getYear());
        quotaData.put("quotas", quotaAIData);

        PlatformConfig config = platformConfigRepo.findByKey("admissionQuota")
                .orElse(PlatformConfig.builder()
                        .key("admissionQuota")
                        .creationDate(LocalDateTime.now())
                        .build());

        assert config != null;
        config.setValue(quotaData);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Transactional
    public void updateAdmissionSettingsTemplate(CreateConfigDataRequest request) {
        CreateConfigDataRequest.AdmissionSettingsData admissionSettingsData = request.getAdmissionSettingsData();

        List<Map<String, Object>> allowedMethodsJson = new ArrayList<>();
        if (admissionSettingsData.getAllowedMethods() != null) {
            allowedMethodsJson = admissionSettingsData.getAllowedMethods().stream()
                    .map(method -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("code", method.getCode());
                        row.put("displayName", method.getDisplayName());
                        row.put("description", method.getDescription());
                        return row;
                    })
                    .collect(Collectors.toList());
        }

        Map<String, Object> admissionJson = new HashMap<>();
        admissionJson.put("allowedMethods", allowedMethodsJson);

        PlatformConfig config = platformConfigRepo.findByKey("admissionSettingsData").orElse(
                PlatformConfig.builder()
                        .key("admissionSettingsData")
                        .creationDate(LocalDateTime.now())
                        .build()
        );

        config.setValue(admissionJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }
}
