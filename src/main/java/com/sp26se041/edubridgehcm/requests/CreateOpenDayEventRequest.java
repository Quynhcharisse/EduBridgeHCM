package com.sp26se041.edubridgehcm.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOpenDayEventRequest {
     int campusId;
     String title;
     String description;
     String bannerUrl;
     LocalDate eventDate;
     LocalTime startTime;
     LocalTime endTime;
}
