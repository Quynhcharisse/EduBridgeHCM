package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.HolidayImpactLevel;
import com.sp26se041.edubridgehcm.enums.SessionType;
import com.sp26se041.edubridgehcm.enums.WorkShiftKind;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.SchoolConfig;
import com.sp26se041.edubridgehcm.models.SchoolHoliday;
import com.sp26se041.edubridgehcm.requests.UpdateCampusConfigRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchoolConfigUtil {

    private static final DateTimeFormatter SLOT_WINDOW_TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

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

        if (request.getAdmissionStepsOverride() != null) {
            merged.put("admissionSteps", mergeAdmissionSteps(
                    (List<Map<String, Object>>) hqData.get("admissionSteps"),
                    request.getAdmissionStepsOverride()
            ));
        }
        return merged;
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
                sb.append("  • ").append(formatWorkShiftLabel(shift.get("name"))).append(": ")
                        .append(shift.get("startTime")).append(" - ").append(shift.get("endTime")).append("\n");
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

        List<Map<String, Object>> seasons = (List<Map<String, Object>>) operationData.get("admissionSeasons");
        if (seasons != null && !seasons.isEmpty()) {
            sb.append("\n🚀 CÁC CHIẾN DỊCH TUYỂN SINH ĐẶC BIỆT:\n");
            for (Map<String, Object> s : seasons) {
                sb.append("- ").append(s.get("seasonName"))
                        .append(" [").append(s.get("startDate")).append(" -> ").append(s.get("endDate")).append("]\n");

                if (Boolean.TRUE.equals(s.get("enableSunday"))) {
                    sb.append("  • 🟢 Mở cửa làm việc ngày Chủ Nhật\n");
                }

                if (s.get("minCounsellorMultiplier") != null && (int) s.get("minCounsellorMultiplier") > 1) {
                    sb.append("  • 👥 Tăng cường nhân sự: x")
                            .append(s.get("minCounsellorMultiplier")).append(" tư vấn viên/ca\n");
                }

                List<Map<String, Object>> extraShifts = (List<Map<String, Object>>) s.get("extraShifts");
                if (extraShifts != null && !extraShifts.isEmpty()) {
                    sb.append("  • 🌙 Ca làm việc tăng cường:\n");
                    for (Map<String, Object> shift : extraShifts) {
                        sb.append("    + ").append(formatWorkShiftLabel(shift.get("name")))
                                .append(": ").append(shift.get("startTime")).append(" - ").append(shift.get("endTime")).append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String formatWorkShiftLabel(Object nameObj) {
        if (nameObj == null) {
            return "—";
        }
        String s = String.valueOf(nameObj);
        WorkShiftKind k = WorkShiftKind.resolve(s);
        if (k != null) {
            return k.getDisplayNameVi() + " (" + k.name() + ")";
        }
        return s;
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

        //Lấy chính sách thực thi cho ngày này (đã bao gồm Season Overrides)
        // Lưu ý: Dùng hàm getEffectivePolicy mà mình đã nâng cấp ở trên để lấy toàn bộ Map
        Map<String, Object> effectivePolicy = getEffectiveWorkingConfig(targetDate, operationSettingsData);

        // KIỂM TRA HỌC KỲ (ACADEMIC SEMESTER)
        if (!isWithinAcademicTerms(targetDate, effectivePolicy)) {
            return "Ngày " + targetDate + " nằm ngoài học kỳ chính thức.";
        }

        // KIỂM TRA NGÀY NGHỈ/LỄ (SCHOOL HOLIDAYS)
        if (holidays != null) {
            for (SchoolHoliday holiday : holidays) {
                if (!targetDate.isBefore(holiday.getStartDate()) && !targetDate.isAfter(holiday.getEndDate())) {
                    var impact = holiday.getHolidayImpactLevel();
                    // Chặn tuyệt đối nếu nghỉ toàn bộ hoặc nhân viên nghỉ
                    if (impact == HolidayImpactLevel.ALL_SHUTDOWN || impact == HolidayImpactLevel.STAFF_ONLY) {
                        return "Cơ sở đóng cửa: " + holiday.getTitle();
                    }
                }
            }
        }

        //KIỂM TRA GIỜ LÀM VIỆC (Dựa trên Policy đã được Override)
        Map<String, Object> workingConfig = (Map<String, Object>) effectivePolicy.get("workingConfig");
        if (workingConfig != null) {
            String dayOfWeek = targetDate.getDayOfWeek().name().substring(0, 3); // "MON", "TUE"...

            String error = validateWithWorkingConfig(dayOfWeek, start, end, sessionTypeReq, workingConfig);
            if (error != null) return error;
        }

        return null;
    }

    //Đây là hàm then chốt để xử lý Override Policy
    // Nó sẽ quyết định xem ngày hôm đó dùng lịch thường hay lịch mùa cao điểm.
    public static Map<String, Object> getEffectiveWorkingConfig(LocalDate date, Map<String, Object> operationData) {
        if (operationData == null) return new HashMap<>();

        // 1. Lấy dữ liệu gốc làm nền
        Map<String, Object> effectivePolicy = new HashMap<>(operationData);
        Map<String, Object> baseWorking = (Map<String, Object>) operationData.get("workingConfig");
        List<Map<String, Object>> seasons = (List<Map<String, Object>>) operationData.get("admissionSeasons");

        if (seasons == null || seasons.isEmpty()) return effectivePolicy;

        // 2. Tìm mùa tuyển sinh đang hoạt động
        Map<String, Object> activeSeason = seasons.stream().filter(s -> {
            LocalDate start = parseDate(s.get("startDate"));
            LocalDate end = parseDate(s.get("endDate"));
            return start != null && end != null && !date.isBefore(start) && !date.isAfter(end);
        }).findFirst().orElse(null);

        // 3. Nếu không có mùa nào, trả về Policy gốc
        if (activeSeason == null) return effectivePolicy;

        // 4. TIẾN HÀNH "NẤU" (MERGE) - Ưu tiên Season Overrides
        Map<String, Object> effectiveWorking = new HashMap<>(baseWorking);

        // Đè trạng thái mở cửa Chủ Nhật
        if (Boolean.TRUE.equals(activeSeason.get("enableSunday"))) {
            effectiveWorking.put("isOpenSunday", true);
        }

        // Đè hoặc Cộng dồn ca làm việc
        if (activeSeason.get("extraShifts") != null) {
            List<Map<String, Object>> combinedShifts = new ArrayList<>((List<Map<String, Object>>) baseWorking.get("workShifts"));
            combinedShifts.addAll((List<Map<String, Object>>) activeSeason.get("extraShifts"));
            effectiveWorking.put("workShifts", combinedShifts);
        }

        // Áp dụng hệ số nhân nhân sự (minCounsellorMultiplier)
        int baseCounsellor = (int) operationData.getOrDefault("minCounsellorPerSlot", 1);
        int multiplier = (int) activeSeason.getOrDefault("minCounsellorMultiplier", 1);

        effectivePolicy.put("minCounsellorPerSlot", baseCounsellor * multiplier);
        effectivePolicy.put("workingConfig", effectiveWorking);
        effectivePolicy.put("activeSeasonName", activeSeason.get("seasonName")); // Để hiển thị cho UI

        return effectivePolicy;
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
            return "Cơ sở không làm việc vào " + mapDayToVietnamese(dayReq) + " (mã ngày: " + dayReq + ").";
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
                return "Khung giờ " + start + " – " + end + " không nằm trọn trong một ca làm việc đã cấu hình cho cơ sở.";
            }

            String shiftRaw = String.valueOf(matchedShift.get("name"));
            String shiftName = shiftRaw.toLowerCase();
            String sessionValue = sessionTypeReq.toLowerCase();

            if (!isSessionMatched(shiftName, sessionValue)) {
                return "Khung giờ " + start + " – " + end + " thuộc ca \"" + shiftRaw
                        + "\" nhưng không khớp loại buổi đã chọn (" + sessionTypeReq + ").";
            }
        }

        return null;
    }

     //Lấy khung giờ từ <strong>một</strong> ca trong {@code workingConfig.workShifts} khớp {@code sessionType} (MORNING / AFTERNOON / EVENING).
     //Nếu có nhiều ca cùng buổi, lấy <strong>ca đầu tiên</strong> trong danh sách (thứ tự cấu hình HQ). Không gộp nhiều ca —
     //{@link #validateWithWorkingConfig} yêu cầu cả [start,end] nằm trọn trong một ca.
    // @return {@code [0] = start, [1] = end} hoặc {@code null} nếu không có ca tương ứng
    public static LocalTime[] resolveShiftTimeWindowForSessionType(Map<String, Object> workingConfig, String sessionTypeName) {
        if (workingConfig == null || sessionTypeName == null || sessionTypeName.isBlank()) {
            return null;
        }
        final String sessionValue;
        try {
            sessionValue = SessionType.valueOf(sessionTypeName.trim().toUpperCase()).getValue();
        } catch (IllegalArgumentException e) {
            return null;
        }
        List<Map<String, Object>> workShifts = (List<Map<String, Object>>) workingConfig.get("workShifts");
        if (workShifts == null || workShifts.isEmpty()) {
            return null;
        }
        // Buổi chiều (AFTERNOON) khớp cả NOON và AFTERNOON — ưu tiên ca chiều rồi mới ca trưa
        if ("afternoon".equals(sessionValue)) {
            LocalTime[] fromAfternoon = pickFirstShiftWindow(workShifts, sessionValue, WorkShiftKind.AFTERNOON);
            if (fromAfternoon != null) {
                return fromAfternoon;
            }
            LocalTime[] fromNoon = pickFirstShiftWindow(workShifts, sessionValue, WorkShiftKind.NOON);
            if (fromNoon != null) {
                return fromNoon;
            }
        }
        return pickFirstShiftWindow(workShifts, sessionValue, null);
    }

    private static LocalTime[] pickFirstShiftWindow(List<Map<String, Object>> workShifts, String sessionValue, WorkShiftKind requiredKind) {
        for (Map<String, Object> shift : workShifts) {
            if (shift == null) {
                continue;
            }
            String shiftName = String.valueOf(shift.get("name"));
            if (!isSessionMatched(shiftName, sessionValue)) {
                continue;
            }
            if (requiredKind != null) {
                WorkShiftKind k = WorkShiftKind.resolve(shiftName);
                if (k != requiredKind) {
                    continue;
                }
            }
            LocalTime sStart = LocalTime.parse(String.valueOf(shift.get("startTime")));
            LocalTime sEnd = LocalTime.parse(String.valueOf(shift.get("endTime")));
            if (sStart.isBefore(sEnd)) {
                return new LocalTime[]{sStart, sEnd};
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
        WorkShiftKind kind = WorkShiftKind.resolve(shiftName);
        if (kind != null) {
            if ("morning".equals(sessionValue)) {
                return kind == WorkShiftKind.MORNING;
            }
            if ("afternoon".equals(sessionValue)) {
                return kind == WorkShiftKind.AFTERNOON || kind == WorkShiftKind.NOON;
            }
            if ("evening".equals(sessionValue)) {
                return kind == WorkShiftKind.EVENING;
            }
            return false;
        }
        String n = shiftName.toLowerCase();
        if ("morning".equals(sessionValue)) {
            return n.contains("sáng") || n.contains("morning");
        }
        if ("afternoon".equals(sessionValue)) {
            return n.contains("chiều") || n.contains("afternoon") || n.contains("trưa") || n.contains("noon");
        }
        if ("evening".equals(sessionValue)) {
            return n.contains("tối") || n.contains("evening");
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

    public static Map<String, Object> getEffectiveOperationSettingsMap(SchoolConfig hqOperationConfig, Campus campus) {
        Map<String, Object> base = new HashMap<>();
        if (hqOperationConfig != null && hqOperationConfig.getValue() instanceof Map) {
            base.putAll(new HashMap<>((Map<String, Object>) hqOperationConfig.getValue()));
        }
        Object pd = campus.getPolicyDetail();
        if (!(pd instanceof Map)) {
            return base;
        }
        Map<String, Object> policy = (Map<String, Object>) pd;
        String[] numericKeys = {
                "minCounsellorPerSlot",
                "slotDurationInMinutes",
                "maxBookingPerSlot",
                "allowBookingBeforeHours"
        };
        for (String key : numericKeys) {
            if (policy.get(key) != null) {
                base.put(key, policy.get(key));
            }
        }
        
        return base;
    }

    public static List<String[]> splitRangeIntoPolicySlotWindows(LocalTime start, LocalTime end, int slotDurationMinutes) {
        if (slotDurationMinutes <= 0) {
            throw new IllegalArgumentException("Thời lượng mỗi slot trong cấu hình phải > 0 để tách slot.");
        }
        long totalMinutes = Duration.between(start, end).toMinutes();
        if (totalMinutes <= 0) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc.");
        }
        if (totalMinutes % slotDurationMinutes != 0) {
            int usable = (int) (totalMinutes / slotDurationMinutes) * slotDurationMinutes;
            throw new IllegalArgumentException(
                    "Độ dài ca " + totalMinutes + " phút phải chia hết cho " + slotDurationMinutes
                            + " phút/slot (theo cấu hình vận hành). Gợi ý: rút endTime còn " + usable + " phút ("
                            + (usable / slotDurationMinutes) + " slot) hoặc chỉnh policy/slot phút.");
        }
        int n = (int) (totalMinutes / slotDurationMinutes);
        List<String[]> windows = new ArrayList<>(n);
        LocalTime cur = start;
        for (int i = 0; i < n; i++) {
            LocalTime next = cur.plusMinutes(slotDurationMinutes);
            windows.add(new String[]{cur.format(SLOT_WINDOW_TIME_FMT), next.format(SLOT_WINDOW_TIME_FMT)});
            cur = next;
        }
        if (!cur.equals(end)) {
            throw new IllegalStateException("Lệch biên khi tách slot.");
        }
        return windows;
    }

    public static Map<String, Integer> getNumericPolicyFromOperationMap(Map<String, Object> effectiveOperationSettings) {
        Map<String, Integer> constraints = new HashMap<>();
        if (effectiveOperationSettings == null) {
            return constraints;
        }
        String[] keys = {
                "minCounsellorPerSlot",
                "slotDurationInMinutes",
                "maxBookingPerSlot",
                "allowBookingBeforeHours"
        };
        for (String key : keys) {
            Object value = effectiveOperationSettings.get(key);
            if (value != null) {
                try {
                    constraints.put(key, Integer.parseInt(String.valueOf(value)));
                } catch (NumberFormatException ignored) {
                    // bỏ qua giá trị lỗi
                }
            }
        }
        return constraints;
    }

    public static String validateAssignmentRangeAgainstBlockingHolidays(
            LocalDate startDate,
            LocalDate endDate,
            List<SchoolHoliday> holidays,
            Integer campusId) {
        if (holidays == null || holidays.isEmpty()) {
            return null;
        }
        for (SchoolHoliday h : holidays) {
            if (!holidayAppliesToCampus(h, campusId)) {
                continue;
            }
            if (!dateRangesOverlap(startDate, endDate, h.getStartDate(), h.getEndDate())) {
                continue;
            }
            HolidayImpactLevel impact = h.getHolidayImpactLevel();
            if (impact == HolidayImpactLevel.ALL_SHUTDOWN || impact == HolidayImpactLevel.STAFF_ONLY) {
                return "Khoảng gán lịch trùng ngày nghỉ \"" + h.getTitle() + "\" (mức " + impact.name()
                        + "). Không gán tư vấn tại cơ sở khi đóng cửa toàn phần hoặc nhân sự nghỉ.";
            }
        }
        return null;
    }

    private static boolean holidayAppliesToCampus(SchoolHoliday h, Integer campusId) {
        if (h.getCampus() == null) {
            return true;
        }
        return campusId != null && h.getCampus().getId().equals(campusId);
    }

    private static boolean dateRangesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
        return !aEnd.isBefore(bStart) && !aStart.isAfter(bEnd);
    }
}

