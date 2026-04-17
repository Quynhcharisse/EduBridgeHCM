package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.HolidayImpactLevel;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CounsellorSlot;
import com.sp26se041.edubridgehcm.models.SchoolHoliday;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorSlotRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolHolidayRepo;
import com.sp26se041.edubridgehcm.requests.CreateHolidayRequest;
import com.sp26se041.edubridgehcm.requests.UpdateHolidayRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolHolidayService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.holiday.HolidayValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolHolidayServiceImpl implements SchoolHolidayService {

    private final CounsellorSlotRepo counsellorSlotRepo;

    private final SchoolHolidayRepo schoolHolidayRepo;

    private final CampusRepo campusRepo;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> createHoliday(CreateHolidayRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        String error = HolidayValidation.createHolidayValidation(request, actorCampus);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        Integer schoolId = actorCampus.getSchool().getId();
        Integer targetCampusId = Boolean.TRUE.equals(request.getIsGlobal()) ? null : request.getCampusId();
        HolidayImpactLevel holidayImpactLevel = HolidayImpactLevel.valueOf(request.getHolidayImpactLevel());

        // start_date <= endDate AND end_date >= startDate
        boolean isOverlap = schoolHolidayRepo.existsBySchoolIdAndCampusIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                schoolId,
                targetCampusId,
                request.getEndDate(),   // Khớp với StartDateLessThanEqual
                request.getStartDate()  // Khớp với EndDateGreaterThanEqual
        );

        if (isOverlap) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Khoảng thời gian này đã trùng với một lịch nghỉ khác.", null);
        }

        // Xử lý các Slot tư vấn bị ảnh hưởng
        if (holidayImpactLevel == HolidayImpactLevel.ALL_SHUTDOWN || holidayImpactLevel == HolidayImpactLevel.STAFF_ONLY) {

            // 1. Xử lý lịch đã đặt (BOOKED)
            List<CounsellorSlot> bookedSlots = getAffectedSlots(schoolId, targetCampusId, Status.BOOKED, request.getStartDate(), request.getEndDate());

            if (!bookedSlots.isEmpty()) {
                // Check forceCreate trước khi thực hiện bất kỳ thay đổi nào
                if (!Boolean.TRUE.equals(request.getForceCreate())) {
                    return ResponseBuilder.build(HttpStatus.CONFLICT,
                            "Có " + bookedSlots.size() + " lịch đã đặt. Bạn có chắc chắn muốn hủy để tạo ngày nghỉ?", null);
                }

                // Nếu đồng ý Force Create -> Hủy lịch
                bookedSlots.forEach(slot -> slot.setStatus(Status.CANCELLED));
                counsellorSlotRepo.saveAll(bookedSlots);
            }

            // 2. Xử lý lịch trống (AVAILABLE) -> DISABLED
            List<CounsellorSlot> availableSlots = getAffectedSlots(schoolId, targetCampusId, Status.AVAILABLE, request.getStartDate(), request.getEndDate());
            if (!availableSlots.isEmpty()) {
                availableSlots.forEach(slot -> slot.setStatus(Status.DISABLED));
                counsellorSlotRepo.saveAll(availableSlots);
            }
        }

        // Lưu Holiday mới
        SchoolHoliday newHoliday = SchoolHoliday.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .holidayImpactLevel(holidayImpactLevel)
                .school(actorCampus.getSchool())
                .campus(targetCampusId != null ? campusRepo.findById(targetCampusId).orElse(null) : null)
                .build();

        schoolHolidayRepo.save(newHoliday);
        return ResponseBuilder.build(HttpStatus.CREATED, "Tạo ngày nghỉ thành công.", null);
    }

    // Hàm lấy Slot theo điều kiện
    // ==> để giải quyết sự khác biệt giữa lịch nghỉ cấp Trường (Global) và lịch nghỉ cấp Cơ sở (Campus).
    private List<CounsellorSlot> getAffectedSlots(Integer schoolId, Integer campusId, Status status, LocalDate start, LocalDate end) {

        if (campusId == null) {
            return counsellorSlotRepo.findByStatusAndCounsellorCampusSchoolIdAndStartDateBetween(status, schoolId, start, end);
        }

        return counsellorSlotRepo.findByStatusAndCounsellorCampusSchoolIdAndCounsellorCampusIdAndStartDateBetween(status, schoolId, campusId, start, end);
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> updateHoliday(UpdateHolidayRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        String error = HolidayValidation.updateHolidayValidation(request, actorCampus);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        SchoolHoliday existingHoliday = schoolHolidayRepo.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ngày nghỉ"));

        Integer schoolId = actorCampus.getSchool().getId();
        Integer targetCampusId = existingHoliday.getCampus() != null ? existingHoliday.getCampus().getId() : null;


        boolean isOverlap = schoolHolidayRepo.existsBySchoolIdAndCampusIdAndIdNotAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                schoolId, targetCampusId, request.getId(), request.getEndDate(), request.getStartDate());

        if (isOverlap) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Khoảng thời gian cập nhật trùng với một ngày nghỉ khác.", null);
        }

        // 3. Khôi phục Slot cũ: Tìm các slot đang bị DISABLED trong khoảng cũ
        List<CounsellorSlot> oldDisabledSlots = getAffectedSlots(schoolId, targetCampusId, Status.DISABLED,
                existingHoliday.getStartDate(), existingHoliday.getEndDate());

        // 4. Cập nhật thông tin Holiday mới
        existingHoliday.setTitle(request.getTitle());
        existingHoliday.setStartDate(request.getStartDate());
        existingHoliday.setEndDate(request.getEndDate());
        existingHoliday.setHolidayImpactLevel(HolidayImpactLevel.valueOf(request.getHolidayImpactLevel()));
        schoolHolidayRepo.save(existingHoliday);

        // 5. Đồng bộ hóa Slot
        // BƯỚC A: Trả những slot cũ không còn nằm trong kỳ nghỉ mới về AVAILABLE
        oldDisabledSlots.forEach(slot -> {
            if (slot.getStartDate().isBefore(request.getStartDate()) || slot.getStartDate().isAfter(request.getEndDate())) {
                slot.setStatus(Status.AVAILABLE);
            }
        });
        counsellorSlotRepo.saveAll(oldDisabledSlots);

        //Áp dụng luật mới cho khoảng thời gian mới
        // Tái sử dụng logic quét và xử lý (Booked -> Cancel, Available -> Disabled)
        return applyHolidayImpactToSlots(schoolId, targetCampusId, request.getStartDate(),
                request.getEndDate(), request.getForceCreate(),
                HolidayImpactLevel.valueOf(request.getHolidayImpactLevel()));
    }

    private ResponseEntity<ResponseObject> applyHolidayImpactToSlots(Integer schoolId, Integer campusId, LocalDate start, LocalDate end, Boolean forceCreate, HolidayImpactLevel impact) {
        // TRƯỜNG HỢP 1: Nghỉ làm việc (Khóa/Hủy slot)
        if (impact == HolidayImpactLevel.ALL_SHUTDOWN || impact == HolidayImpactLevel.STAFF_ONLY) {

            List<CounsellorSlot> bookedSlots = getAffectedSlots(schoolId, campusId, Status.BOOKED, start, end);
            if (!bookedSlots.isEmpty() && !Boolean.TRUE.equals(forceCreate)) {
                return ResponseBuilder.build(HttpStatus.CONFLICT, "Có " + bookedSlots.size() + " lịch đã đặt. Bạn có chắc muốn hủy?", null);
            }
            bookedSlots.forEach(s -> s.setStatus(Status.CANCELLED));
            counsellorSlotRepo.saveAll(bookedSlots);

            List<CounsellorSlot> availableSlots = getAffectedSlots(schoolId, campusId, Status.AVAILABLE, start, end);
            availableSlots.forEach(s -> s.setStatus(Status.DISABLED));
            counsellorSlotRepo.saveAll(availableSlots);

        }
        // TRƯỜNG HỢP 2: Các mức impact nhẹ (STUDENT_ONLY, ONLINE_ONLY...)
        // Nếu trước đó đang bị DISABLED thì mở lại cho nhân viên làm việc
        else {
            List<CounsellorSlot> disabledSlots = getAffectedSlots(schoolId, campusId, Status.DISABLED, start, end);
            if (!disabledSlots.isEmpty()) {
                disabledSlots.forEach(s -> s.setStatus(Status.AVAILABLE));
                counsellorSlotRepo.saveAll(disabledSlots);
            }
        }

        return ResponseBuilder.build(HttpStatus.OK, "Cập nhật ngày nghỉ thành công", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewHolidayList() {
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin Campus", null);
        }

        Integer schoolId = actorCampus.getSchool().getId();
        List<SchoolHoliday> holidays;

        // 1. Lấy dữ liệu từ Repo tùy theo quyền của Actor
        if (Boolean.TRUE.equals(actorCampus.getIsPrimaryBranch())) {
            holidays = schoolHolidayRepo.findBySchoolIdOrderByStartDateDesc(schoolId);
        } else {
            // Giả sử repo có hàm findBySchoolIdAndTargetCampusOrIsGlobal
            holidays = schoolHolidayRepo.findBySchoolIdAndCampusId(schoolId, actorCampus.getId());
        }

        List<Map<String, Object>> response = buildSchoolHolidayList(holidays, schoolId);

        return ResponseBuilder.build(HttpStatus.OK, "Lấy danh sách ngày nghỉ thành công", response);
    }

    private List<Map<String, Object>> buildSchoolHolidayList(List<SchoolHoliday> schoolHolidayData, Integer schoolId) {

        List<Status> affectedStatuses = List.of(Status.DISABLED, Status.CANCELLED);

        return schoolHolidayData.stream().map(h -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", h.getId());
            map.put("title", h.getTitle());
            map.put("startDate", h.getStartDate());
            map.put("endDate", h.getEndDate());
            map.put("impactLevel", h.getHolidayImpactLevel());
            map.put("isGlobal", h.getCampus() == null);
            map.put("campusName", h.getCampus() != null ? h.getCampus().getName() : "Toàn hệ thống");

            long count;
            if (h.getCampus() == null) {
                // Đếm Global cho cả trường
                count = counsellorSlotRepo.countByCounsellorCampusSchoolIdAndStartDateBetweenAndStatusIn(
                        schoolId, h.getStartDate(), h.getEndDate(), affectedStatuses);
            } else {
                // Đếm riêng cho từng Campus
                count = counsellorSlotRepo.countByCounsellorCampusSchoolIdAndCounsellorCampusIdAndStartDateBetweenAndStatusIn(
                        schoolId, h.getCampus().getId(), h.getStartDate(), h.getEndDate(), affectedStatuses);
            }

            map.put("affectedSlotsCount", count);
            return map;
        }).collect(Collectors.toList());
    }

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }
}
