package com.sp26se041.edubridgehcm.utils;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

public class GoogleAuthUtil {

    public static String getAccessToken() throws IOException {

        String path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        if (path == null || path.isBlank()) {
            path = "secrets/service-account.json";
        }

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(path))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive"));

        credentials.refreshIfExpired();

        return credentials.getAccessToken().getTokenValue();

    }



}
