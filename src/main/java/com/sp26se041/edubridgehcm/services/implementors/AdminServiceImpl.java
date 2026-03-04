package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.requests.SubscriptionRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.AdminService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    @Override
    public ResponseEntity<ResponseObject> getSubscriptionList() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> createSubscription(SubscriptionRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updateSubscription(SubscriptionRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> deactivateSubscription(int id) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> processRegistration(boolean aTrue, int accountId) {
        return null;
    }
}
