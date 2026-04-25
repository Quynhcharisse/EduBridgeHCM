package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignCounsellorIntoSlotsRequest {

    List<Integer> templateIds;

    List<Integer> counsellorIds;

    List<Integer> slotIds;

    Integer campaignId;

    LocalDate startDate;

    LocalDate endDate;

    String action;
}
