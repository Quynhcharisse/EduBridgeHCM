package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.requests.CreateConfigDataRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SystemService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final PlatformConfigRepo platformConfigRepo;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateConfigData(CreateConfigDataRequest request) {
        if (request.getBusinessData() == null || request.getMediaData() == null || request.getDesignData() == null || request.getReportData() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Data missing", null);
        }

        updateConfig(request);
        return ResponseBuilder.build(HttpStatus.OK, "Update successfully", null);
    }


    @Transactional
    public void updateConfig(CreateConfigDataRequest request) {
        updateBusiness(request);
        updateMedia(request);
        updateDesign(request);
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
        platformConfigRepo.save(config);
    }
}
