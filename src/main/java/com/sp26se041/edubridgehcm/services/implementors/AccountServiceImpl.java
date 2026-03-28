package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.Relationship;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.*;
import com.sp26se041.edubridgehcm.repositories.*;
import com.sp26se041.edubridgehcm.requests.RestrictionRequest;
import com.sp26se041.edubridgehcm.requests.UpdateProfileRequest;
import com.sp26se041.edubridgehcm.responses.PageResponse;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AccountService;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.utils.*;
import com.sp26se041.edubridgehcm.validations.account.AccountValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final JWTService jWTService;

    private final AccountRepo accountRepo;

    private final CampusRepo campusRepo;

    private final CounsellorRepo counsellorRepo;

    private final ParentRepo parentRepo;

    private final SchoolRepo schoolRepo;

    @Override
    public ResponseEntity<ResponseObject> logout(HttpServletRequest request, HttpServletResponse response) {
        if (AuthRequestUtil.isMobileRequest(request)) {
            if (AuthRequestUtil.extractAuthenticatedAccount() == null) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Logout failed", null);
            }

            return ResponseBuilder.build(HttpStatus.OK, "Logout successfully", null);
        }

        String refreshToken = AuthRequestUtil.extractRefreshToken(request, null);

        if (refreshToken == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Logout failed", null);
        }

        if (!jWTService.checkIfNotExpired(refreshToken)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Token invalid", null);
        }

        CookieUtil.removeCookie(response);

        return ResponseBuilder.build(HttpStatus.OK, "Logout successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getAccessToken(HttpServletRequest request) {
        String accessToken = AuthRequestUtil.extractAccessToken(request);

        if (accessToken == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "No access", null);
        }

        Account account = AuthRequestUtil.extractAuthenticatedAccount();

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "No account", null);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("access", accessToken);
        data.put("id", account.getId());
        data.put("email", account.getEmail());
        data.put("role", account.getRole());

        return ResponseBuilder.build(HttpStatus.OK, "", data);
    }

    @Override
    public ResponseEntity<ResponseObject> toggleAccountRestriction(int accountId, RestrictionRequest request) {
        Account account = accountRepo.findById(accountId).orElse(null);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Account not found", null);
        }

        if (account.isRestricted() == request.getIsRestricted()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, request.getIsRestricted() ? "Account is already restricted" : "Account is already unrestricted", null);
        }

        if (request.getIsRestricted()) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Reason is required when restricting an account", null);
            }

            account.setIsRestricted(true);
            account.setStatus(Status.ACCOUNT_RESTRICTED);
            account.setRestrictionReason(request.getReason().trim());
            account.setRestrictionDate(LocalDateTime.now());
        } else {
            account.setIsRestricted(false);
            account.setRestrictionReason(null);
            account.setRestrictionDate(null);
            account.setStatus(Status.ACCOUNT_ACTIVE);
        }

        accountRepo.save(account);

        return ResponseBuilder.build(HttpStatus.OK, request.getIsRestricted() ? "Account restricted successfully" : "Account unrestricted successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewUserList(String role, int page, int pageSize) {

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Role targetRole = parseSupportedUserListRole(role);
        if (targetRole == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Role must be PARENT or SCHOOL", null);
        }

        if (targetRole == Role.PARENT) {
            Page<Account> parentPage = accountRepo.findByRoleOrderByIdDesc(Role.PARENT, pageable);
            PageResponse<Map<String, Object>> response = PaginationUtil.buildPageResponse(parentPage, this::mapParentSummary);
            return ResponseBuilder.build(HttpStatus.OK, "View parent list successfully", response);
        }

        Page<School> schoolPage = schoolRepo.findAllByOrderByIdDesc(pageable);
        List<Integer> schoolIds = schoolPage.getContent().stream()
                .map(School::getId)
                .toList();

        Map<Integer, List<Campus>> campusesBySchoolId = schoolIds.isEmpty()
                ? Collections.emptyMap()
                : campusRepo.findBySchoolIdIn(schoolIds).stream()
                .collect(Collectors.groupingBy(campus -> campus.getSchool().getId()));

        PageResponse<Map<String, Object>> response = PaginationUtil.buildPageResponse(
                schoolPage,
                school -> mapSchoolSummary(school, campusesBySchoolId.getOrDefault(school.getId(), Collections.emptyList()))
        );

        return ResponseBuilder.build(HttpStatus.OK, "View school list successfully", response);
    }

    @Override
    public ResponseEntity<ResponseObject> viewSchoolCampusList(int schoolId, int page, int pageSize) {

        if (schoolId <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "School id must be greater than 0", null);
        }

        School school = schoolRepo.findById(schoolId).orElse(null);
        if (school == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "School not found", null);
        }

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<Campus> campusPage = campusRepo.findBySchoolIdOrderByIsPrimaryBranchDescIdDesc(schoolId, pageable);
        PageResponse<Map<String, Object>> response = PaginationUtil.buildPageResponse(campusPage, this::mapCampusSummary);

        return ResponseBuilder.build(HttpStatus.OK, "View school campus list successfully", response);
    }

    @Override
    public ResponseEntity<ResponseObject> viewCampusCounsellorList(int campusId, int page, int pageSize) {

        if (campusId <= 0) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Campus id must be greater than 0", null);
        }

        Campus campus = campusRepo.findById(campusId).orElse(null);
        if (campus == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Campus not found", null);
        }

        Pageable pageable;
        try {
            pageable = PaginationUtil.buildPageRequest(page, pageSize);
        } catch (IllegalArgumentException e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }

        Page<Counsellor> counsellorPage = counsellorRepo.findByCampusIdOrderByIdDesc(campusId, pageable);
        PageResponse<Map<String, Object>> response = PaginationUtil.buildPageResponse(counsellorPage, this::mapCounsellorItem);

        return ResponseBuilder.build(HttpStatus.OK, "View campus counsellor list successfully", response);
    }

    private Map<String, Object> mapGeneralInfoUser(Account acc) {
        Map<String, Object> data = new HashMap<>();
        data.put("accId", acc.getId());
        data.put("email", acc.getEmail());
        data.put("role", acc.getRole());
        data.put("status", acc.getStatus());
        data.put("isRestricted", acc.isRestricted());
        data.put("restrictionReason", acc.getRestrictionReason());
        data.put("registerDate", acc.getRegisterDate());
        return data;
    }

    private Role parseSupportedUserListRole(String value) {
        String normalizedValue = normalize(value);

        if (normalizedValue == null) {
            return null;
        }

        if (Role.PARENT.name().equalsIgnoreCase(normalizedValue)) {
            return Role.PARENT;
        }

        if (Role.SCHOOL.name().equalsIgnoreCase(normalizedValue)) {
            return Role.SCHOOL;
        }

        return null;
    }

    private Map<String, Object> mapParentSummary(Account acc) {
        Map<String, Object> data = mapGeneralInfoUser(acc);

        Parent parent = acc.getParent();
        data.put("name", parent != null ? parent.getName() : null);
        data.put("phone", parent != null ? parent.getPhone() : null);
        data.put("gender", parent != null ? parent.getGender() : null);
        data.put("relationship", parent != null ? parent.getRelationship() : null);
        data.put("idCardNumber", parent != null ? parent.getIdCardNumber() : null);
        data.put("workplace", parent != null ? parent.getWorkplace() : null);
        data.put("occupation", parent != null ? parent.getOccupation() : null);
        data.put("currentAddress", parent != null ? parent.getCurrentAddress() : null);

        return data;
    }

    private Map<String, Object> mapSchoolSummary(School school, List<Campus> campuses) {
        List<Campus> safeCampuses = campuses == null ? Collections.emptyList() : campuses;
        Campus primaryCampus = safeCampuses.stream()
                .filter(campus -> Boolean.TRUE.equals(campus.getIsPrimaryBranch()))
                .findFirst()
                .orElse(null);

        Map<String, Object> data = new HashMap<>();
        data.put("schoolId", school.getId());
        data.put("schoolName", school.getName());
        data.put("schoolDescription", school.getDescription());
        data.put("taxCode", school.getTaxCode());
        data.put("websiteUrl", school.getWebsiteUrl());
        data.put("hotline", school.getHotline());
        data.put("representativeName", school.getRepresentativeName());
        data.put("logoUrl", school.getLogoUrl());
        data.put("foundingDate", school.getFoundingDate());
        data.put("overallStatus", SchoolUtil.checkSchoolStatus(school));
        data.put("primaryCampus", mapPrimaryCampusSummary(primaryCampus));
        data.put("campusCount", safeCampuses.size());
        data.put("counsellorCount", counsellorRepo.countByCampusSchoolId(school.getId()));
        return data;
    }

    private Map<String, Object> mapPrimaryCampusSummary(Campus campus) {

        if (campus == null) {
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("campusId", campus.getId());
        data.put("campusName", campus.getName());
        data.put("address", campus.getAddress());
        data.put("phoneNumber", campus.getPhoneNumber());
        data.put("status", campus.getStatus());
        data.put("isPrimaryBranch", campus.getIsPrimaryBranch());
        return data;
    }

    private Map<String, Object> mapCampusSummary(Campus campus) {
        Map<String, Object> data = new HashMap<>();
        data.put("campusId", campus.getId());
        data.put("campusName", campus.getName());
        data.put("address", campus.getAddress());
        data.put("phoneNumber", campus.getPhoneNumber());
        data.put("city", campus.getCity());
        data.put("district", campus.getDistrict());
        data.put("latitude", campus.getLatitude());
        data.put("longitude", campus.getLongitude());
        data.put("boardingType", campus.getBoardingType());
        data.put("status", campus.getStatus());
        data.put("isPrimaryBranch", campus.getIsPrimaryBranch());
        data.put("account", buildAccountSummary(campus.getAccount()));
        return data;
    }

    private Map<String, Object> buildAccountSummary(Account acc) {

        if (acc == null) {
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("accountId", acc.getId());
        data.put("email", acc.getEmail());
        data.put("status", acc.getStatus());
        data.put("isRestricted", acc.isRestricted());
        return data;
    }

    private Map<String, Object> mapCounsellorItem(Counsellor counsellor) {
        Map<String, Object> item = new HashMap<>();
        item.put("counsellorId", counsellor.getId());
        item.put("name", counsellor.getName());
        item.put("employeeCode", counsellor.getEmployeeCode());
        item.put("campusId", counsellor.getCampus() != null ? counsellor.getCampus().getId() : null);
        item.put("campusName", counsellor.getCampus() != null ? counsellor.getCampus().getName() : null);

        Account acc = counsellor.getAccount();
        item.put("accountId", acc != null ? acc.getId() : null);
        item.put("email", acc != null ? acc.getEmail() : null);
        item.put("status", acc != null ? acc.getStatus() : null);
        return item;
    }

    @Override
    public ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request, HttpServletRequest httpRequest) {

        Account account = AuthRequestUtil.extractAuthenticatedAccount();

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No account", null);
        }

        String error = AccountValidation.updateProfileValidation(request, account);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        if (account.getRole() == Role.PARENT) {
            updateParentProfile(account.getParent(), request.getParentData(), account.getFirstLogin());
        }

        if (account.getRole() == Role.COUNSELLOR) {
            updateCounsellorProfile(account.getCounsellor(), request.getCounsellorData());
        }

        if (account.getRole() == Role.SCHOOL) {
            updateCampusProfile(account.getCampus(), request.getCampusData());
        }

        account.setFirstLogin(false);
        accountRepo.save(account);

        return ResponseBuilder.build(HttpStatus.OK, "Update profile successfully", null);
    }

    private void updateParentProfile(Parent parent, UpdateProfileRequest.ParentData parentData, boolean isFirstLogin) {

        parent.setName(parentData.getName());
        parent.setGender(Gender.valueOf(parentData.getGender()));
        parent.setRelationship(Relationship.valueOf(parentData.getRelationship()));
        parent.setPhone(parentData.getPhone());
        parent.setWorkplace(parentData.getWorkplace());
        parent.setOccupation(parentData.getOccupation());
        parent.setCurrentAddress(parentData.getCurrentAddress());

        if (isFirstLogin) {
            parent.setIdCardNumber(normalize(parentData.getIdCardNumber()));
        }

        parentRepo.save(parent);
    }

    private void updateCounsellorProfile(Counsellor counsellor, UpdateProfileRequest.CounsellorData counsellorData) {
        counsellor.setName(normalize(counsellorData.getName()));
        counsellorRepo.save(counsellor);
    }

    private void updateCampusProfile(Campus campus, UpdateProfileRequest.CampusData campusData) {

        if (campus.getIsPrimaryBranch()) {
            campus.getSchool().setDescription(normalize(campusData.getSchoolData().getDescription()));
            campus.getSchool().setLogoUrl(normalize(campusData.getSchoolData().getLogoUrl()));
            campus.getSchool().setWebsiteUrl(normalize(campusData.getSchoolData().getWebsiteUrl()));
            campus.getSchool().setRepresentativeName(normalize(campusData.getSchoolData().getRepresentativeName()));
            campus.getSchool().setHotline(normalize(campusData.getSchoolData().getHotline()));
            campus.getSchool().setFoundingDate(campusData.getSchoolData().getFoundingDate());
        }

        campus.setName(normalize(campusData.getName()));
        campus.setPhoneNumber(normalize(campusData.getPhoneNumber()));
        campus.setAddress(normalize(campusData.getAddress()));

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("coverUrl", campusData.getImageJson().getCoverUrl());

        imageMap.put("itemList", campusData.getImageJson().getItemList().stream()
                .map(item -> {
                    Map<String, Object> dataItem = new HashMap<>();
                    dataItem.put("name", normalize(item.getName()));
                    dataItem.put("url", normalize(item.getUrl()));
                    dataItem.put("altName", normalize(item.getAltName()));
                    dataItem.put("uploadDate", item.getUploadDate());
                    dataItem.put("isUsage", item.getIsUsage());
                    return dataItem;
                })
                .collect(Collectors.toList())
        );
        campus.setImageJson(imageMap);

        Map<String, Object> facilityMap = new HashMap<>();
        facilityMap.put("overview", campusData.getFacilityJson().getOverview());

        facilityMap.put("itemList", campusData.getFacilityJson().getItemList().stream()
                .map(item -> {
                    Map<String, Object> dataItem = new HashMap<>();
                    dataItem.put("facilityCode", normalize(item.getFacilityCode()));
                    dataItem.put("name", normalize(item.getName()));
                    dataItem.put("value", normalize(item.getValue()));
                    dataItem.put("unit", normalize(item.getUnit()));
                    dataItem.put("category", normalize(item.getCategory()));
                    return dataItem;
                })
                .collect(Collectors.toList())
        );
        campus.setFacility(facilityMap);
        campus.setPolicyDetail(normalize(campusData.getPolicyDetail()));

        campusRepo.save(campus);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request) {

        Account account = AuthRequestUtil.extractAuthenticatedAccount();

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No account", null);
        }

        return ResponseBuilder.build(HttpStatus.OK, "View profile successfully", buildProfileData(account));
    }

    private Map<String, Object> buildProfileData(Account account) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", account.getEmail());
        data.put("role", account.getRole());
        data.put("status", account.getStatus());
        data.put("firstLogin", account.getFirstLogin());

        if (account.getRole() == Role.PARENT) {
            data.put("parent", buildParentProfileData(account.getParent()));
        }

        if (account.getRole() == Role.COUNSELLOR) {
            data.put("counsellor", buildCounsellorProfileData(account.getCounsellor()));
        }

        if (account.getRole() == Role.SCHOOL) {
            data.put("campus", buildCampusProfileData(account.getCampus()));
        }

        return data;
    }

    private Map<String, Object> buildParentProfileData(Parent parent) {

        if (parent == null) return null;

        Map<String, Object> parentData = new HashMap<>();
        parentData.put("name", parent.getName());
        parentData.put("gender", parent.getGender());
        parentData.put("phone", parent.getPhone());
        parentData.put("relationship", parent.getRelationship());
        parentData.put("occupation", parent.getOccupation());
        parentData.put("workplace", parent.getWorkplace());
        parentData.put("currentAddress", parent.getCurrentAddress());
        parentData.put("idCardNumber", parent.getIdCardNumber());
        return parentData;
    }

    private Map<String, Object> buildCounsellorProfileData(Counsellor counsellor) {

        if (counsellor == null) return null;

        Map<String, Object> counsellorData = new HashMap<>();
        counsellorData.put("name", counsellor.getName());
        counsellorData.put("employeeCode", counsellor.getEmployeeCode());
        counsellorData.put("campusId", counsellor.getCampus().getId());
        counsellorData.put("campusName", counsellor.getCampus().getName());
        return counsellorData;
    }

    private Map<String, Object> buildCampusProfileData(Campus campus) {

        if (campus == null) return null;

        Map<String, Object> campusData = new HashMap<>();
        campusData.put("name", campus.getName());
        campusData.put("phoneNumber", campus.getPhoneNumber());
        campusData.put("address", campus.getAddress());
        campusData.put("status", campus.getStatus());
        campusData.put("isPrimaryBranch", campus.getIsPrimaryBranch());

        if (campus.getIsPrimaryBranch()) {
            campusData.put("schoolName", campus.getSchool().getName());
            campusData.put("schoolDescription", campus.getSchool().getDescription());
            campusData.put("taxCode", campus.getSchool().getTaxCode());
            campusData.put("logoUrl", campus.getSchool().getLogoUrl());
            campusData.put("websiteUrl", campus.getSchool().getWebsiteUrl());
            campusData.put("representativeName", campus.getSchool().getRepresentativeName());
            campusData.put("hotline", campus.getSchool().getHotline());
            campusData.put("businessLicenseUrl", campus.getSchool().getBusinessLicenseUrl());
            campusData.put("foundingDate", campus.getSchool().getFoundingDate());
        }

        campusData.put("imageJson", campus.getImageJson());
        campusData.put("facility", campus.getFacility());
        return campusData;
    }
}
