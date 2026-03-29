package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.requests.CreateFacilityTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (request.getBusinessData() == null || request.getMediaData() == null || request.getDesignData() == null || request.getReportData() == null) {
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

        // Load All Schools một lần để tối ưu hiệu năng (tránh N+1 query)
        Map<Integer, String> schoolNames = schoolRepo.findAll().stream()
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
        updateBusiness(request);
        updateMedia(request);
        updateDesign(request);
        updateAdmissionQuota(request);
        updateSubscriptionPolicy(request);
        updateReport(request);
    }

    @Transactional
    public void updateBusiness(CreateConfigDataRequest request) {
        CreateConfigDataRequest.BusinessData businessData = request.getBusinessData();
        Map<String, Object> businessJson = new HashMap<>();

        businessJson.put("taxRate", businessData.getTaxRate());
        businessJson.put("serviceRate", businessData.getServiceRate());
        businessJson.put("minPay", businessData.getMinPay());
        businessJson.put("maxPay", businessData.getMaxPay());

        PlatformConfig config = platformConfigRepo.findByKey("business").orElse(null);

        assert config != null;
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
        mediaJson.put("maxDesignRefImg", mediaData.getMaxDesignRefImg());
        mediaJson.put("maxFeedbackImg", mediaData.getMaxFeedbackImg());
        mediaJson.put("maxFeedbackVideo", mediaData.getMaxFeedbackVideo());
        mediaJson.put("maxReportImg", mediaData.getMaxReportImg());
        mediaJson.put("maxReportVideo", mediaData.getMaxReportVideo());

        List<Map<String, String>> imgFormats = mediaData.getImgFormats().stream()
                .map(format -> {
                    Map<String, String> f = new HashMap<>();
                    f.put("format", "." + format.getFormat());
                    return f;
                })
                .toList();
        mediaJson.put("imgFormat", imgFormats);

        List<Map<String, String>> videoFormats = mediaData.getVideoFormats().stream()
                .map(format -> {
                    Map<String, String> f = new HashMap<>();
                    f.put("format", "." + format.getFormat());
                    return f;
                })
                .toList();
        mediaJson.put("videoFormat", videoFormats);

        PlatformConfig config = platformConfigRepo.findByKey("media").orElse(null);
        assert config != null;
        config.setValue(mediaJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Transactional
    public void updateDesign(CreateConfigDataRequest request) {
        CreateConfigDataRequest.DesignData designData = request.getDesignData();
        Map<String, Object> designJson = new HashMap<>();

        designJson.put("illustrationImage", designData.getIllustrationImage());
        List<Map<String, String>> logoPos = designData.getPositions().stream()
                .map(position -> {
                    Map<String, String> p = new HashMap<>();
                    p.put("p", position.getPosition());
                    return p;
                })
                .toList();
        designJson.put("positions", logoPos);

        PlatformConfig config = platformConfigRepo.findByKey("design").orElse(null);
        assert config != null;
        config.setValue(designJson);
        platformConfigRepo.save(config);
    }

    @Transactional
    public void updateSubscriptionPolicy(CreateConfigDataRequest request) {
        CreateConfigDataRequest.SubscriptionData subData = request.getSubscriptionData();

        Map<String, Object> subJson = new HashMap<>();
        subJson.put("trialDays", subData.getTrialDays());
        subJson.put("gracePeriod", subData.getGracePeriod());
        subJson.put("taxRate", subData.getTaxRate());
        subJson.put("minSubscriptionMonth", subData.getMinSubscriptionMonth());

        PlatformConfig config = platformConfigRepo.findByKey("subscriptionPolicy").orElse(null);
        assert config != null;
        config.setValue(subJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Transactional
    public void updateAdmissionQuota(CreateConfigDataRequest request) {
        CreateConfigDataRequest.AdmissionQuotaData admissData = request.getAdmissionQuotaData();

        PlatformConfig config = platformConfigRepo.findByKey("admission_quota")
                .orElse(PlatformConfig.builder()
                        .key("admission_quota")
                        .creationDate(LocalDateTime.now())
                        .build());

        Map<String, Object> allYearsData = (config.getValue() != null)
                ? (Map<String, Object>) config.getValue()
                : new HashMap<>();

        Map<String, Object> currentYearInfo = new HashMap<>();
        currentYearInfo.put("sourceUrl", admissData.getSourceUrl());

        Map<String, Integer> formattedQuotas = new HashMap<>();
        admissData.getQuotas().forEach((id, val) -> formattedQuotas.put(id.toString(), val));
        currentYearInfo.put("quotas", formattedQuotas);

        allYearsData.put(admissData.getYear(), currentYearInfo);

        config.setValue(allYearsData);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Transactional
    public void updateReport(CreateConfigDataRequest request) {
        CreateConfigDataRequest.ReportData reportData = request.getReportData();
        Map<String, Object> reportJson = new HashMap<>();

        reportJson.put("maxDisbursementDay", reportData.getMaxDisbursementDay());
        List<Map<String, String>> severityLevels = reportData.getLevels().stream()
                .map(level -> {
                    Map<String, String> l = new HashMap<>();
                    l.put("name", level.getName());
                    l.put("compensation", level.getCompensation());
                    return l;
                })
                .toList();
        reportJson.put("severityLevels", severityLevels);

        PlatformConfig config = platformConfigRepo.findByKey("report").orElse(null);
        assert config != null;
        config.setValue(reportJson);
        config.setModifiedDate(LocalDateTime.now());
        platformConfigRepo.save(config);
    }

    @Override
    public ResponseEntity<ResponseObject> createFacilityTemplate(CreateFacilityTemplateRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> getFacilityTemplate() {
        return null;
    }

}
