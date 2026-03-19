package com.sp26se041.edubridgehcm.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurriculumType {

    NATIONAL("national"), //Tập trung vào các môn văn hóa để thi tốt nghiệp THPT và xét tuyển đại học trong nước.
    INTERNATIONAL("international"), //Không học theo sách giáo khoa của Bộ GD&ĐT Việt Nam (ngoại trừ các môn bắt buộc cho người Việt như Ngữ văn, Lịch sử, Địa lý được tinh giản).
    INTEGRATED("integrated"); //Kết hợp giữa chương trình MoET và một chương trình quốc tế (thường là Cambridge - Anh, hoặc các khung của Mỹ, Úc)

    private final String value;
}
