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
import com.sp26se041.edubridgehcm.requests.RefreshTokenRequest;
import com.sp26se041.edubridgehcm.requests.RegisterRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AuthService;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String TOKEN_TYPE = "Bearer";

    @Value("${jwt.expiration.access-token}")
    private long accessExpiration;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshExpiration;

    private final JWTService jwtService;

    private final AccountRepo accountRepo;


    private final ParentRepo parentRepo;

    private final SchoolRegistrationRequestRepo schoolRegistrationRequestRepo;

    @Override
    public ResponseEntity<ResponseObject> login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {

        String email = normalize(request == null ? null : request.getEmail());

        if (email == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Email is require", null);
        }

        Account account = accountRepo.findByEmail(email).orElse(null);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Account not found", null);
        }

        if (account.getStatus().equals(Status.ACCOUNT_INACTIVE)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is inactive", null);
        }

        if (account.getStatus().equals(Status.ACCOUNT_PENDING_VERIFY)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is awaiting admin verified", null);
        }

        String access = jwtService.generateAccessToken(account);
        String refresh = jwtService.generateRefreshToken(account);

        if (AuthRequestUtil.isMobileRequest(httpRequest)) {
            return ResponseBuilder.build(HttpStatus.OK, "Login successfully", buildMobileAuthData(account, access, refresh));
        }

        CookieUtil.createCookies(response, access, refresh, accessExpiration, refreshExpiration);
        return ResponseBuilder.build(HttpStatus.OK, "Login successfully", buildAccountData(account));
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> register(RegisterRequest request, HttpServletResponse response) {
        if (accountRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Email already exists", null);
        }

        if (Role.valueOf(request.getRole().toUpperCase()).equals(Role.PARENT)) {
            Account account = accountRepo.save(Account.builder()
                    .email(request.getEmail())
                    .role(Role.PARENT)
                    .registerDate(LocalDate.now())
                    .status(Status.ACCOUNT_ACTIVE)
                    .firstLogin(true)
                    .build());

            account.setParent(parentRepo.save(Parent.builder()
                    .account(account)
                    .build()));
            return ResponseBuilder.build(HttpStatus.OK, "Register successfully", buildAccountData(account));
        }

        if (Role.valueOf(request.getRole().toUpperCase()).equals(Role.SCHOOL)) {
            SchoolRegistrationRequest schoolRegistrationRequest = schoolRegistrationRequestRepo.save(SchoolRegistrationRequest.builder()
                    .email(request.getEmail())
                    .schoolName(request.getSchoolRequest().getSchoolName())
                    .campusName(request.getSchoolRequest().getCampusName())
                    .campusAddress(request.getSchoolRequest().getCampusAddress())
                    .campusPhone(request.getSchoolRequest().getCampusPhone())
                    .taxCode(request.getSchoolRequest().getTaxCode())
                    .websiteUrl(request.getSchoolRequest().getWebsiteUrl())
                    .logoUrl(request.getSchoolRequest().getLogoUrl())
                    .representativeName(request.getSchoolRequest().getRepresentativeName())
                    .hotline(request.getSchoolRequest().getHotline())
                    .foundingDate(LocalDate.of(request.getSchoolRequest().getFoundingDate().getYear(), request.getSchoolRequest().getFoundingDate().getMonth(), request.getSchoolRequest().getFoundingDate().getDayOfMonth()))
                    .businessLicenseUrl(request.getSchoolRequest().getBusinessLicenseUrl())
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
            accountData.put("parent", buildParentData(account.getParent()));
        }

        if (account.getRole().equals(Role.SCHOOL)) {
            accountData.put("school", null);
        }
        return accountData;
    }

    private Map<String, Object> buildParentData(Parent parent) {
        Map<String, Object> parentData = new HashMap<>();
        parentData.put("gender", parent.getGender());
        parentData.put("name", parent.getName());
        parentData.put("relationship", parent.getRelationship());
        parentData.put("idCardNumber", parent.getIdCardNumber());
        parentData.put("workplace", parent.getWorkplace());
        parentData.put("currentAddress", parent.getCurrentAddress());
        return parentData;
    }

    private Map<String, Object> buildSchoolRegistrationData(SchoolRegistrationRequest schoolRequest) {
        Map<String, Object> schoolRequestData = new HashMap<>();
        schoolRequestData.put("requestId", schoolRequest.getId());
        schoolRequestData.put("schoolName", schoolRequest.getSchoolName());
        schoolRequestData.put("campusName", schoolRequest.getCampusName());
        schoolRequestData.put("campusAddress", schoolRequest.getCampusAddress());
        schoolRequestData.put("campusPhone", schoolRequest.getCampusPhone());
        schoolRequestData.put("taxCode", schoolRequest.getTaxCode());
        schoolRequestData.put("websiteUrl", schoolRequest.getWebsiteUrl());
        schoolRequestData.put("logoUrl", schoolRequest.getLogoUrl());
        schoolRequestData.put("foundingDate", schoolRequest.getFoundingDate());
        schoolRequestData.put("representativeName", schoolRequest.getRepresentativeName());
        schoolRequestData.put("hotline", schoolRequest.getHotline());
        schoolRequestData.put("businessLicenseUrl", schoolRequest.getBusinessLicenseUrl());
        schoolRequestData.put("status", schoolRequest.getStatus());
        schoolRequestData.put("createdAt", schoolRequest.getCreatedAt());
        return schoolRequestData;
    }

    @Override
    public ResponseEntity<ResponseObject> refresh(RefreshTokenRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        String refreshToken = AuthRequestUtil.extractRefreshToken(httpRequest, request == null ? null : request.getRefreshToken());

        if (refreshToken == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "No refresh token found", null);
        }

        String email = jwtService.extractEmailFromJWT(refreshToken);

        if (email == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Refresh token invalid", null);
        }

        Account currentAcc = accountRepo.findByEmailAndStatus(email, Status.ACCOUNT_ACTIVE).orElse(null);

        if (currentAcc == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "No user found", null);
        }

        String newAccess = jwtService.generateAccessToken(currentAcc);

        String newRefresh = jwtService.generateRefreshToken(currentAcc);

        if (AuthRequestUtil.isMobileRequest(httpRequest)) {
            return ResponseBuilder.build(HttpStatus.OK, "Refresh access token successfully", buildMobileAuthData(currentAcc, newAccess, newRefresh));
        }

        CookieUtil.createCookies(response, newAccess, newRefresh, accessExpiration, refreshExpiration);

        return ResponseBuilder.build(HttpStatus.OK, "Refresh access token successfully", null);
    }

    private Map<String, Object> buildMobileAuthData(Account account, String accessToken, String refreshToken) {
        Map<String, Object> data = new HashMap<>();
        data.put("account", buildAccountData(account));
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("tokenType", TOKEN_TYPE);
        data.put("accessExpiresIn", accessExpiration / 1000);
        data.put("refreshExpiresIn", refreshExpiration / 1000);
        return data;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
