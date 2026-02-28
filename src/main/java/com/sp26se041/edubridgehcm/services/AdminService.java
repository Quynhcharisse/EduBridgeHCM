package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.SubscriptionRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AdminService {

    // manage packages

    ResponseEntity<ResponseObject> getSubscriptionList();

    ResponseEntity<ResponseObject> createSubscription(SubscriptionRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> updateSubscription(SubscriptionRequest request, HttpServletResponse response);

    ResponseEntity<ResponseObject> deactivateSubscription(int id);


}
