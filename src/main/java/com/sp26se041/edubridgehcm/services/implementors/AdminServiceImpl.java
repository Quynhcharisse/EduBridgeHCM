package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.PersonalityTypeGroup;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.enums.SubjectType;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import com.sp26se041.edubridgehcm.models.Subject;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRegistrationRequestRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.requests.AddSubjectRequest;
import com.sp26se041.edubridgehcm.requests.CreatePersonalityTypeRequest;
import com.sp26se041.edubridgehcm.requests.CreateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.UpdateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStatusServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AdminService;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SchoolRegistrationRequestRepo schoolRegistrationRequestRepo;

    private final AccountRepo accountRepo;

    private final SchoolRepo schoolRepo;

    private final CampusRepo campusRepo;

    private final PersonalityTypeRepo personalityTypeRepo;
    private final SubjectRepo subjectRepo;

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> verifyRegistration(int requestId) {

        if (requestId <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "requestId must be greater than 0", null);
        }

        SchoolRegistrationRequest request = schoolRegistrationRequestRepo.findById(requestId).orElse(null);

        if (request == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "No registration request with ID found: " + requestId, null);

        }

        String error = validationVerifyRegistration(requestId, request);
        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        return handleVerify(request);
    }

    private String validationVerifyRegistration(int requestId, SchoolRegistrationRequest request) {

        if (request == null) {
            return "No registration request with ID found: " + requestId;
        }

        if (request.getStatus() != Status.ACCOUNT_PENDING_VERIFY) {
            return "This request has been processed previously.";
        }

        if (schoolRepo.existsByTaxCode(request.getTaxCode().trim())) {
            return "This tax identification number already exists.";
        }

        return "";
    }

    private ResponseEntity<ResponseObject> handleVerify(SchoolRegistrationRequest request) {

        // tạo account
        Account account = accountRepo.save(Account.builder()
                .role(Role.SCHOOL)
                .email(request.getEmail().trim())
                .registerDate(LocalDate.now())
                .status(Status.ACCOUNT_ACTIVE)
                .firstLogin(true)
                .build());

        // tạo school (lấy thẳng từ bảng tạm)
        School school = schoolRepo.save(School.builder()
                .name(request.getSchoolName().trim())
                .description(request.getDescription().trim())
                .taxCode(request.getTaxCode().trim())
                .websiteUrl(request.getWebsiteUrl())
                .logoUrl(request.getLogoUrl())
                .representativeName(request.getRepresentativeName())
                .hotline(request.getHotline())
                .foundingDate(request.getFoundingDate())
                .businessLicenseUrl(request.getBusinessLicenseUrl())
                .build());

        // tạo campus đầu tiên (primary branch)
        campusRepo.save(Campus.builder()
                .account(account)
                .name(request.getCampusName().trim())
                .phoneNumber(request.getCampusPhone())
                .address(request.getCampusAddress().trim())
                .status(Status.ACCOUNT_ACTIVE)
                .isPrimaryBranch(true)
                .school(school)
                .build());

        // đánh dấu bảng tạm đã duyệt
        request.setStatus(Status.VERIFIED);
        schoolRegistrationRequestRepo.save(request);

        return ResponseBuilder.build(HttpStatus.OK, "Verified successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewSchoolRegistrationList() {

        List<SchoolRegistrationRequest> schoolRegistrationRequestList = schoolRegistrationRequestRepo.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> data = schoolRegistrationRequestList.stream()
                .map(this::buildRegistrationData)
                .toList();

        return ResponseBuilder.build(HttpStatus.OK, "View school registration list successfully", data);
    }

    private Map<String, Object> buildRegistrationData(SchoolRegistrationRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", request.getId());
        data.put("schoolName", request.getSchoolName());
        data.put("taxCode", request.getTaxCode());
        data.put("websiteUrl", request.getWebsiteUrl());
        data.put("logoUrl", request.getLogoUrl());
        data.put("foundingDate", request.getFoundingDate());
        data.put("representativeName", request.getRepresentativeName());
        data.put("hotline", request.getHotline());
        data.put("businessLicenseUrl", request.getBusinessLicenseUrl());
        data.put("campusName", request.getCampusName());
        data.put("campusAddress", request.getCampusAddress());
        data.put("campusPhone", request.getCampusPhone());
        data.put("status", request.getStatus());
        data.put("createdAt", request.getCreatedAt());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> createServicePackageFee(CreateServicePackageFeeRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updateServicePackageFee(UpdateServicePackageFeeRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewServicePackageFeeList() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updateStatusServicePackageFee(UpdateStatusServicePackageFeeRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> createPersonalityType(CreatePersonalityTypeRequest request) {

        String error = validateCreatePersonalityType(request);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        if (personalityTypeRepo.existsByCode(request.getCode())) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Personality type code already exists in the system", null);
        }

        PersonalityTypeGroup group;
        try {
            group = parsePersonalityTypeGroup(request.getPersonalityTypeGroup());
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        PersonalityType personalityType = PersonalityType.builder()
                .personalityTypeGroup(group)
                .name(normalize(request.getName()))
                .description(normalize(request.getDescription()))
                .code(normalize(request.getCode()))
                .quote(request.getQuoteInfo())
                .traits(request.getTraits())
                .strengths(request.getStrengths())
                .weaknesses(request.getWeaknesses())
                .sources(request.getSources())
                .recommendedCareers(request.getRecommendedCareers())
                .build();

        personalityTypeRepo.save(personalityType);

        return ResponseBuilder.build(HttpStatus.OK, "Create personality type successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getPersonalityTypeList() {

        List<PersonalityType> personalityTypes = personalityTypeRepo.findAll();

        Map<String, List<PersonalityType>> result = new LinkedHashMap<>();

        for (PersonalityType p : personalityTypes) {
            String group = p.getPersonalityTypeGroup().getValue();
            result
                    .computeIfAbsent(group, k -> new ArrayList<>())
                    .add(p);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Get personality types successfully", result);
    }

    @Override
    public ResponseEntity<ResponseObject> createSubject(AddSubjectRequest request) {

        String error = validateAddSubjectInfo(request);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        SubjectType subjectType;

        try {
            subjectType = parseSubjectType(request.getSubjectType());
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
        Subject subject = Subject.builder()
                .type(subjectType)
                .name(normalize(request.getName()))
                .build();

        subjectRepo.save(subject);

        return ResponseBuilder.build(HttpStatus.OK, "Create subject successfully", subject);
    }

    @Override
    public ResponseEntity<ResponseObject> getAllSubjects() {

        List<Subject> subjects = subjectRepo.findAll();

        List<Map<String, Object>> result = subjects.stream()
                .collect(Collectors.groupingBy(Subject::getType))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> groupMap = new HashMap<>();

                    // 🔥 dùng value thay vì name()
                    groupMap.put("type", entry.getKey().getValue());

                    // label cho FE
                    String label = switch (entry.getKey()) {
                        case REGULAR_SUBJECT -> "Môn học chính";
                        case FOREIGN_LANGUAGE_SUBJECT -> "Ngoại ngữ";
                    };
                    groupMap.put("label", label);

                    List<Map<String, Object>> subjectList = entry.getValue().stream()
                            .map(s -> {
                                Map<String, Object> item = new HashMap<>();
                                item.put("id", s.getId());
                                item.put("name", s.getName());
                                return item;
                            })
                            .toList();

                    groupMap.put("subjects", subjectList);

                    return groupMap;
                })
                .toList();

        return ResponseBuilder.build(
                HttpStatus.OK,
                "Get all subjects successfully",
                result
        );
    }

    private String validateAddSubjectInfo(AddSubjectRequest request) {

        if (isBlank(request.getName())) {
            return "Subject name must not be blank";
        }
        if (request.getSubjectType() == null || request.getSubjectType().isBlank()) {
            return "Subject type must not be blank";
        }
        return "";
    }

    private SubjectType parseSubjectType(String subjectType) {
        try {
            return SubjectType.valueOf(subjectType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid subject type. Allowed values: REGULAR_SUBJECT, FOREIGN_LANGUAGE_SUBJECT"
            );
        }
    }

    private PersonalityTypeGroup parsePersonalityTypeGroup(String group) {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("Personality type group must not be blank");
        }

        try {
            return PersonalityTypeGroup.valueOf(group.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid personality type group. Allowed values: ANALYST, DIPLOMAT, SENTINEL, EXPLORER"
            );
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }


    private String validateCreatePersonalityType(CreatePersonalityTypeRequest request) {

        if (request == null) {
            return "Request must not be null";
        }

        if (isBlank(request.getCode())) {
            return "Code must not be blank";
        }

        if (isBlank(request.getName())) {
            return "Name must not be blank";
        }

        if (isBlank(request.getDescription())) {
            return "Description must not be blank";
        }

        if (request.getQuoteInfo() == null) {
            return "QuoteInfo must not be null";
        }

        if (isBlank(request.getQuoteInfo().getAuthor())) {
            return "Author must not be blank";
        }

        if (isBlank(request.getQuoteInfo().getContent())) {
            return "Content must not be blank";
        }

        if (request.getTraits().size() != 4) {
            return
                    "Traits must contain exactly 4 items";
        }

        for (int i = 0; i < request.getTraits().size(); i++) {
            if (isBlank(request.getTraits().get(i).getName())) {
                return "Trait name at index [" + i + "] must not be blank";
            }
            if (isBlank(request.getTraits().get(i).getDescription())) {
                return "Trait description at index [" + i + "] must not be blank";
            }
        }

        if (request.getStrengths().isEmpty()) {
            return "Strengths must not be empty";
        }

        for (int j = 0; j < request.getStrengths().size(); j++) {
            if (isBlank(request.getStrengths().get(j))) {
                return "Strength at index [" + j + "] must not be blank";
            }
        }

        for (int i = 0; i < request.getWeaknesses().size(); i++) {
            if (isBlank(request.getWeaknesses().get(i))) {
                return "Weakness at index [" + i + "] must not be blank";
            }
        }

        for (int i = 0; i < request.getSources().size(); i++) {
            if (isBlank(request.getSources().get(i).getTitle())) {
                return "Display name source at index [" + i + "] must not be blank";
            }
            if (isBlank(request.getSources().get(i).getUrl())) {
                return "Url source at index [" + i + "] must not be blank";
            }
        }

        if (request.getRecommendedCareers().isEmpty()) {
            return "Recommended careers must not be empty";
        }

        for (int j = 0; j < request.getRecommendedCareers().size(); j++) {
            if (isBlank(request.getRecommendedCareers().get(j).getName())) {
                return "Recommended career name at index [" + j + "] must not be blank";
            }
            if (isBlank(request.getRecommendedCareers().get(j).getExplainText())) {
                return "Explain text for recommended career at index [" + j + "] must not be blank";
            }
        }

        return "";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
