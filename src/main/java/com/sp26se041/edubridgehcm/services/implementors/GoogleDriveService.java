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

    public File getFileSpecific(String parentFolderId, String fileName) throws IOException {

        String query = String.format(
                "'%s' in parents and name='%s' and trashed=false",
                parentFolderId,
                fileName
        );

        Drive.Files.List request = drive.files()
                .list()
                .setQ(query)
                .setFields("files(id,name,mimeType,webViewLink)")
                .setPageSize(1); // chỉ lấy 1 file

        com.google.api.services.drive.model.FileList result = request.execute();

        if (result.getFiles() == null || result.getFiles().isEmpty()) {
            return null;
        }

        return result.getFiles().get(0);
    }

}
