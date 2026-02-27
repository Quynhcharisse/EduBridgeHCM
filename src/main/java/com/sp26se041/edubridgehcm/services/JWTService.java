package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.models.Account;

public interface JWTService {
    String extractEmailFromJWT(String jwt);

    String generateAccessToken(Account account);

    String generateRefreshToken(Account account);

    boolean checkIfNotExpired(String jwt);
}
