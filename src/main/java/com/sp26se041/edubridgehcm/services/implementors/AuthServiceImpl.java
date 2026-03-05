package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRegistrationRequestRepo;
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
import java.time.LocalDateTime;
import java.util.HashMap;
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


    private final ParentRepo parentRepo;

    private final SchoolRegistrationRequestRepo schoolRegistrationRequestRepo;

    @Override
    public ResponseEntity<ResponseObject> login(LoginRequest request, HttpServletResponse response) {

        if (request.getEmail() == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Email is require", null);
        }

        Account account = accountRepo.findByEmail(request.getEmail()).orElse(null);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Account not found", null);
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

        if (Role.valueOf(request.getRole()).equals(Role.PARENT)) {
            Account account = accountRepo.save(Account.builder()
                    .email(request.getEmail())
                    .role(Role.PARENT)
                    .registerDate(LocalDate.now())
                    .status(Status.ACCOUNT_ACTIVE)
                    .firstLogin(true)
                    .build());

            parentRepo.save(Parent.builder()
                    .account(account)
                    .name(request.getName())
                    .build());
            return ResponseBuilder.build(HttpStatus.OK, "Register successfully", buildAccountData(account));
        }

        if (Role.valueOf(request.getRole()).equals(Role.SCHOOL)) {
            SchoolRegistrationRequest schoolRegistrationRequest = schoolRegistrationRequestRepo.save(SchoolRegistrationRequest.builder()
                    .emailPersonal(request.getEmail().toLowerCase())
                    .schoolName(request.getSchoolName())
                    .schoolAddress(request.getSchoolAddress())
                    .campusName(request.getCampusName())
                    .campusAddress(request.getCampusAddress())
                    .taxCode(request.getTaxCode())
                    .websiteUrl(request.getWebsiteUrl())
                    .documentUrls(request.getDocumentUrls())
                    .status(Status.ACCOUNT_PENDING_VERIFY) // trạng thái chờ Admin duyệt
                    .createdAt(LocalDateTime.now())
                    .build());
            return ResponseBuilder.build(HttpStatus.OK, "Registration submitted. Your account is pending admin verified.", buildSchoolRegistrationData(schoolRegistrationRequest));
        }

        return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "This role is not allowed for self-registration", null);
    }

    private Map<String, Object> buildAccountData(Account account) {
        Map<String, Object> accountData = new HashMap<>();
        accountData.put("email", account.getEmail());
        accountData.put("role", account.getRole().getValue().toUpperCase());
        accountData.put("registerDate", account.getRegisterDate());
        accountData.put("status", account.getStatus());
        accountData.put("firstLogin", account.getFirstLogin());

        if (account.getRole().equals(Role.PARENT)) {
            accountData.put("parent", account.getParent() != null ? buildParentData(account.getParent()) : null);
        }

        if (account.getRole().equals(Role.SCHOOL)) {
            accountData.put("school", null);
        }
        return accountData;
    }

    private Map<String, Object> buildParentData(Parent parent) {
        Map<String, Object> parentData = new HashMap<>();
        parentData.put("relationship", parent.getRelationship());
        parentData.put("idCardNumber", parent.getIdCardNumber());
        parentData.put("workplace", parent.getWorkplace());
        parentData.put("occupation", parent.getOccupation());
        parentData.put("currentAddress", parent.getCurrentAddress());
        return parentData;
    }

    private Map<String, Object> buildSchoolRegistrationData(SchoolRegistrationRequest schoolRequest) {
        Map<String, Object> schoolRequestData = new HashMap<>();
        schoolRequestData.put("requestId", schoolRequest.getId());
        schoolRequestData.put("emailPersonal", schoolRequest.getEmailPersonal());
        schoolRequestData.put("schoolName", schoolRequest.getSchoolName());
        schoolRequestData.put("schoolAddress", schoolRequest.getSchoolAddress());
        schoolRequestData.put("campusName", schoolRequest.getCampusName());
        schoolRequestData.put("campusAddress", schoolRequest.getCampusAddress());
        schoolRequestData.put("taxCode", schoolRequest.getTaxCode());
        schoolRequestData.put("websiteUrl", schoolRequest.getWebsiteUrl());
        schoolRequestData.put("documentUrls", schoolRequest.getDocumentUrls());
        schoolRequestData.put("status", schoolRequest.getStatus());
        schoolRequestData.put("createdAt", schoolRequest.getCreatedAt());
        return schoolRequestData;
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
