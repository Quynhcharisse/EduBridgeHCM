package com.sp26se041.edubridgehcm.utils;

import com.sp26se041.edubridgehcm.models.Account;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthRequestUtil {
    public static final String DEVICE_TYPE_HEADER = "X-Device-Type";
    public static final String DEVICE_TYPE_MOBILE = "mobile";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ACCESS_COOKIE_NAME = "access";
    public static final String REFRESH_COOKIE_NAME = "refresh";

    private AuthRequestUtil() {
    }

    public static boolean isMobileRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String deviceType = request.getHeader(DEVICE_TYPE_HEADER);
        return deviceType != null && DEVICE_TYPE_MOBILE.equalsIgnoreCase(deviceType.trim());
    }

    public static String extractAccessToken(HttpServletRequest request) {
        if (isMobileRequest(request)) {
            return extractBearerToken(request);
        }

        Cookie accessCookie = CookieUtil.getCookie(request, ACCESS_COOKIE_NAME);
        return accessCookie == null ? null : normalize(accessCookie.getValue());
    }

    public static String extractRefreshToken(HttpServletRequest request, String mobileRefreshToken) {
        if (isMobileRequest(request)) {
            return normalize(mobileRefreshToken);
        }

        Cookie refreshCookie = CookieUtil.getCookie(request, REFRESH_COOKIE_NAME);
        return refreshCookie == null ? null : normalize(refreshCookie.getValue());
    }

    public static Account extractAuthenticatedAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        return principal instanceof Account account ? account : null;
    }

    private static String extractBearerToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null) {
            return null;
        }

        String trimmed = authorization.trim();
        if (!trimmed.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }

        return normalize(trimmed.substring(BEARER_PREFIX.length()));
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

