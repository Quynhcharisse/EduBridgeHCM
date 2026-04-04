package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SchoolConfigUtil {
    public static List<Map<String, Object>> mergeFacilityItems(List<Map<String, Object>> templateItems,
                                                               List<UpdateCampusConfigRequest.FacilityItemRequest> requestItems) {
        if (templateItems == null) templateItems = new ArrayList<>();
        if (requestItems == null) return templateItems;

        // 1. Chuyển request thành Map để tìm kiếm nhanh theo facilityCode
        Map<String, UpdateCampusConfigRequest.FacilityItemRequest> requestMap = requestItems.stream()
                .collect(Collectors.toMap(item -> item.getFacilityCode(), item -> item, (a, b) -> a));

        // 2. DUYỆT MẪU CỦA HQ: Cập nhật giá trị cho các mục đã có trong mẫu
        List<Map<String, Object>> mergedList = templateItems.stream().map(templateItem -> {
            String code = (String) templateItem.get("facilityCode");
            Map<String, Object> newItem = new HashMap<>(templateItem); // Clone mẫu

            if (requestMap.containsKey(code)) {
                UpdateCampusConfigRequest.FacilityItemRequest req = requestMap.get(code);
                newItem.put("value", req.getValue());
                newItem.put("unit", req.getUnit());
                newItem.put("isUsage", true);
                newItem.put("isCustom", false); // Mục này mượn từ HQ
            } else {
                newItem.put("isUsage", false);
                newItem.put("value", 0);
                newItem.put("isCustom", false);
            }
            return newItem;
        }).collect(Collectors.toList());

        // 3. XỬ LÝ THÊM MỚI: Tìm những item trong Request mà HQ KHÔNG CÓ
        Set<String> templateCodes = templateItems.stream()
                .map(t -> (String) t.get("facilityCode"))
                .collect(Collectors.toSet());

        List<Map<String, Object>> customItems = requestItems.stream()
                .filter(req -> !templateCodes.contains(req.getFacilityCode())) // Lọc ra đồ mới
                .map(req -> {
                    Map<String, Object> customItem = new HashMap<>();
                    customItem.put("facilityCode", req.getFacilityCode());
                    customItem.put("name", req.getName());
                    customItem.put("value", req.getValue());
                    customItem.put("unit", req.getUnit());
                    customItem.put("category", req.getCategory());
                    customItem.put("isUsage", true);
                    customItem.put("isCustom", true); // Đánh dấu đây là hàng tự thêm
                    return customItem;
                })
                .collect(Collectors.toList());

        mergedList.addAll(customItems);
        return mergedList;
    }

    public static String convertOperationToPolicyString(Map<String, Object> operationData) {

        if (operationData == null) return "Chưa có cấu hình vận hành từ cơ sở chính.";

        StringBuilder sb = new StringBuilder();
        sb.append("===== THÔNG TIN VẬN HÀNH CHUNG =====\n");
        sb.append("📞 Đường dây nóng: ").append(operationData.getOrDefault("hotline", "Không có")).append("\n");
        sb.append("📧 Email hỗ trợ: ").append(operationData.getOrDefault("emailSupport", "Không có")).append("\n\n");

        Map<String, Object> working = (Map<String, Object>) operationData.get("workingConfig");

        if (working != null) {
            sb.append("⏰ GIỜ LÀM VIỆC:\n");
            sb.append("- Ghi chú: ").append(working.getOrDefault("note", "Không có")).append("\n");
        }

        List<Map<String, Object>> shifts = (List<Map<String, Object>>) working.get("workShifts");

        if (shifts != null && !shifts.isEmpty()) {
            for (Map<String, Object> shift : shifts) {
                sb.append("  • ").append(shift.get("name"))
                        .append(": ").append(shift.get("startTime"))
                        .append(" - ").append(shift.get("endTime")).append("\n");
            }
        }

        List<String> regularDays = (List<String>) working.get("regularDays");
        if (regularDays != null && !regularDays.isEmpty()) {
            String daysVietnamese = regularDays.stream()
                    .map(SchoolConfigUtil::mapDayToVietnamese)
                    .collect(Collectors.joining(", "));
            sb.append("- Các ngày trong tuần: ").append(daysVietnamese).append("\n");
        }

        // Xử lý ngày cuối tuần
        List<String> weekendDays = (List<String>) working.get("weekendDays");
        if (weekendDays != null && !weekendDays.isEmpty()) {
            String weekendsVietnamese = weekendDays.stream()
                    .map(SchoolConfigUtil::mapDayToVietnamese)
                    .collect(Collectors.joining(", "));
            sb.append("- Ngày cuối tuần: ").append(weekendsVietnamese).append("\n");
        }

        boolean isOpenSunday = (boolean) working.getOrDefault("isOpenSunday", false);
        sb.append("- Mở cửa Chủ Nhật: ").append(isOpenSunday ? "Có" : "Nghỉ").append("\n\n");

        //Xử lý Quy trình nhập học (Các bước thực hiện)
        List<Map<String, Object>> steps = (List<Map<String, Object>>) operationData.get("admissionSteps");
        if (steps != null && !steps.isEmpty()) {
            sb.append("📝 QUY TRÌNH NHẬP HỌC:\n");
            // Sắp xếp các bước theo thứ tự
            steps.sort(Comparator.comparingInt(s -> (int) s.get("stepOrder")));

            for (Map<String, Object> step : steps) {
                sb.append("  Bước ").append(step.get("stepOrder")).append(". ")
                        .append(step.get("stepName")).append(": ")
                        .append(step.get("description")).append("\n");
            }
        }
        return sb.toString();
    }

    public static String mapDayToVietnamese(String day) {
        switch (day.toUpperCase()) {
            case "MON":
                return "Thứ Hai";
            case "TUE":
                return "Thứ Ba";
            case "WED":
                return "Thứ Tư";
            case "THU":
                return "Thứ Năm";
            case "FRI":
                return "Thứ Sáu";
            case "SAT":
                return "Thứ Bảy";
            case "SUN":
                return "Chủ Nhật";
            default:
                return day;
        }
    }
}

