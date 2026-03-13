package com.sp26se041.edubridgehcm.services.implementors;

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
import java.util.HashMap;
import java.util.Map;

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
            if (request.getReason().trim().isEmpty()) {
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

        if (request == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Request body is required", null);
        }

        String name = normalize(request.getName());
        String phone = normalize(request.getPhone());
        String address = normalize(request.getAddress());

        Account account = CookieUtil.extractAccountFromCookie(httpRequest, jWTService, accountRepo);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No account", null);
        }

        if (account.isRestricted()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        if (name == null && phone == null && address == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "At least one field is required", null);
        }

        if (phone != null) {
            account.setPhone(phone);
        }

        if (address != null) {
            account.setAddress(address);
        }

        if (name != null) {
            if (account.getRole() == Role.PARENT) {
                Parent parent = account.getParent();
                if (parent == null) {
                    return ResponseBuilder.build(HttpStatus.CONFLICT, "Parent profile is not initialized", null);
                }
                parent.setName(name);
            } else if (account.getRole() == Role.COUNSELLOR) {
                Counsellor counsellor = account.getCounsellor();
                if (counsellor == null) {
                    return ResponseBuilder.build(HttpStatus.CONFLICT, "Counsellor profile is not initialized", null);
                }
                counsellor.setName(name);
            } else if (account.getRole() == Role.SCHOOL) {
                Campus campus = account.getCampus();
                if (campus != null) {
                    campus.setName(name);
                }
            }
        }

        Account saved = accountRepo.save(account);
        return ResponseBuilder.build(HttpStatus.OK, "Update profile successfully", buildProfileData(saved));
    }

    @Override
    public ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request) {
        // update dành cho parent , school, counsellor
        Account account = CookieUtil.extractAccountFromCookie(request, jWTService, accountRepo);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No account", null);
        }

        return ResponseBuilder.build(HttpStatus.OK, "View profile successfully", buildProfileData(account));
    }

    private Map<String, Object> buildProfileData(Account account) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", account.getId());
        data.put("email", account.getEmail());
        data.put("role", account.getRole());
        data.put("status", account.getStatus());
        data.put("phone", account.getPhone());
        data.put("address", account.getAddress());
        data.put("firstLogin", account.getFirstLogin());
        data.put("isRestricted", account.isRestricted());
        data.put("restrictionReason", account.getRestrictionReason());
        data.put("restrictionDate", account.getRestrictionDate());

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
        if (parent == null) {
            return null;
        }

        Map<String, Object> parentData = new HashMap<>();
        parentData.put("id", parent.getId());
        parentData.put("name", parent.getName());
        parentData.put("gender", parent.getGender());
        parentData.put("relationship", parent.getRelationship());
        parentData.put("idCardNumber", parent.getIdCardNumber());
        parentData.put("workplace", parent.getWorkplace());
        parentData.put("occupation", parent.getOccupation());
        parentData.put("currentAddress", parent.getCurrentAddress());
        return parentData;
    }

    private Map<String, Object> buildCounsellorProfileData(Counsellor counsellor) {
        if (counsellor == null) {
            return null;
        }

        Map<String, Object> counsellorData = new HashMap<>();
        counsellorData.put("id", counsellor.getId());
        counsellorData.put("name", counsellor.getName());
        counsellorData.put("employeeCode", counsellor.getEmployeeCode());
        counsellorData.put("campusId", counsellor.getCampus() == null ? null : counsellor.getCampus().getId());
        counsellorData.put("campusName", counsellor.getCampus() == null ? null : counsellor.getCampus().getName());
        return counsellorData;
    }

    private Map<String, Object> buildCampusProfileData(Campus campus) {
        if (campus == null) {
            return null;
        }

        Map<String, Object> campusData = new HashMap<>();
        campusData.put("id", campus.getId());
        campusData.put("name", campus.getName());
        campusData.put("phoneNumber", campus.getPhoneNumber());
        campusData.put("address", campus.getAddress());
        campusData.put("status", campus.getStatus());
        campusData.put("isPrimaryBranch", campus.getIsPrimaryBranch());
        campusData.put("schoolId", campus.getSchool() == null ? null : campus.getSchool().getId());
        campusData.put("schoolName", campus.getSchool() == null ? null : campus.getSchool().getName());
        return campusData;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
