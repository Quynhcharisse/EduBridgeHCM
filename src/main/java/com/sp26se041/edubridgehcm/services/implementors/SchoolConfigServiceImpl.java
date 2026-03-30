package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.repositories.SchoolConfigRepo;
import com.sp26se041.edubridgehcm.requests.CreateFacilityTemplateRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolConfigService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolConfigServiceImpl implements SchoolConfigService {

    private final SchoolConfigRepo schoolConfigRepo;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createOrUpdateFacilityTemplate(int schoolId, CreateFacilityTemplateRequest request) {

        if (request == null || request.getOverview() == null || request.getItemList() == null || request.getItemList().isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid facility template data", null);
        }

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("coverUrl", request.getImageJsonData().getCoverUrl());

        imageMap.put("itemList", request.getImageJsonData().getItemList().stream()
                .map(item -> {
                    Map<String, Object> dataItem = new HashMap<>();
                    dataItem.put("name", item.getName());
                    dataItem.put("url", item.getUrl());
                    dataItem.put("altName", item.getAltName());
                    dataItem.put("uploadDate", item.getUploadDate());
                    dataItem.put("isUsage", item.getIsUsage());
                    return dataItem;
                })
                .collect(Collectors.toList())
        );

        Map<String, Object> facilityTemplate = new HashMap<>();

        facilityTemplate.put("overview", request.getOverview());
        facilityTemplate.put("itemList", request.getItemList().stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("facilityCode", item.getFacilityCode());
            itemMap.put("name", item.getName());
            itemMap.put("value", item.getValue());
            itemMap.put("unit", item.getUnit());
            itemMap.put("category", item.getCategory());
            return itemMap;
        }).collect(Collectors.toList()));

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "facility_template")
                .orElse(SchoolConfig.builder()
                        .schoolId(schoolId)
                        .key("facility_template")
                        .build());
        config.setValue(facilityTemplate);
        config.setUpdatedAt(LocalDateTime.now());
        schoolConfigRepo.save(config);
        return ResponseBuilder.build(HttpStatus.OK, "Facility template upsert successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getFacilityTemplate(int schoolId) {

        SchoolConfig config = schoolConfigRepo.findBySchoolIdAndKey(schoolId, "facility_template").orElse(null);

        if (config == null || config.getValue() == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No facility template found for this school", null);
        }
        return ResponseBuilder.build(HttpStatus.OK, "", config.getValue());
    }
}

