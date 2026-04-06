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

    Integer templateId;

    Integer campusId;

    List<Integer> counsellorIds; // Danh sách ID các counsellor muốn có trong slot này

    LocalDate startDate;

    LocalDate endDate;

    String action;
}
