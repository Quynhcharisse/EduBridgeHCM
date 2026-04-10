package com.sp26se041.edubridgehcm.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCampusRequest {

    String email;

    String name;

    String address;

    String phone;

    String city;

    String district;

    String ward;

    String boardingType;

    Double latitude;

    Double longitude;
}
