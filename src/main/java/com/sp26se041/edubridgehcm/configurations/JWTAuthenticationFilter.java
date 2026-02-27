package com.sp26se041.edubridgehcm.configurations;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;
    private static final String AUTH_PATH_PREFIX = "/api/v1/auth";
    private static final String COOKIE_NAME = "access";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(AUTH_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie accessCookie = CookieUtil.getCookie(request, COOKIE_NAME);

        if (accessCookie == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractEmailFromJWT(accessCookie.getValue());

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Account account = (Account) userDetailsService.loadUserByUsername(username);

            if (!account.getStatus().equals(Status.ACCOUNT_ACTIVE)) {
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    account, null, account.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}

