package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.Relationship;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.requests.RestrictionRequest;
import com.sp26se041.edubridgehcm.requests.UpdateProfileRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AccountService;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final JWTService jWTService;

    private final AccountRepo accountRepo;

    @Override
    public ResponseEntity<ResponseObject> logout(HttpServletRequest request, HttpServletResponse response) {

        Cookie refresh = CookieUtil.getCookie(request, "refresh");

        if (refresh == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Logout failed", null);
        }

        if (!jWTService.checkIfNotExpired(refresh.getValue())) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Token invalid", null);
        }

        CookieUtil.removeCookie(response);

        return ResponseBuilder.build(HttpStatus.OK, "Logout successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> getAccessToken(HttpServletRequest request) {

        Cookie access = CookieUtil.getCookie(request, "access");

        if (access == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "No access", null);
        }

        Account account = CookieUtil.extractAccountFromCookie(request, jWTService, accountRepo);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "No account", null);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("access", access.getValue());
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
            account.setRestrictionReason(request.getReason().trim());
            account.setRestrictionDate(LocalDateTime.now());
        } else {
            account.setRestricted(false);
            account.setRestrictionReason(null);
            account.setRestrictionDate(null);
        }

        // keep account status active so restricted users can still login and use read API
        account.setStatus(Status.ACCOUNT_ACTIVE);

        accountRepo.save(account);

        return ResponseBuilder.build(HttpStatus.OK, request.isRestricted() ? "Account restricted successfully" : "Account unrestricted successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request, HttpServletRequest httpRequest) {

        Account account = CookieUtil.extractAccountFromCookie(httpRequest, jWTService, accountRepo);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No account", null);
        }

        String error = updateProfileValidation(request, account);

        if (!error.isEmpty()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        if (account.getRole() == Role.PARENT) {
            updateParentProfile(account.getParent(), request.getParentData());
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


    private void updateParentProfile(Parent parent, UpdateProfileRequest.ParentData parentData) {
        parent.setName(parentData.getName());
        parent.setGender(Gender.valueOf(parentData.getGender()));
        parent.setRelationship(Relationship.valueOf(parentData.getRelationship()));
        parent.setPhone(parentData.getPhone());
        parent.setWorkplace(parentData.getWorkplace());
        parent.setOccupation(parentData.getOccupation());
        parent.setCurrentAddress(parentData.getCurrentAddress());
    }

    private void updateCounsellorProfile(Counsellor counsellor, UpdateProfileRequest.CounsellorData counsellorData) {
        counsellor.setName(counsellorData.getName());
    }

    private void updateCampusProfile(Campus campus, UpdateProfileRequest.CampusData campusData) {
        campus.setName(campusData.getName());
        campus.setPhoneNumber(campusData.getPhoneNumber());
        campus.setAddress(campusData.getAddress());
        campus.setImageJson(campusData.getImageJson());
        campus.setFacility(campusData.getFacilityJson());
        campus.setPolicyDetail(campusData.getPolicyDetail());
    }

    private String updateProfileValidation(UpdateProfileRequest request, Account account) {

        if (account == null) {
            return "Account does not exist";
        }

        if (account.isRestricted()) {
            return "Your account is restricted";
        }

        if (account.getRole() == Role.PARENT) {

            if (request.getParentData().getName().trim().isEmpty()) {
                return "Require parent name";
            }

            if (request.getParentData().getGender() == null || !isGenderValid(request.getParentData().getGender())) {
                return "Invalid parent gender";
            }

            if (request.getParentData().getRelationship() == null || !isRelationshipValid(request.getParentData().getRelationship())) {
                return "Invalid parent relationship";
            }

            if (request.getParentData().getPhone() == null || request.getParentData().getPhone().trim().isEmpty()) {
                return "Require parent phone";
            }

            if (request.getParentData().getOccupation().trim().isEmpty()) {
                return "Require parent occupation";
            }

            if (request.getParentData().getWorkplace().trim().isEmpty()) {
                return "Require parent workplace";
            }

            if (request.getParentData().getCurrentAddress().trim().isEmpty()) {
                return "Require parent address";
            }

            return "";
        }

        if (account.getRole() == Role.COUNSELLOR) {

            if (request.getCounsellorData().getName().trim().isEmpty()) {
                return "Require counsellor name";
            }
            return "";
        }

        if (account.getRole() == Role.SCHOOL) {

            if (request.getCampusData().getName() == null || request.getCampusData().getName().trim().isEmpty()) {
                return "Require campus name";
            }

            if (request.getCampusData().getPhoneNumber() == null || request.getCampusData().getPhoneNumber().trim().isEmpty()) {
                return "Require campus phone number";
            }

            if (request.getCampusData().getAddress() == null || request.getCampusData().getAddress().trim().isEmpty()) {
                return "Require campus address";
            }

            return "";
        }

        return "";
    }

    private boolean isGenderValid(String value) {
        return Arrays.stream(Gender.values())
                .anyMatch(g -> g.getValue().equalsIgnoreCase(value));
    }

    private boolean isRelationshipValid(String value) {
        return Arrays.stream(Relationship.values())
                .anyMatch(r -> r.getValue().equalsIgnoreCase(value));
    }

    @Override
    public ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request) {

        Account account = CookieUtil.extractAccountFromCookie(request, jWTService, accountRepo);

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
        parentData.put("relationship", parent.getRelationship());
        parentData.put("workplace", parent.getWorkplace());
        parentData.put("occupation", parent.getOccupation());
        parentData.put("currentAddress", parent.getCurrentAddress());
        parentData.put("idCardNumber", parent.getIdCardNumber());
        return parentData;
    }

    private Map<String, Object> buildCounsellorProfileData(Counsellor counsellor) {

        if (counsellor == null) return null;

        Map<String, Object> counsellorData = new HashMap<>();
        counsellorData.put("name", counsellor.getName());
        counsellorData.put("employeeCode", counsellor.getEmployeeCode());
        counsellorData.put("campusId", Optional.of(counsellor.getCampus().getId()).orElse(null));
        counsellorData.put("campusName", Optional.of(counsellor.getCampus().getName()).orElse(null));
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
        campusData.put("schoolId", Optional.of(campus.getSchool().getId()).orElse(null));
        campusData.put("schoolName", Optional.of(campus.getSchool().getName()).orElse(null));
        campusData.put("imageJson", campus.getImageJson());
        campusData.put("facility", campus.getFacility());
        return campusData;
    }
}
