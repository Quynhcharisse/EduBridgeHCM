package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCampusConfigRequest {

    String policyDetail;

    String overview;

    List<CreateFacilityTemplateRequest.FacilityItem> itemList;

    CreateFacilityTemplateRequest.ImageJsonData imageJsonData;
}

