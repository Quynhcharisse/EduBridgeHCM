package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.CreateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.requests.ProcessRegistrationRequest;
import com.sp26se041.edubridgehcm.requests.SubscriptionRequest;
import com.sp26se041.edubridgehcm.requests.UpdatePostRequest;
import com.sp26se041.edubridgehcm.requests.UpdateServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStatusServicePackageFeeRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AdminService {

    ResponseEntity<ResponseObject> processRegistration(boolean isApproved, int requestId, ProcessRegistrationRequest reviewRequest);

    ResponseEntity<ResponseObject> createServicePackageFee(CreateServicePackageFeeRequest request);

    ResponseEntity<ResponseObject> updateServicePackageFee(UpdateServicePackageFeeRequest request);

    ResponseEntity<ResponseObject> viewServicePackageFeeList();

    ResponseEntity<ResponseObject> updateStatusServicePackageFee(UpdateStatusServicePackageFeeRequest request);

    ResponseEntity<ResponseObject> createPost(CreatePostRequest request);

    ResponseEntity<ResponseObject> updatePost(UpdatePostRequest request);

    ResponseEntity<ResponseObject> viewPostList();

    ResponseEntity<ResponseObject> disablePost(DisablePostRequest request);
}
