package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.AdmissionCampaign;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.CampusProgramOffering;
import com.sp26se041.edubridgehcm.models.Program;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.AdmissionCampaignRepo;
import com.sp26se041.edubridgehcm.repositories.CampusProgramOfferingRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.ProgramRepo;
import com.sp26se041.edubridgehcm.requests.CreateAccountCounsellorRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionCampaignTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.requests.CreateCampusRequest;
import com.sp26se041.edubridgehcm.requests.ViewCampusProgramOfferingRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.SchoolService;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {
    private final CampusRepo campusRepo;
    private final AdmissionCampaignRepo admissionCampaignRepo;
    private final CampusProgramOfferingRepo campusProgramOfferingRepo;
    private final ProgramRepo programRepo;
    private final AccountRepo accountRepo;

    @Override
    public ResponseEntity<ResponseObject> createCampus(CreateCampusRequest request, HttpServletRequest httpServletRequest) {
        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can add new campus", null);
        }

        String error = validateCreateCampus(request);
        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        Account acc = accountRepo.save(Account.builder()
                .email(normalize(request.getEmail()))
                .role(Role.SCHOOL)
                .status(Status.ACCOUNT_ACTIVE)
                .registerDate(LocalDate.now())
                .firstLogin(false)
                .isRestricted(false)
                .build());

        Campus campus = campusRepo.save(Campus.builder()
                .school(actorCampus.getSchool())
                .account(acc)
                .name(normalize(request.getName()))
                .address(normalize(request.getAddress()))
                .phoneNumber(normalize(request.getPhone()))
                .status(Status.VERIFIED)
                .isPrimaryBranch(false)
                .build());

        Map<String, Object> data = new HashMap<>();
        data.put("campus", buildCampusData(campus));
        data.put("account", buildAccountData(acc));

        return ResponseBuilder.build(HttpStatus.OK, "Create campus successfully", data);
    }

    private String validateCreateCampus(CreateCampusRequest request) {
        if (request == null) {
            return "Request is required";
        }

        if (normalize(request.getEmail()) == null) {
            return "Email is required";
        }

        if (normalize(request.getEmail()).length() > 100) {
            return "Email exceeds 100 characters";
        }

        if (accountRepo.findByEmail(normalize(request.getEmail())).isPresent()) {
            return "Email is already in use";
        }

        if (normalize(request.getName()) == null) {
            return "Name is required";
        }

        if (normalize(request.getName()).length() > 50) {
            return "Name exceeds 50 characters";
        }

        if (normalize(request.getAddress()) == null) {
            return "Address is required";
        }

        if (normalize(request.getAddress()).length() > 250) {
            return "Address exceeds 250 characters";
        }

        if (normalize(request.getPhone()) == null) {
            return "Phone is required";
        }

        if (!normalize(request.getPhone()).matches("^(09|08|07|03)\\d{8}$")) {
            return "Phone must start with 09, 08, 07, or 03 and contain 10 digits";
        }

        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusList(HttpServletRequest httpServletRequest) {
        Campus actorCampus = extractActorCampus();

        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        List<Campus> campusList = actorCampus.getIsPrimaryBranch()
                ? campusRepo.findBySchoolId(actorCampus.getSchool().getId())
                : List.of(actorCampus);

        List<Map<String, Object>> data = campusList.stream()
                .map(this::buildCampusData)
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "View campus list successfully", data);
    }

    private Map<String, Object> buildCampusData(Campus campus) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", campus.getId());
        data.put("name", campus.getName());
        data.put("address", campus.getAddress());
        data.put("phoneNumber", campus.getPhoneNumber());
        data.put("status", campus.getStatus());
        data.put("isPrimaryBranch", campus.getIsPrimaryBranch());
        data.put("schoolId", campus.getSchool().getId());
        return data;
    }

    private Map<String, Object> buildAccountData(Account account) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", account.getEmail());
        data.put("registerDate", account.getRegisterDate());
        data.put("status", account.getStatus());
        data.put("role", account.getRole());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> createAdmissionCampaignTemplate(CreateAdmissionCampaignTemplateRequest request, HttpServletRequest httpServletRequest) {
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        if (!actorCampus.getIsPrimaryBranch()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can create campaign template", null);
        }

        if (request == null || normalize(request.getName()) == null || request.getStartDate() == null || request.getEndDate() == null || request.getYear() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campaign name, year, start date and end date are required", null);
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "End date must be after start date", null);
        }

        AdmissionCampaign campaign = admissionCampaignRepo.save(AdmissionCampaign.builder()
                .school(actorCampus.getSchool())
                .name(normalize(request.getName()))
                .description(normalize(request.getDescription()))
                .year(request.getYear())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Status.OPEN)
                .build());

        return ResponseBuilder.build(HttpStatus.OK, "Create campaign template successfully", buildCampaignData(campaign));
    }

    @Override
    public ResponseEntity<ResponseObject> createCampusProgramOffering(CreateCampusProgramOfferingRequest request, HttpServletRequest httpServletRequest) {
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        if (request == null || request.getAdmissionCampaignId() == null || request.getProgramId() == null || request.getLearningMode() == null || request.getQuota() <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campaign, program, learning mode and quota are required", null);
        }

        AdmissionCampaign campaign = admissionCampaignRepo.findById(request.getAdmissionCampaignId()).orElse(null);
        if (campaign == null || !campaign.getSchool().getId().equals(actorCampus.getSchool().getId())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campaign is out of your school scope", null);
        }

        Program program = programRepo.findById(request.getProgramId()).orElse(null);
        if (program == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Program not found", null);
        }

        Campus targetCampus = resolveTargetCampus(actorCampus, request.getCampusId());
        if (targetCampus == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus is out of your scope", null);
        }

        BigDecimal tuitionFee = request.getTuitionFee();
        if (tuitionFee == null || tuitionFee.signum() < 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tuition fee must be >= 0", null);
        }

        CampusProgramOffering offering = campusProgramOfferingRepo.save(CampusProgramOffering.builder()
                .campus(targetCampus)
                .admissionCampaign(campaign)
                .program(program)
                .quota(request.getQuota())
                .learningMode(request.getLearningMode())
                .priceAdjustmentPercentage(0)
                .tuitionFee(tuitionFee)
                .status(Status.OPEN)
                .build());

        return ResponseBuilder.build(HttpStatus.OK, "Create campus offering successfully", buildOfferingData(offering));
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusProgramOfferingList(ViewCampusProgramOfferingRequest request, HttpServletRequest httpServletRequest) {
        Campus actorCampus = extractActorCampus();
        if (actorCampus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
        }

        Integer requestedCampusId = request == null ? null : request.getCampusId();

        List<CampusProgramOffering> offeringList;
        if (actorCampus.getIsPrimaryBranch()) {
            if (requestedCampusId == null) {
                offeringList = campusProgramOfferingRepo.findByAdmissionCampaignSchoolId(actorCampus.getSchool().getId());
            } else {
                Campus campus = campusRepo.findByIdAndSchoolId(requestedCampusId, actorCampus.getSchool().getId()).orElse(null);
                if (campus == null) {
                    return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Campus is out of your scope", null);
                }
                offeringList = campusProgramOfferingRepo.findByCampusId(campus.getId());
            }
        } else {
            if (requestedCampusId != null && !requestedCampusId.equals(actorCampus.getId())) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You can only view your campus data", null);
            }
            offeringList = campusProgramOfferingRepo.findByCampusId(actorCampus.getId());
        }

        List<Map<String, Object>> payload = offeringList.stream()
                .map(this::buildOfferingData)
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "View campus offering list successfully", payload);
    }

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }

    private Campus resolveTargetCampus(Campus actorCampus, Integer requestedCampusId) {
        if (!actorCampus.getIsPrimaryBranch()) {
            if (requestedCampusId != null && !requestedCampusId.equals(actorCampus.getId())) {
                return null;
            }
            return actorCampus;
        }

        Integer targetCampusId = requestedCampusId == null ? actorCampus.getId() : requestedCampusId;
        return campusRepo.findByIdAndSchoolId(targetCampusId, actorCampus.getSchool().getId()).orElse(null);
    }

    private Map<String, Object> buildCampaignData(AdmissionCampaign campaign) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", campaign.getId());
        data.put("name", campaign.getName());
        data.put("description", campaign.getDescription());
        data.put("year", campaign.getYear());
        data.put("startDate", campaign.getStartDate());
        data.put("endDate", campaign.getEndDate());
        data.put("status", campaign.getStatus());
        data.put("schoolId", campaign.getSchool().getId());
        return data;
    }

    private Map<String, Object> buildOfferingData(CampusProgramOffering offering) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", offering.getId());
        data.put("campusId", offering.getCampus().getId());
        data.put("campusName", offering.getCampus().getName());
        data.put("campaignId", offering.getAdmissionCampaign().getId());
        data.put("campaignName", offering.getAdmissionCampaign().getName());
        data.put("programId", offering.getProgram().getId());
        data.put("quota", offering.getQuota());
        data.put("learningMode", offering.getLearningMode());
        data.put("tuitionFee", offering.getTuitionFee());
        data.put("status", offering.getStatus());
        return data;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public ResponseEntity<ResponseObject> createAccountCounsellor(CreateAccountCounsellorRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewAccountCounsellorList() {
        return null;
    }

}
