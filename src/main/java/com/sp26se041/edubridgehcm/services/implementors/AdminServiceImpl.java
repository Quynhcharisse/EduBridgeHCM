package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.models.SchoolRegistrationRequest;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRegistrationRequestRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.CreateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.requests.UpdatePostRequest;
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

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final SchoolRegistrationRequestRepo schoolRegistrationRequestRepo;

    private final AccountRepo accountRepo;

    private final SchoolRepo schoolRepo;

    private final CampusRepo campusRepo;

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
                .registerDate(LocalDate.now())
                .status(Status.ACCOUNT_ACTIVE)
                .firstLogin(true)
                .build());

        // tạo school (lấy thẳng từ bảng tạm)
        School school = schoolRepo.save(School.builder()
                .name(request.getSchoolName().trim())
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
    public ResponseEntity<ResponseObject> createPost(CreatePostRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updatePost(UpdatePostRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewPostList() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> disablePost(DisablePostRequest request) {
        return null;
    }
}
