package com.sp26se041.edubridgehcm.requests;

import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConsultationOfflineRequest {

    String phone;

    String question;

    LocalTime appointmentTime;

    LocalDate appointmentDate;

}
