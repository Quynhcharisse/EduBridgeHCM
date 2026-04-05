package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SchoolConfigUtil {
    public static List<Map<String, Object>> mergeFacilityItems(List<Map<String, Object>> templateItems, List<UpdateCampusConfigRequest.FacilityItemRequest> requestItems) {
        if (templateItems == null) templateItems = new ArrayList<>();
        if (requestItems == null) return templateItems;

        // 1. Chuyển request thành Map để tìm kiếm nhanh theo facilityCode
        Map<String, UpdateCampusConfigRequest.FacilityItemRequest> requestMap = requestItems.stream().collect(Collectors.toMap(item -> item.getFacilityCode(), item -> item, (a, b) -> a));

        // 2. DUYỆT MẪU CỦA HQ: Cập nhật giá trị cho các mục đã có trong mẫu
        List<Map<String, Object>> mergedList = templateItems.stream().map(templateItem -> {
            String code = (String) templateItem.get("facilityCode");
            Map<String, Object> newItem = new HashMap<>(templateItem); // Clone mẫu

            if (requestMap.containsKey(code)) {
                UpdateCampusConfigRequest.FacilityItemRequest req = requestMap.get(code);
                newItem.put("value", req.getValue());
                newItem.put("unit", req.getUnit());
                newItem.put("isUsage", true);
                newItem.put("isCustom", false);
            } else {
                newItem.put("isUsage", false);
                newItem.put("value", 0);
                newItem.put("isCustom", false);
            }
            return newItem;
        }).collect(Collectors.toList());

        // 3. XỬ LÝ THÊM MỚI: Tìm những item trong Request mà HQ KHÔNG CÓ
        Set<String> templateCodes = templateItems.stream().map(t -> (String) t.get("facilityCode")).collect(Collectors.toSet());

        List<Map<String, Object>> customItems = requestItems.stream().filter(req -> !templateCodes.contains(req.getFacilityCode())) // Lọc ra đồ mới
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
                }).collect(Collectors.toList());

        mergedList.addAll(customItems);
        return mergedList;
    }

    public static Map<String, Object> mergeOperationConfig(Map<String, Object> hqData, UpdateCampusConfigRequest request) {
        if (hqData == null) return new HashMap<>();

        Map<String, Object> merged = new HashMap<>(hqData);

        if (request.getHotline() != null) {
            merged.put("hotline", request.getHotline());
        }

        if (request.getEmailSupport() != null) {
            merged.put("emailSupport", request.getEmailSupport());
        }

        if (request.getMinCounsellorPerSlot() != null) {
            merged.put("minCounsellorPerSlot", request.getMinCounsellorPerSlot());
        }

        if (request.getSlotDurationInMinutes() != null) {
            merged.put("slotDurationInMinutes", request.getSlotDurationInMinutes());
        }
        if (request.getMaxBookingPerSlot() != null) {
            merged.put("maxBookingPerSlot", request.getMaxBookingPerSlot());
        }
        if (request.getAllowBookingBeforeHours() != null) {
            merged.put("allowBookingBeforeHours", request.getAllowBookingBeforeHours());
        }

        if (request.getWorkingOverride() != null) {
            merged.put("workingConfig", mergeWorkingConfig(
                    (Map<String, Object>) hqData.get("workingConfig"),
                    request.getWorkingOverride()
            ));
        }

        if (request.getAdmissionStepsOverride() != null) {
            merged.put("admissionSteps", mergeAdmissionSteps(
                    (List<Map<String, Object>>) hqData.get("admissionSteps"),
                    request.getAdmissionStepsOverride()
            ));
        }
        return merged;
    }

    private static Map<String, Object> mergeWorkingConfig(Map<String, Object> hqWorking,
                                                          UpdateCampusConfigRequest.CampusWorkingOverride override) {

        Map<String, Object> mergedWorking = (hqWorking != null) ? new HashMap<>(hqWorking) : new HashMap<>();

        if (override.getNote() != null) mergedWorking.put("note", override.getNote());
        if (override.getIsOpenSunday() != null) mergedWorking.put("isOpenSunday", override.getIsOpenSunday());

        // Nếu Campus gửi danh sách ca làm việc mới, dùng cái đó
        if (override.getWorkShifts() != null && !override.getWorkShifts().isEmpty()) {
            List<Map<String, Object>> shiftMaps = override.getWorkShifts().stream().map(shift -> {
                Map<String, Object> m = new HashMap<>();
                m.put("name", shift.getName());
                m.put("startTime", shift.getStartTime());
                m.put("endTime", shift.getEndTime());
                return m;
            }).collect(Collectors.toList());

            mergedWorking.put("workShifts", shiftMaps);
        }

        return mergedWorking;
    }

    private static List<Map<String, Object>> mergeAdmissionSteps(List<Map<String, Object>> hqSteps,
                                                                 List<UpdateCampusConfigRequest.AdmissionStepOverride> overrides) {

        if (hqSteps == null) return new ArrayList<>();

        List<Map<String, Object>> mergedSteps = hqSteps.stream()
                .map(HashMap::new)
                .collect(Collectors.toList());

        for (var override : overrides) {
            mergedSteps.stream()
                    .filter(s -> String.valueOf(s.get("stepOrder")).equals(String.valueOf(override.getStepOrder())))
                    .findFirst()
                    .ifPresent(s -> s.put("description", override.getDescription()));
        }
        return mergedSteps;
    }

    public static String convertOperationToPolicyString(Map<String, Object> operationData) {

        if (operationData == null) return "Chưa có cấu hình vận hành từ cơ sở chính.";

        StringBuilder sb = new StringBuilder();
        sb.append("===== THÔNG TIN VẬN HÀNH CHUNG =====\n");

        String hotline = String.valueOf(operationData.getOrDefault("hotline", "Không có"));
        sb.append("📞 Đường dây nóng: ").append(hotline).append("\n");

        String emailSupport = String.valueOf(operationData.getOrDefault("emailSupport", "Không có"));
        sb.append("📧 Email hỗ trợ: ").append(emailSupport).append("\n");

        if (operationData.get("minCounsellorPerSlot") != null) {
            sb.append("👥 Số tư vấn viên tối thiểu mỗi ca: ")
                    .append(operationData.get("minCounsellorPerSlot"))
                    .append(" người\n");
        }

        if (operationData.get("slotDurationInMinutes") != null) {
            sb.append("⏱️ Thời lượng mỗi ca tư vấn: ").append(operationData.get("slotDurationInMinutes")).append(" phút\n");
        }
        if (operationData.get("maxBookingPerSlot") != null) {
            sb.append("🎟️ Số khách tối đa mỗi ca: ").append(operationData.get("maxBookingPerSlot")).append(" người\n");
        }
        if (operationData.get("allowBookingBeforeHours") != null) {
            sb.append("🛡️ Yêu cầu đặt lịch trước: ").append(operationData.get("allowBookingBeforeHours")).append(" giờ\n");
        }

        Map<String, Object> working = (Map<String, Object>) operationData.get("workingConfig");

        if (working != null) {
            sb.append("⏰ GIỜ LÀM VIỆC:\n");
            sb.append("- Ghi chú: ").append(working.getOrDefault("note", "Không có")).append("\n");
        }

        List<Map<String, Object>> shifts = (List<Map<String, Object>>) working.get("workShifts");

        if (shifts != null && !shifts.isEmpty()) {
            for (Map<String, Object> shift : shifts) {
                sb.append("  • ").append(shift.get("name")).append(": ").append(shift.get("startTime")).append(" - ").append(shift.get("endTime")).append("\n");
            }
        }

        List<String> regularDays = (List<String>) working.get("regularDays");
        if (regularDays != null && !regularDays.isEmpty()) {
            String daysVietnamese = regularDays.stream().map(SchoolConfigUtil::mapDayToVietnamese).collect(Collectors.joining(", "));
            sb.append("- Các ngày trong tuần: ").append(daysVietnamese).append("\n");
        }

        // Xử lý ngày cuối tuần
        List<String> weekendDays = (List<String>) working.get("weekendDays");
        if (weekendDays != null && !weekendDays.isEmpty()) {
            String weekendsVietnamese = weekendDays.stream().map(SchoolConfigUtil::mapDayToVietnamese).collect(Collectors.joining(", "));
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
                sb.append("  Bước ").append(step.get("stepOrder")).append(". ").append(step.get("stepName")).append(": ").append(step.get("description")).append("\n");
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

    public static Map<String, Object> getWorkingConfig(SchoolConfig operationConfig) {
        if (operationConfig != null && operationConfig.getValue() instanceof Map) {

            Map<String, Object> value = (Map<String, Object>) operationConfig.getValue();

            Object workingConfig = value.get("workingConfig");

            if (workingConfig instanceof Map) {
                return (Map<String, Object>) workingConfig;
            }
        }
        return null;
    }

    public static String validateWithWorkingConfig(String dayOfWeek, LocalTime start, LocalTime end, Map<String, Object> workingConfig) {
        if (workingConfig == null) return null;

        String dayReq = (dayOfWeek != null) ? dayOfWeek.toUpperCase() : "";

        // 1. Kiểm tra ngày hoạt động
        List<String> regularDays = (List<String>) workingConfig.get("regularDays");
        List<String> weekendDays = (List<String>) workingConfig.get("weekendDays");
        boolean isOpenSunday = Boolean.TRUE.equals(workingConfig.get("isOpenSunday"));

        boolean isWorkingDay = (regularDays != null && regularDays.contains(dayReq)) ||
                (weekendDays != null && weekendDays.contains(dayReq)) ||
                ("SUN".equals(dayReq) && isOpenSunday);

        if (!isWorkingDay) {
            return "The campus is not operational on " + dayReq + ".";
        }

        // 2. Kiểm tra khung giờ (Work Shifts)
        List<Map<String, Object>> workShifts = (List<Map<String, Object>>) workingConfig.get("workShifts");
        if (workShifts != null && !workShifts.isEmpty()) {
            boolean isInShift = workShifts.stream().anyMatch(shift -> {
                LocalTime sStart = LocalTime.parse(String.valueOf(shift.get("startTime")));
                LocalTime sEnd = LocalTime.parse(String.valueOf(shift.get("endTime")));
                return (start.equals(sStart) || start.isAfter(sStart)) &&
                        (end.equals(sEnd) || end.isBefore(sEnd));
            });

            if (!isInShift) {
                return "The requested time slot falls outside of operational shifts.";
            }
        }

        return null;
    }
}

