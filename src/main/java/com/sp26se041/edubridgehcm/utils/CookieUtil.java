package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.services.JWTService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;

public class CookieUtil {
    public static Cookie getCookie(@NonNull HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static void createCookies(HttpServletResponse response, String accessValue, String refreshValue, long accessExp, long refreshExp) {
        String accessCookie = String.format("access=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None; Secure", accessValue, accessExp / 1000);
        response.addHeader("Set-Cookie", accessCookie);

        String refreshCookie = String.format("refresh=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=None; Secure", refreshValue, refreshExp / 1000);
        response.addHeader("Set-Cookie", refreshCookie);
    }

    public static void removeCookie(HttpServletResponse response) {
        // Delete cookie "access"
        response.addHeader("Set-Cookie", "access=; Path=/; Max-Age=0; HttpOnly; SameSite=None; Secure");

        // Delete cookie "refresh"
        response.addHeader("Set-Cookie", "refresh=; Path=/; Max-Age=0; HttpOnly; SameSite=None; Secure");
    }

    public static Account extractAccountFromCookie(HttpServletRequest request, JWTService jwtService, AccountRepo accountRepo) {
        Cookie cookie = CookieUtil.getCookie(request, "refresh");
        if (cookie == null) {
            return null;
        }

        String refreshToken = cookie.getValue();
        String email = jwtService.extractEmailFromJWT(refreshToken);

        return accountRepo.findByEmailAndStatus(email, Status.ACCOUNT_ACTIVE).orElse(null);
    }

}
