package com.sp26se041.edubridgehcm.configurations;

import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
public class RestrictedAccountAspect {

    private final JWTService jwtService;
    private final AccountRepo accountRepo;

    // this aspect will intercept all POST, PUT/PATCH, DELETE requests and check if the account is restricted
    @Before("(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)) && " +
            "!@annotation(com.sp26se041.edubridgehcm.configurations.SkipRestrictedCheck)")
    public void checkRestrictedAccount() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        Account account = CookieUtil.extractAccountFromCookie(request, jwtService, accountRepo);

        if (account != null && account.isRestricted()) {
            throw new ResponseStatusException( //việc throw giúp dừng mọi logic xử lý phía sau
                    HttpStatus.FORBIDDEN,
                    "Your account has been restricted. Reason: " + account.getRestrictionReason() +
                            ". You can only view data, cannot create, update or delete."
            );
        }
    }
}

