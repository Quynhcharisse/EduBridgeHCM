package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.Relationship;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.requests.RestrictionRequest;
import com.sp26se041.edubridgehcm.requests.UpdateProfileRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AccountService;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.utils.SchoolUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

        if (account.isRestricted() == request.isRestricted()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, request.isRestricted() ? "Account is already restricted" : "Account is already unrestricted", null);
        }

        if (request.isRestricted()) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Reason is required when restricting an account", null);
            }
            account.setRestricted(true);
            account.setStatus(Status.ACCOUNT_RESTRICTED);
            account.setRestrictionReason(request.getReason().trim());
            account.setRestrictionDate(LocalDateTime.now());
        } else {
            account.setRestricted(false);
            account.setRestrictionReason(null);
            account.setRestrictionDate(null);
            account.setStatus(Status.ACCOUNT_ACTIVE);
        }

        accountRepo.save(account);

        return ResponseBuilder.build(HttpStatus.OK, request.isRestricted() ? "Account restricted successfully" : "Account unrestricted successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewUserList() {

        List<Account> accList = accountRepo.findAllByOrderByIdDesc();

        List<Map<String, Object>> parentList = accList.stream()
                .filter(acc -> acc.getRole() == Role.PARENT)
                .map(this::mapParentItem)
                .toList();

        List<Account> schoolAccounts = accList.stream()
                .filter(acc -> acc.getRole() == Role.SCHOOL)
                .toList();

        List<Integer> campusIds = schoolAccounts.stream()
                .map(Account::getCampus)
                .map(Campus::getId)
                .distinct()
                .toList();

        List<Counsellor> counsellorList = campusIds.isEmpty()
                ? Collections.emptyList()
                : counsellorRepo.findByCampusIdIn(campusIds);

        Map<Integer, List<Counsellor>> counsellorByCampusId = counsellorList.stream()
                .collect(Collectors.groupingBy(c -> c.getCampus().getId()));

        Map<Integer, Map<String, Object>> schoolMap = new HashMap<>();

        for (Account acc : schoolAccounts) {
            Campus campus = acc.getCampus();
            School school = campus.getSchool();
            Map<String, Object> schoolNode = schoolMap.computeIfAbsent(school.getId(), key -> {
                Map<String, Object> data = new HashMap<>();
                data.put("schoolId", school.getId());
                data.put("schoolName", school.getName());
                data.put("taxCode", school.getTaxCode());
                data.put("websiteUrl", school.getWebsiteUrl());
                data.put("hotline", school.getHotline());
                data.put("representativeName", school.getRepresentativeName());
                data.put("logoUrl", school.getLogoUrl());
                data.put("foundingDate", school.getFoundingDate());
                data.put("primaryCampus", null);
                data.put("branchCampuses", new ArrayList<Map<String, Object>>());
                data.put("overallStatus", SchoolUtil.checkSchoolStatus(school));
                return data;
            });

            Map<String, Object> campusNode = mapSchoolCampusItem(acc,
                    counsellorByCampusId.getOrDefault(campus.getId(), Collections.emptyList()));

            if (campus.getIsPrimaryBranch()) {
                schoolNode.put("primaryCampus", campusNode);
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> branchCampuses = (List<Map<String, Object>>) schoolNode.get("branchCampuses");
                branchCampuses.add(campusNode);
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("parents", parentList);
        body.put("schools", new ArrayList<>(schoolMap.values()));
        body.put("totalParents", parentList.size());
        body.put("totalSchools", schoolMap.size());

        return ResponseBuilder.build(HttpStatus.OK, "View user list successfully", body);
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

    private Map<String, Object> mapParentItem(Account acc) {
        Map<String, Object> data = mapGeneralInfoUser(acc);

        Parent parent = acc.getParent();
        Map<String, Object> detail = new HashMap<>();
        detail.put("name", parent.getName());
        detail.put("phone", parent.getPhone());
        detail.put("gender", parent.getGender());
        detail.put("relationship", parent.getRelationship());
        detail.put("occupation", parent.getOccupation());
        detail.put("workplace", parent.getWorkplace());
        detail.put("currentAddress", parent.getCurrentAddress());

        data.put("detail", detail);

        return data;
    }

    private Map<String, Object> mapSchoolCampusItem(Account acc, List<Counsellor> counsellorList) {
        Map<String, Object> data = mapGeneralInfoUser(acc);

        Campus campus = acc.getCampus();
        Map<String, Object> detail = new HashMap<>();
        detail.put("campusId", campus.getId());
        detail.put("campusName", campus.getName());
        detail.put("campusAddress", campus.getAddress());
        detail.put("campusPhoneNumber", campus.getPhoneNumber());
        detail.put("isPrimaryBranch", campus.getIsPrimaryBranch());
        detail.put("counsellorList", counsellorList.stream().map(this::mapCounsellorItem).toList());
        detail.put("counsellorSize", counsellorList.size());

        data.put("detail", detail);

        return data;
    }

    private Map<String, Object> mapCounsellorItem(Counsellor counsellor) {
        Map<String, Object> item = new HashMap<>();
        item.put("counsellorId", counsellor.getId());
        item.put("name", counsellor.getName());
        item.put("employeeCode", counsellor.getEmployeeCode());

        Account acc = counsellor.getAccount();
        item.put("accountId", acc.getId());
        item.put("email", acc.getEmail());
        item.put("status", acc.getStatus());
        return item;
    }

    @Override
    public ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request, HttpServletRequest httpRequest) {

        Account account = AuthRequestUtil.extractAuthenticatedAccount();

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No account", null);
        }

        String error = updateProfileValidation(request, account);

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
            campus.getSchool().setName(normalize(campusData.getSchoolData().getName()));
            campus.getSchool().setLogoUrl(normalize(campusData.getSchoolData().getLogoUrl()));
            campus.getSchool().setWebsiteUrl(normalize(campusData.getSchoolData().getWebsiteUrl()));
            campus.getSchool().setRepresentativeName(normalize(campusData.getSchoolData().getRepresentativeName()));
            campus.getSchool().setHotline(normalize(campusData.getSchoolData().getHotline()));
            campus.getSchool().setBusinessLicenseUrl(normalize(campusData.getSchoolData().getBusinessLicenseUrl()));
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
                    dataItem.put("isUsage", item.isUsage());
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

    private String updateProfileValidation(UpdateProfileRequest request, Account account) {

        if (account == null) {
            return "Account does not exist";
        }

        if (request == null) {
            return "Request body is required";
        }

        if (account.isRestricted()) {
            return "Your account is restricted";
        }

        if (account.getRole() == Role.ADMIN) {
            return "Admin does not support this profile update API";
        }

        if (account.getRole() == Role.PARENT) {

            if (request.getCounsellorData() != null || request.getCampusData() != null) {
                return "Only parentData is allowed for parent role";
            }

            if (request.getParentData() == null) {
                return "Require parent data";
            }

            String parentName = normalize(request.getParentData().getName());
            String parentPhone = normalize(request.getParentData().getPhone());
            String parentOccupation = normalize(request.getParentData().getOccupation());
            String parentWorkplace = normalize(request.getParentData().getWorkplace());
            String parentAddress = normalize(request.getParentData().getCurrentAddress());
            String idCardNumber = normalize(request.getParentData().getIdCardNumber());
            boolean isFirstLogin = account.getFirstLogin();

            if (isFirstLogin && idCardNumber == null) {
                return "Require parent id card number on first login";
            }

            if (idCardNumber != null && !isExactDigits(idCardNumber)) {
                return "Parent id card number must contain exactly 12 digits";
            }

            if (!isFirstLogin && idCardNumber != null && !idCardNumber.equals(account.getParent().getIdCardNumber())) {
                return "Parent id card number can only be updated on first login";
            }

            if (parentName == null) {
                return "Require parent name";
            }

            if (parseGender(request.getParentData().getGender()) == null) {
                return "Invalid parent gender";
            }

            if (parseRelationship(request.getParentData().getRelationship()) == null) {
                return "Invalid parent relationship";
            }

            if (parentPhone == null) {
                return "Require parent phone";
            }

            if (!isValidPhoneNumber(parentPhone)) {
                return "Parent phone number must contain exactly 10 digits and start with 03, 07, 08, or 09";
            }

            if (parentOccupation == null) {
                return "Require parent occupation";
            }

            if (hasMaxWords(parentOccupation)) {
                return "Parent occupation must not exceed 100 words";
            }

            if (parentWorkplace == null) {
                return "Require parent workplace";
            }

            if (hasMaxWords(parentWorkplace)) {
                return "Parent workplace must not exceed 100 words";
            }

            if (parentAddress == null) {
                return "Require parent address";
            }

            if (hasMaxWords(parentAddress)) {
                return "Parent address must not exceed 100 words";
            }

            return "";
        }

        if (account.getRole() == Role.COUNSELLOR) {

            if (request.getParentData() != null || request.getCampusData() != null) {
                return "Only counsellorData is allowed for counsellor role";
            }

            if (request.getCounsellorData() == null) {
                return "Require counsellor data";
            }

            if (normalize(request.getCounsellorData().getName()) == null) {
                return "Require counsellor name";
            }
            return "";
        }

        if (account.getRole() == Role.SCHOOL) {

            if (request.getParentData() != null || request.getCounsellorData() != null) {
                return "Only campusData is allowed for school role";
            }

            if (request.getCampusData() == null) {
                return "Require campus data";
            }

            if (normalize(request.getCampusData().getName()) == null) {
                return "Require campus name";
            }

            if (normalize(request.getCampusData().getPhoneNumber()) == null) {
                return "Require campus phone number";
            }

            if (!isValidPhoneNumber(request.getCampusData().getPhoneNumber())) {
                return "Campus phone number must contain exactly 10 digits and start with 03, 07, 08, or 09";
            }

            if (normalize(request.getCampusData().getAddress()) == null) {
                return "Require campus address";
            }

            if (hasMaxWords(request.getCampusData().getAddress())) {
                return "Campus address must not exceed 100 words";
            }

            if (request.getCampusData().getImageJson() == null) {
                return "Require campus imageJson";
            }

            if (request.getCampusData().getImageJson().getItemList() == null) {
                return "Require image itemList";
            }

            if (request.getCampusData().getImageJson().getItemList().stream().anyMatch(item -> item == null || normalize(item.getUrl()) == null)) {
                return "Invalid image item";
            }

            if (request.getCampusData().getFacilityJson() == null) {
                return "Require campus facilityJson";
            }

            if (request.getCampusData().getFacilityJson().getItemList() == null) {
                return "Require facility itemList";
            }

            if (request.getCampusData().getFacilityJson().getItemList().stream().anyMatch(item -> item == null || normalize(item.getFacilityCode()) == null || normalize(item.getName()) == null)) {
                return "Invalid facility item";
            }

            if (account.getCampus().getIsPrimaryBranch()) {
                if (request.getCampusData().getSchoolData() == null) {
                    return "Require school data for primary branch";
                }

                if (normalize(request.getCampusData().getSchoolData().getName()) == null) {
                    return "Require school name for primary branch";
                }

                String hotline = normalize(request.getCampusData().getSchoolData().getHotline());
                if (hotline != null && !isValidPhoneNumber(hotline)) {
                    return "School hotline must contain exactly 10 digits and start with 03, 07, 08, or 09";
                }
            }

            return "";
        }

        return "Role does not support profile update";
    }

    private Gender parseGender(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(Gender.values())
                .filter(g -> g.getValue().equalsIgnoreCase(normalizedValue) || g.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    private Relationship parseRelationship(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(Relationship.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isValidPhoneNumber(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue != null && normalizedValue.matches("^(03|07|08|09)\\d{8}$");
    }

    private boolean isExactDigits(String value) {
        String normalizedValue = normalize(value);
        return normalizedValue != null && normalizedValue.matches("^\\d{" + 12 + "}$");
    }

    private boolean hasMaxWords(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return true;
        }

        return normalizedValue.split("\\s+").length > 100;
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
        campusData.put("schoolId", campus.getSchool().getId());
        campusData.put("schoolName", campus.getSchool().getName());
        campusData.put("imageJson", campus.getImageJson());
        campusData.put("facility", campus.getFacility());
        return campusData;
    }
}
