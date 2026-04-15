package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.models.SchoolHoliday;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchoolConfigUtil {
    public static List<Map<String, Object>> mergeFacilityItems(
            List<Map<String, Object>> templateItems,
            List<Map<String, Object>> currentCampusItems,
            List<UpdateCampusConfigRequest.FacilityItemRequest> requestItems
    ) {

        Map<String, Map<String, Object>> finalResultMap = new LinkedHashMap<>();

        // s1 : đổ dữ liệu làm khung
        if (templateItems != null) {
            for (Map<String, Object> item : templateItems) {
                Map<String, Object> newItem = new HashMap<>(item);
                newItem.put("isCustom", false);
                newItem.put("isUsage", false); // mặc đich chưa dùng ==> nếu campus phụ chưa điền
                finalResultMap.put((String) item.get("facilityCode"), newItem);
            }
        }

        // s2: bỏ data facility Campus hiện tại đang có vào
        // ==> để giữ lại đồ Custom đã thêm lần trước
        if (currentCampusItems != null) {
            for (Map<String, Object> item : currentCampusItems) {
                finalResultMap.put((String) item.get("facilityCode"), new HashMap<>(item));
            }
        }

        // s3: đè dữ liệu mới từ Request lên ==> thêm mới
        if (requestItems != null) {
            for (UpdateCampusConfigRequest.FacilityItemRequest req : requestItems) {
                String code = req.getFacilityCode();
                Map<String, Object> itemData = finalResultMap.getOrDefault(code, new HashMap<>());

                itemData.put("facilityCode", code);
                itemData.put("name", req.getName());
                itemData.put("value", req.getValue());
                itemData.put("unit", req.getUnit());
                itemData.put("category", req.getCategory());
                itemData.put("isUsage", req.getValue() > 0);

                // Nếu code này không có trong HQ, thì đích thị là đồ Custom
                boolean isHqCode = templateItems != null && templateItems.stream().anyMatch(h -> h.get("facilityCode").equals(code));
                itemData.put("isCustom", !isHqCode);

                finalResultMap.put(code, itemData);
            }
        }

        return new ArrayList<>(finalResultMap.values());
    }

    public static Map<String, Object> mergeOperationConfig(Map<String, Object> hqData, UpdateCampusConfigRequest request) {
        if (hqData == null) return new HashMap<>();

        Map<String, Object> merged = new HashMap<>(hqData);

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

    public static String validateSlotAvailability(
            LocalDate targetDate,
            LocalTime start,
            LocalTime end,
            String sessionTypeReq,
            Map<String, Object> operationSettingsData,
            List<SchoolHoliday> holidays) {

        if (operationSettingsData == null) return null;

        // KIỂM TRA HỌC KỲ (ACADEMIC SEMESTER)
        if (!isWithinAcademicTerms(targetDate, operationSettingsData)) {
            return "The selected date " + targetDate + " falls outside of the active academic semesters.";
        }

        // KIỂM TRA NGÀY NGHỈ/LỄ (SCHOOL HOLIDAYS)
        if (holidays != null) {
            for (SchoolHoliday holiday : holidays) {
                // Check trùng ngày
                if (!targetDate.isBefore(holiday.getStartDate()) && !targetDate.isAfter(holiday.getEndDate())) {
                    if ((Boolean.TRUE.equals(holiday.getApplyToConsultant()))) {
                        return "The campus is closed for: " + holiday.getTitle();
                    }
                }
            }
        }

        //KIỂM TRA GIỜ LÀM VIỆC (WORKING CONFIG)
        Object workingConfigObj = operationSettingsData.get("workingConfig");
        if (workingConfigObj instanceof Map) {
            String dayOfWeek = targetDate.getDayOfWeek().name().substring(0, 3); // "MON", "TUE"...
            String error = validateWithWorkingConfig(dayOfWeek, start, end, sessionTypeReq, (Map<String, Object>) workingConfigObj);
            if (error != null) return error;
        }

        return null;
    }

    //check nhanh "Ngày này có đi học không?
    public static boolean isWithinAcademicTerms(LocalDate targetDate, Map<String, Object> operationSettingsData) {
        if (operationSettingsData == null) return true;

        Object calendarObj = operationSettingsData.get("academicCalendar");
        if (!(calendarObj instanceof Map)) return true; // Nếu không config lịch thì mặc định cho phép

        Map<String, Object> calendar = (Map<String, Object>) calendarObj;

        // Tận dụng hàm isDateInTerm bạn đã viết ở dòng 282
        boolean inTerm1 = isDateInTerm(targetDate, (Map<String, Object>) calendar.get("term1"));
        boolean inTerm2 = isDateInTerm(targetDate, (Map<String, Object>) calendar.get("term2"));

        return inTerm1 || inTerm2;
    }

    public static String validateWithWorkingConfig(String dayOfWeek, LocalTime start, LocalTime end, String sessionTypeReq, Map<String, Object> workingConfig) {
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
        //ktra thêm khớp Session type
        List<Map<String, Object>> workShifts = (List<Map<String, Object>>) workingConfig.get("workShifts");

        if (workShifts != null && !workShifts.isEmpty()) {

            // Tìm ca làm việc mà khung giờ user chọn nằm trọn bên trong
            Map<String, Object> matchedShift = workShifts.stream().filter(shift -> {
                LocalTime sStart = LocalTime.parse(String.valueOf(shift.get("startTime")));
                LocalTime sEnd = LocalTime.parse(String.valueOf(shift.get("endTime")));

                //khung giờ yêu cầu phải nằm trong (hoặc bằng) khung giờ ca làm việc
                return (start.equals(sStart) || start.isAfter(sStart)) &&
                        (end.equals(sEnd) || end.isBefore(sEnd));
            }).findFirst().orElse(null);

            if (matchedShift == null) {
                return "The time slot " + start + " - " + end + " is not covered by any of the campus shifts";
            }

            //verify if the shift name khớp vs session type
            String shiftName = String.valueOf(matchedShift.get("name")).toLowerCase();
            String sessionValue = sessionTypeReq.toLowerCase();

            if (!isSessionMatched(shiftName, sessionValue)) {
                return "The time slot " + start + "-" + end + " belongs to the '" + shiftName +
                        "' shift, which does not match the selected session: '" + sessionTypeReq + "'.";
            }
        }

        return null;
    }

    private static LocalDate parseDate(Object dateObj) {
        if (dateObj == null) return null;
        if (dateObj instanceof LocalDate) return (LocalDate) dateObj;
        return LocalDate.parse(String.valueOf(dateObj));
    }

    // check date in this semester
    private static boolean isDateInTerm(LocalDate date, Map<String, Object> term) {
        if (term == null || term.get("start") == null || term.get("end") == null) return false;

        LocalDate start = LocalDate.parse(String.valueOf(term.get("start")));
        LocalDate end = LocalDate.parse(String.valueOf(term.get("end")));

        return !date.isBefore(start) && !date.isAfter(end);
    }

    private static boolean isSessionMatched(String shiftName, String sessionValue) {
        // Nếu ca tên là "Sáng" hoặc "Morning" thì khớp với MORNING
        if (sessionValue.equals("morning")) {
            return shiftName.contains("sáng") || shiftName.contains("morning");
        }
        if (sessionValue.equals("afternoon")) {
            return shiftName.contains("chiều") || shiftName.contains("afternoon");
        }
        if (sessionValue.equals("evening")) {
            return shiftName.contains("tối") || shiftName.contains("evening");
        }
        return false;
    }

    public static Map<String, Integer> getCampusPolicy(Object policyDetail) {
        Map<String, Integer> constraints = new HashMap<>();
        if (!(policyDetail instanceof Map)) {
            return constraints; // Trả về map trống nếu chưa có config
        }

        Map<String, Object> policy = (Map<String, Object>) policyDetail;

        // Danh sách các key cần trích xuất
        String[] keys = {
                "minCounsellorPerSlot",
                "slotDurationInMinutes",
                "maxBookingPerSlot",
                "allowBookingBeforeHours"
        };

        for (String key : keys) {
            Object value = policy.get(key);
            if (value != null) {
                try {
                    constraints.put(key, Integer.parseInt(String.valueOf(value)));
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu dữ liệu lỗi format
                }
            }
        }

        return constraints;
    }
}

