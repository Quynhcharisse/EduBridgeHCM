package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.NotificationEventType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRegistrationRequestRepo;
import com.sp26se041.edubridgehcm.requests.LoginRequest;
import com.sp26se041.edubridgehcm.requests.RefreshTokenRequest;
import com.sp26se041.edubridgehcm.requests.RegisterRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AuthService;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.services.NotificationService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.auth.RegisterValidation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";
    private final NotificationService notificationService;

    @Value("${jwt.expiration.access-token}")
    private long accessExpiration;

    @Value("${jwt.expiration.refresh-token}")
    private long refreshExpiration;

    private final JWTService jwtService;

    private final AccountRepo accountRepo;

    private final SupabaseStorageService supabaseStorageService;

    private final ParentRepo parentRepo;

    private final SchoolRegistrationRequestRepo schoolRegistrationRequestRepo;

    private final PlatformConfigRepo platformConfigRepo;

    @Override
    public ResponseEntity<ResponseObject> uploadBusinessLicensePdf(MultipartFile file) {
        PlatformConfig media = platformConfigRepo.findByKey("media").orElse(null);
        
        if (media == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy cấu hình media", null);
        }

        try {
            ConfigSystemUtil.validateFileSize(media, file, false);
            ConfigSystemUtil.validateFileFormat(media, file, false);
        } catch (RuntimeException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        }

        String fileName = "business_license_" + UUID.randomUUID();

        Map<String, String> response;

        try {
            response = supabaseStorageService.uploadDocument(file, "BUSINESS_LICENSE", fileName, List.of("pdf", "doc", "docx"));
        } catch (IllegalArgumentException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        }

        return ResponseBuilder.build(HttpStatus.OK, "Tải tệp lên thành công", response);

    }

    @Override
    public ResponseEntity<ResponseObject> login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {

        String email = normalize(request == null ? null : request.getEmail());

        if (email == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Yêu cầu nhập email", null);
        }

        Account account = accountRepo.findByEmail(email).orElse(null);

        if (account == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Tài khoản không tồn tại trong hệ thống", null);
        }

        if (account.getStatus().equals(Status.ACCOUNT_INACTIVE)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị tạm khóa", null);
        }

        if (account.getStatus().equals(Status.ACCOUNT_PENDING_VERIFY)) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang chờ quản trị viên phê duyệt", null);
        }

        String access = jwtService.generateAccessToken(account);
        String refresh = jwtService.generateRefreshToken(account);

        if (AuthRequestUtil.isMobileRequest(httpRequest)) {
            return ResponseBuilder.build(HttpStatus.OK, "Đăng nhập thành công", buildMobileAuthData(account, access, refresh));
        }

        CookieUtil.createCookies(response, access, refresh, accessExpiration, refreshExpiration);
        return ResponseBuilder.build(HttpStatus.OK, "Đăng nhập thành công", buildAccountData(account));
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> register(RegisterRequest request, HttpServletResponse response) {

        //Lấy cấu hình Media từ hệ thống để validate định dạng file (Logo, Giấy phép, Avatar)
        Map<String, Object> mediaConfig = (Map<String, Object>) platformConfigRepo.findByKey("media")
                .map(PlatformConfig::getValue)
                .orElse(null);

        String error = RegisterValidation.registerValidation(request, mediaConfig);
        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        if (accountRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Email đã tồn tại trong hệ thống", null);
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
                    .avatar(request.getAvatar())
                    .build()));

         if (Role.valueOf(request.getRole().toUpperCase()).equals(Role.PARENT)) {
                try {
                    Map<String, Object> contextData = new HashMap<>();
                    contextData.put("parentId", account.getParent().getId());
                    contextData.put("actorName", account.getEmail());
                    contextData.put("subjectType", "PARENT");
                    contextData.put("route", "/admin/users");
                    notificationService.publish(NotificationEventType.NEW_USER_REGISTERED, null, contextData);
                } catch (Exception ex) {
                    log.error("Gửi thông báo thất bại cho đăng kí tài khoản phụ huynh id={}", account.getParent().getId(), ex);
                }
            }

            return ResponseBuilder.build(HttpStatus.OK, "Đăng ký tài khoản thành công", buildAccountData(account));
        }

        if (Role.valueOf(request.getRole().toUpperCase()).equals(Role.SCHOOL)) {
            SchoolRegistrationRequest schoolRegistrationRequest = schoolRegistrationRequestRepo.save(SchoolRegistrationRequest.builder()
                    .email(request.getEmail())
                    .schoolName(request.getSchoolRequest().getSchoolName())
                    .description(request.getSchoolRequest().getDescription())
                    .campusAddress(request.getSchoolRequest().getCampusAddress())
                    .campusPhone(request.getSchoolRequest().getCampusPhone())
                    .taxCode(request.getSchoolRequest().getTaxCode())
                    .websiteUrl(request.getSchoolRequest().getWebsiteUrl())
                    .logoUrl(request.getSchoolRequest().getLogoUrl())
                    .representativeName(request.getSchoolRequest().getRepresentativeName())
                    .hotline(request.getSchoolRequest().getHotline())
                    .foundingDate(request.getSchoolRequest().getFoundingDate())
                    .businessLicenseUrl(request.getSchoolRequest().getBusinessLicenseUrl())
                    .status(Status.ACCOUNT_PENDING_VERIFY) // trạng thái chờ Admin duyệt
                    .createdAt(LocalDateTime.now())
                    .build());

            if (Role.valueOf(request.getRole().toUpperCase()).equals(Role.SCHOOL)) {
                try {
                    Map<String, Object> contextData = new HashMap<>();
                    contextData.put("schoolRequestId", schoolRegistrationRequest.getId());
                    contextData.put("actorName", schoolRegistrationRequest.getSchoolName());
                    contextData.put("subjectType", "SCHOOL");
                    contextData.put("route", "/admin/school-requests");
                    notificationService.publish(NotificationEventType.NEW_USER_REGISTERED, null, contextData);
                } catch (Exception ex) {
                    log.error("Gửi thông báo thất bại cho đăng kí trường id={}", schoolRegistrationRequest.getId(), ex);
                }
            }

            return ResponseBuilder.build(HttpStatus.OK, "Yêu cầu đăng ký đã được gửi. Vui lòng chờ quản trị viên phê duyệt.", buildSchoolRegistrationData(schoolRegistrationRequest));
        }

        return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Vai trò này không được phép tự đăng ký", null);
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
        parentData.put("avatar", parent.getAvatar());
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
        schoolRequestData.put("description", schoolRequest.getDescription());
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
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Không tìm thấy mã làm mới (Refresh Token)", null);
        }

        String email = jwtService.extractEmailFromJWT(refreshToken);

        if (email == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Mã làm mới không hợp lệ hoặc đã hết hạn", null);
        }

        Account currentAcc = accountRepo.findByEmailAndStatus(email, Status.ACCOUNT_ACTIVE).orElse(null);

        if (currentAcc == null) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản không tồn tại", null);
        }

        String newAccess = jwtService.generateAccessToken(currentAcc);

        String newRefresh = jwtService.generateRefreshToken(currentAcc);

        if (AuthRequestUtil.isMobileRequest(httpRequest)) {
            return ResponseBuilder.build(HttpStatus.OK, "Làm mới mã truy cập thành công", buildMobileAuthData(currentAcc, newAccess, newRefresh));
        }

        CookieUtil.createCookies(response, newAccess, newRefresh, accessExpiration, refreshExpiration);

        return ResponseBuilder.build(HttpStatus.OK, "Làm mới mã truy cập thành công", null);
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
