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
import com.sp26se041.edubridgehcm.requests.ProcessRegistrationRequest;
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
    public ResponseEntity<ResponseObject> verifyRegistration(boolean isVerified, int requestId, ProcessRegistrationRequest reviewRequest) {

        // 1. lấy dữ liệu từ bảng tạm
        SchoolRegistrationRequest schoolRegistrationRequest = schoolRegistrationRequestRepo.findById(requestId).orElse(null);

        if (schoolRegistrationRequest == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No registration request with ID found: " + requestId, null);
        }

        // 2. xử lý logic cho verify
        return handleVerify(schoolRegistrationRequest, reviewRequest);
    }

    private ResponseEntity<ResponseObject> handleVerify(SchoolRegistrationRequest request, ProcessRegistrationRequest reviewRequest) {

        if (request.getStatus() != Status.ACCOUNT_PENDING_VERIFY) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "This request has been processed previously.", null);
        }

        if (schoolRepo.existsByTaxCode(request.getTaxCode())) {
            return ResponseBuilder.build(HttpStatus.CONFLICT, "This tax identification number already exists.", null);
        }

        // tạo account
        Account account = accountRepo.save(Account.builder()
                .role(Role.SCHOOL)
                .registerDate(LocalDate.now())
                .status(Status.ACCOUNT_ACTIVE)
                .firstLogin(true)
                .build());

        // tạo school (Thông tin pháp lý)
        // Có thể dùng dữ liệu từ VietQR API response để update
        School school = schoolRepo.save(School.builder()
                .name((reviewRequest.getTaxData() != null && reviewRequest.getTaxData().getName() != null) ? reviewRequest.getTaxData().getName() : request.getSchoolName())
                .taxCode((reviewRequest.getTaxData() != null && reviewRequest.getTaxData().getId() != null) ? reviewRequest.getTaxData().getId() : request.getTaxCode())
                .websiteUrl(request.getWebsiteUrl())
                .logoUrl(request.getLogoUrl())
                .representativeName(request.getRepresentativeName())
                .hotline(request.getHotline())
                .foundingDate(request.getFoundingDate())
                .businessLicenseUrl(request.getBusinessLicenseUrl())
                .build());

        // tạo campus (Địa điểm vận hành)
        campusRepo.save(Campus.builder()
                .account(account)
                .name(request.getCampusName())
                .phoneNumber(request.getCampusPhone())
                .address(request.getCampusAddress())
                .status(Status.ACCOUNT_ACTIVE)
                .isPrimaryBranch(true)
                .school(school)
                .build());

        request.setStatus(Status.VERIFIED);
        schoolRegistrationRequestRepo.save(request);

        return ResponseBuilder.build(HttpStatus.OK, "Verified successful", null);
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
