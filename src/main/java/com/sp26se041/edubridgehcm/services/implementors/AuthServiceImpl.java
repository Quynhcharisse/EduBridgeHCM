package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.requests.LoginRequest;
import com.sp26se041.edubridgehcm.requests.RegisterRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AuthService;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${jwt.expiration.access-token}")
    private long accessExpiration;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshExpiration;

    private final JWTService jwtService;

    private final AccountRepo accountRepo;

    @Override
    public ResponseEntity<ResponseObject> login(LoginRequest request, HttpServletResponse response) {
        Account account = accountRepo.findByEmail(request.getEmail()).orElse(null);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Account not found", null);
        }

        if (request.getEmail() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Email is require", null);
        }

        if (account.getStatus().equals(Status.ACCOUNT_RESTRICTED)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Account is restricted", null);
        }

        if (account.getStatus().equals(Status.ACCOUNT_PENDING_VERIFY)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is awaiting admin verified", null);
        }

        String access = jwtService.generateAccessToken(account);
        String refresh = jwtService.generateRefreshToken(account);

        CookieUtil.createCookies(response, access, refresh, accessExpiration, refreshExpiration);
        return ResponseBuilder.build(HttpStatus.OK, "Login successfully", buildAccountData(account));
    }

    @Override
    public ResponseEntity<ResponseObject> register(RegisterRequest request, HttpServletResponse response) {
        if (accountRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Email already exists", null);
        }

        Account account = Account.builder()
                .email(request.getEmail())
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .registerDate(LocalDate.now())
                .firstLogin(true)
                .build();

        if (account.getRole().equals(Role.PARENT)) {
            account.setStatus(Status.ACCOUNT_ACTIVE);
            accountRepo.save(account);

            return ResponseBuilder.build(HttpStatus.CREATED, "Registration successful", buildAccountData(account));
        }

        if (account.getRole().equals(Role.SCHOOL)) {
            account.setStatus(Status.ACCOUNT_PENDING_VERIFY);
            accountRepo.save(account);
            return ResponseBuilder.build(HttpStatus.ACCEPTED, "Registration submitted. Your account is pending admin verified.", null);
        }

        return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "This role is not allowed for self-registration", null);
    }

    private Map<String, Object> buildAccountData(Account account) {
        Map<String, Object> accountData = new HashMap<>();
        accountData.put("email", account.getEmail());
        accountData.put("role", account.getRole().getValue().toLowerCase());
        accountData.put("registerDate", account.getRegisterDate());
        accountData.put("status", account.getStatus());
        accountData.put("firstLogin", account.getFirstLogin());

        if (account.getRole().equals(Role.PARENT)) {
            accountData.put("parent", buildParentData(account.getParent()));
        }

        if (account.getRole().equals(Role.COUNSELLOR)) {
            accountData.put("counsellor", buildCounsellorData(account.getCounsellor()));
        }

        if (account.getRole().equals(Role.SCHOOL)) {
            accountData.put("school", null);
        }
        return accountData;
    }

    private Map<String, Object> buildParentData(Parent parent) {
        Map<String, Object> parentData = new HashMap<>();
        parentData.put("name", parent.getName());
        parentData.put("phone", parent.getPhone());
        parentData.put("relationship", parent.getRelationship());
        parentData.put("status", parent.getStatus());
        parentData.put("crmMetadata", parent.getCrmMetadata());
        parentData.put("studentProfileList", buildStudentProfileData(parent.getStudentProfileList()));
        return parentData;
    }

    private List<Map<String, Object>> buildStudentProfileData(List<StudentProfile> studentProfileList) {
        return (List<Map<String, Object>>) studentProfileList.stream()
                .map(
                        student -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("studentName", student.getStudentName());
                            data.put("dob", student.getDob());
                            data.put("gender", student.getGender());
                            data.put("profileMetadata", student.getProfileMetadata());
                            return data;
                        }
                )
                .toList();
    }

    private Map<String, Object> buildCounsellorData(Counsellor counsellor) {
        Map<String, Object> counsellorData = new HashMap<>();
        counsellorData.put("name", counsellor.getName());
        counsellorData.put("employeeCode", counsellor.getEmployeeCode());
        return counsellorData;
    }

    @Override
    public ResponseEntity<ResponseObject> refresh(HttpServletRequest request, HttpServletResponse response) {
        Account currentAcc = CookieUtil.extractAccountFromCookie(request, jwtService, accountRepo);

        if (currentAcc == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "No user found", null);
        }

        String newAccess = jwtService.generateAccessToken(currentAcc);

        String newRefresh = jwtService.generateRefreshToken(currentAcc);

        CookieUtil.createCookies(response, newAccess, newRefresh, accessExpiration, refreshExpiration);

        return ResponseBuilder.build(HttpStatus.OK, "Refresh access token successfully", null);
    }
}
