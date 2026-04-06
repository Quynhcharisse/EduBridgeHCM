package com.sp26se041.edubridgehcm.services.implementors;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Collections;

public class GoogleDriveService {

    private final Drive drive;

    public GoogleDriveService(String accessToken) {
        HttpRequestInitializer requestInitializer =
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken);

        this.drive = new Drive.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer
        ).setApplicationName("EduBridge").build();    }

    public String createFolder(String folderName, String parentFolderId) throws IOException {

        File metadata = new File();

        metadata.setName(folderName);
        metadata.setMimeType("application/vnd.google-apps.folder");
        metadata.setParents(Collections.singletonList(parentFolderId));

        File folder = drive.files()
                .create(metadata)
                .setFields("id,name,parents")
                .execute();

        return folder.getId();
    }

    
}
