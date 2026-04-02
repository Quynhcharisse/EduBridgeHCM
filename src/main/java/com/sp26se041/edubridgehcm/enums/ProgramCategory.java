package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProgramCategory {

    MOET("moet"),                           // Chương trình chuẩn của Bộ Giáo dục
    MOET_INTEGRATED("moet_integrated"),     // Chương trình Tích hợp (Bộ GD&ĐT + Tiếng Anh)
    CAMBRIDGE("cambridge"),                 // Chương trình quốc tế Cambridge (IGCSE, A-Level)
    IB("ib"),                               // Chương trình Tú tài Quốc tế (International Baccalaureate)
    AMERICAN_AP("american_ap"),             // Chương trình chuẩn Mỹ (Advanced Placement)
    OXFORD("oxford"),                       // Chương trình quốc tế Oxford
    VOCATIONAL_ORIENTED("vocational");      // Chương trình định hướng nghề nghiệp/du học

    private final String value;
}
