package com.sp26se041.edubridgehcm.services.implementors;

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

import java.util.List;

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

        boolean isOverlap = schoolHolidayRepo.existsBySchoolIdAndCampusIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                schoolId, targetCampusId, request.getStartDate(), request.getEndDate());

        if (isOverlap) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Khoảng thời gian này đã trùng với một lịch nghỉ khác đã tồn tại.", null);
        }

        // Nếu ngày nghỉ này cho phép Tư vấn viên nghỉ (applyToConsultant = true)
        if (Boolean.TRUE.equals(request.getApplyToConsultant())) {

            // Lấy danh sách các slot đã có parent đặt
            List<CounsellorSlot> bookedSlots = (targetCampusId == null)
                    ? counsellorSlotRepo.findByStatusAndCounsellorCampusSchoolIdAndStartDateBetween(Status.BOOKED, schoolId, request.getStartDate(), request.getEndDate())
                    : counsellorSlotRepo.findByStatusAndCounsellorCampusSchoolIdAndCounsellorCampusIdAndStartDateBetween(Status.BOOKED, schoolId, targetCampusId, request.getStartDate(), request.getEndDate());

            if (!bookedSlots.isEmpty()) {
                // Nếu có lịch đặt mà chưa xác nhận forceCreate -> Báo lỗi Level 1
                if (!Boolean.TRUE.equals(request.getForceCreate())) {
                    return ResponseBuilder.build(HttpStatus.CONFLICT,
                            "Hiện có " + bookedSlots.size() + " lịch tư vấn đã được đặt trong khoảng thời gian này. Bạn có chắc chắn muốn hủy chúng để tạo ngày nghỉ?",
                            null);
                }

                if (!bookedSlots.isEmpty()) {
                    // Nếu có khách và chưa xác nhận
                    if (!Boolean.TRUE.equals(request.getForceCreate())) {
                        return ResponseBuilder.build(HttpStatus.CONFLICT, "Có " + bookedSlots.size() + " lịch tư vấn đã đặt. Bạn có chắc muốn hủy để tạo ngày nghỉ?", null);
                    }

                    // Nếu campus đã xác nhận (forceCreate = true), thực hiện hủy lịch và lưu lý do.
                    bookedSlots.forEach(slot -> {
                        slot.setStatus(Status.CANCELLED);
                        // Gửi thông báo cho phụ huynh ==> lịch bị cancelled
                        // ==> tự deal vs parent ==> bên phía tư vấn viên nha
                    });
                    counsellorSlotRepo.saveAll(bookedSlots);
                }
            }
        }

        SchoolHoliday newHoliday = SchoolHoliday.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .applyToConsultant(request.getApplyToConsultant())
                .school(actorCampus.getSchool())
                .campus(targetCampusId != null ? campusRepo.findById(targetCampusId).orElse(null) : null)
                .build();

        schoolHolidayRepo.save(newHoliday);

        List<CounsellorSlot> availableSlots = (targetCampusId == null)
                ? counsellorSlotRepo.findByStatusAndCounsellorCampusSchoolIdAndStartDateBetween(Status.AVAILABLE, schoolId, request.getStartDate(), request.getEndDate())
                : counsellorSlotRepo.findByStatusAndCounsellorCampusSchoolIdAndCounsellorCampusIdAndStartDateBetween(Status.AVAILABLE, schoolId, targetCampusId, request.getStartDate(), request.getEndDate());

        if (!availableSlots.isEmpty()) {
            availableSlots.forEach(slot -> slot.setStatus(Status.DISABLED));
            counsellorSlotRepo.saveAll(availableSlots); // Cập nhật hàng loạt slot sang trạng thái Vô hiệu hóa
        }

        return ResponseBuilder.build(HttpStatus.CREATED, "Tạo ngày nghỉ thành công.", null);
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

        String error = HolidayValidation.updateHolidayValidation();

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewHolidayList() {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }


        return null;
    }

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }
}
