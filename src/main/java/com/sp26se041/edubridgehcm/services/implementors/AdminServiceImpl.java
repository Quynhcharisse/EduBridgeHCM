package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.ParentPostPermission;
import com.sp26se041.edubridgehcm.enums.PersonalityTypeGroup;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.enums.SubjectType;
import com.sp26se041.edubridgehcm.enums.SupportLevel;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.PersonalityType;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import com.sp26se041.edubridgehcm.models.Subject;
import com.sp26se041.edubridgehcm.models.Subscription;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.PersonalityTypeRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRegistrationRequestRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.repositories.SubjectRepo;
import com.sp26se041.edubridgehcm.repositories.SubscriptionRepo;
import com.sp26se041.edubridgehcm.requests.AddSubjectRequest;
import com.sp26se041.edubridgehcm.requests.CreatePersonalityTypeRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStatusServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.UpsertServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AdminService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.admin.SubscriptionValidation;
import com.sp26se041.edubridgehcm.validations.admin.VerifyRegistrationValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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


    private final SupabaseStorageService supabaseStorageService;

    private final SubscriptionRepo subscriptionRepo;


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

        String error = VerifyRegistrationValidation.validationVerifyRegistration(requestId, request, schoolRepo);
        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        return handleVerify(request);
    }


    private ResponseEntity<ResponseObject> handleVerify(SchoolRegistrationRequest request) {

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("name", request.getSchoolName().trim());
        fields.put("description", request.getDescription().trim());
        fields.put("taxCode", request.getTaxCode().trim());
        fields.put("websiteUrl", request.getWebsiteUrl());
        fields.put("representativeName", request.getRepresentativeName());
        fields.put("hotline", request.getHotline());
        fields.put("foundingDate", request.getFoundingDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        fields.put("logoUrl", request.getLogoUrl());
        fields.put("businessLicenseUrl", request.getBusinessLicenseUrl());

        String schoolName = toSafeObjectKey(request.getSchoolName());

        String fileName = schoolName + "_info.pdf";

        try {
            supabaseStorageService.generatePdfFileFromTemplateDocx(fields, "TEMPLATE/school_info_template.docx", schoolName, fileName);

            String objectPath = supabaseStorageService.extractObjectPath(request.getBusinessLicenseUrl());
            supabaseStorageService.moveFile(objectPath, schoolName + "/business_license_" + schoolName + ".pdf");

        } catch (Exception e) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), null);
        }


        // tạo account
        Account account = accountRepo.save(Account.builder().role(Role.SCHOOL).email(request.getEmail().trim()).registerDate(LocalDate.now()).status(Status.ACCOUNT_ACTIVE).firstLogin(true).build());

        // tạo school (lấy thẳng từ bảng tạm)
        School school = schoolRepo.save(School.builder().name(request.getSchoolName().trim()).description(request.getDescription().trim()).taxCode(request.getTaxCode().trim()).websiteUrl(request.getWebsiteUrl()).logoUrl(request.getLogoUrl()).representativeName(request.getRepresentativeName()).hotline(request.getHotline()).foundingDate(request.getFoundingDate()).businessLicenseUrl(request.getBusinessLicenseUrl()).build());


        // tạo campus đầu tiên (primary branch)
        campusRepo.save(Campus.builder().account(account).name(request.getCampusName().trim()).phoneNumber(request.getCampusPhone()).address(request.getCampusAddress().trim()).status(Status.ACCOUNT_ACTIVE).isPrimaryBranch(true).school(school).build());

        // đánh dấu bảng tạm đã duyệt
        request.setStatus(Status.VERIFIED);
        schoolRegistrationRequestRepo.save(request);

        return ResponseBuilder.build(HttpStatus.OK, "Verified successfully", null);
    }

    private String toSafeObjectKey(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }


        // 1. normalize Unicode (tách dấu ra)
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);

        // 2. remove dấu (accent)
        String noAccent = normalized.replaceAll("\\p{M}", "");

        // 3. xử lý riêng đ/Đ
        noAccent = noAccent.replace("đ", "d").replace("Đ", "d");

        // 4. lowercase
        String lower = noAccent.toLowerCase(Locale.ROOT);

        // 5. replace ký tự không hợp lệ -> _
        String safe = lower.replaceAll("[^a-z0-9]+", "_");

        // 6. cleanup: nhiều _ -> 1
        safe = safe.replaceAll("_+", "_");

        // 7. remove _ đầu/cuối
        safe = safe.replaceAll("^_+|_+$", "");

        return safe;
    }


    @Override
    public ResponseEntity<ResponseObject> viewSchoolRegistrationList() {

        List<SchoolRegistrationRequest> schoolRegistrationRequestList = schoolRegistrationRequestRepo.findAllByOrderByCreatedAtDesc();

        List<Map<String, Object>> data = schoolRegistrationRequestList.stream().map(this::buildRegistrationData).toList();

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
    @Transactional
    public ResponseEntity<ResponseObject> upsertServicePackageFee(UpsertServicePackageFeeRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Subscription subscription;

        String error = SubscriptionValidation.upsertSubscriptionValidation(request);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, error, null);
        }

        boolean isCreate = (request.getPackageId() == null);

        if (!isCreate) {
            subscription = subscriptionRepo.findById(request.getPackageId()).orElse(null);

            if (subscription == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Package not found", null);
            }

            if (subscription.getPackageStatus() != Status.PACKAGE_DRAFT) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Cannot edit. Only DRAFT packages can be modified.", null);
            }
        } else {
            subscription = new Subscription();
            subscription.setPackageStatus(Status.PACKAGE_DRAFT);
        }

        subscription.setName(request.getName());
        subscription.setDescription(request.getDescription());
        subscription.setPrice(request.getPrice());
        subscription.setDurationDays(request.getDurationDays());

        if (request.getFeatureData() != null) {
            subscription.setFeatures(buildFeatureJson(request.getFeatureData()));
        }

        subscriptionRepo.save(subscription);

        return ResponseBuilder.build(isCreate ? HttpStatus.CREATED : HttpStatus.OK, isCreate ? "Create draft package successfully" : "Update draft package successfully", null);
    }

    private Map<String, Object> buildFeatureJson(UpsertServicePackageFeeRequest.FeatureData request) {
        Map<String, Object> data = new HashMap<>();
        data.put("maxCounsellors", request.getMaxCounsellors());
        data.put("maxAdmissions", request.getMaxAdmissions());
        data.put("allowChat", request.getAllowChat());
        data.put("parentPostPermission", ParentPostPermission.valueOf(request.getParentPostPermission()));
        data.put("isFeatured", request.getIsFeatured());
        data.put("topRanking", request.getTopRanking());
        data.put("supportLevel", SupportLevel.valueOf(request.getSupportLevel()));
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> viewServicePackageFeeList() {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        List<Subscription> subscriptions = subscriptionRepo.findAll();

        if (subscriptions.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.OK, "No service packages found", Collections.emptyList());
        }

        List<Map<String, Object>> data = subscriptions.stream()
                .map(this::buildSubscriptionData)
                .collect(Collectors.toList());

        return ResponseBuilder.build(HttpStatus.OK, "Get package fee list successfully", data);
    }

    private Map<String, Object> buildSubscriptionData(Subscription subscription) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", subscription.getId());
        data.put("name", subscription.getName());
        data.put("description", subscription.getDescription());
        data.put("status", subscription.getPackageStatus());
        data.put("features", subscription.getFeatures());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> publishServicePackageFee(Integer packageId) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Subscription subscription = subscriptionRepo.findById(packageId).orElse(null);

        if (subscription == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Subscription is not found", null);
        }

        if (!subscription.getPackageStatus().equals(Status.PACKAGE_DRAFT)) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Only DRAFT packages can be published. Current status: " + subscription.getPackageStatus(), null);
        }

        subscription.setPackageStatus(Status.PACKAGE_ACTIVE);
        subscriptionRepo.save(subscription);

        return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Subscription published successfully", null);
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

        if (subjectRepo.existsByName((normalize(request.getName())))) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "Subject already exists in the system", null);
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

    private Campus extractActorCampus() {
        Account account = AuthRequestUtil.extractAuthenticatedAccount();
        if (account == null || account.getRole() != Role.SCHOOL) {
            return null;
        }
        return account.getCampus();
    }

}
