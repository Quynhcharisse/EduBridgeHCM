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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        if (data == null) return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Data invalid", null);

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
                && request.getSubscriptionData() == null
                && request.getAdmissionQuotaData() == null
                && request.getAdmissionSettingsData() == null
        ) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Data missing", null);
        }

        updateConfig(request);
        return ResponseBuilder.build(HttpStatus.OK, "Update successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getQuotaByYear(String year) {

        PlatformConfig config = platformConfigRepo.findByKey("admission_quota").orElse(null);

        if (config == null || config.getValue() == null)
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No quota config found", null);

        Map<String, Object> allYearsData = (Map<String, Object>) config.getValue();

        Map<String, Object> yearData = (Map<String, Object>) allYearsData.get(year);

        if (yearData == null) return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No data for year " + year, null);

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
        if (request.getSubscriptionData() != null) updateSubscriptionPolicy(request);
        if (request.getAdmissionSettingsData() != null) updateAdmissionSettingsTemplate(request);
    }

    @Transactional
    public void updateBusiness(CreateConfigDataRequest request) {
        CreateConfigDataRequest.BusinessData businessData = request.getBusinessData();
        Map<String, Object> businessJson = new HashMap<>();

        businessJson.put("taxRate", businessData.getTaxRate());
        businessJson.put("serviceRate", businessData.getServiceRate());
        businessJson.put("minPay", businessData.getMinPay());
        businessJson.put("maxPay", businessData.getMaxPay());

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
        CreateConfigDataRequest.MediaData mediaData = request.getMediaData();
        Map<String, Object> mediaJson = new HashMap<>();

        mediaJson.put("maxImgSize", mediaData.getMaxImgSize());
        mediaJson.put("maxVideoSize", mediaData.getMaxVideoSize());
        mediaJson.put("maxDocSize", mediaData.getMaxDocSize());

        mediaJson.put("imgFormat", mapFormats(mediaData.getImgFormats()));
        mediaJson.put("videoFormat", mapFormats(mediaData.getVideoFormats()));
        mediaJson.put("docFormat", mapFormats(mediaData.getDocFormats()));

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

    @Transactional
    public void updateSubscriptionPolicy(CreateConfigDataRequest request) {
        CreateConfigDataRequest.SubscriptionData subData = request.getSubscriptionData();

        Map<String, Object> subJson = new HashMap<>();
        subJson.put("trialDays", subData.getTrialDays());
        subJson.put("gracePeriod", subData.getGracePeriod());
        subJson.put("minSubscriptionMonth", subData.getMinSubscriptionMonth());

        PlatformConfig config = platformConfigRepo.findByKey("subscriptionPolicy").orElse(
                PlatformConfig.builder()
                        .key("subscriptionPolicy")
                        .creationDate(LocalDateTime.now())
                        .build()
        );

        config.setValue(subJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Transactional
    public void updateAdmissionQuota(CreateConfigDataRequest request) {
        CreateConfigDataRequest.AdmissionQuotaData admissData = request.getAdmissionQuotaData();

        Map<String, Object> currentYearInfo = new HashMap<>();
        currentYearInfo.put("sourceUrl", admissData.getSourceUrl());

        Map<String, Integer> formattedQuotas = new HashMap<>();
        admissData.getQuotas().forEach((id, val) -> formattedQuotas.put(id.toString(), val));
        currentYearInfo.put("quotas", formattedQuotas);

        PlatformConfig config = platformConfigRepo.findByKey("admissionQuota")
                .orElse(PlatformConfig.builder()
                        .key("admissionQuota")
                        .creationDate(LocalDateTime.now())
                        .build());

        Map<String, Object> allYearsData = (config.getValue() != null)
                ? (Map<String, Object>) config.getValue()
                : new HashMap<>();

        allYearsData.put(admissData.getYear(), currentYearInfo);

        assert config != null;
        config.setValue(allYearsData);
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
