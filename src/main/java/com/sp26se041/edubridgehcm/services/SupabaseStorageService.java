package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface SupabaseStorageService {
    String uploadPdfFile(MultipartFile file, String folderName,String fileName) throws IOException;
    void generatePdfFileFromTemplateDocx(Map<String, Object> data, String templatePath, String folderName, String fileName) throws Exception;
    void moveFile(String fromPath, String toPath);
    String extractObjectPath(String fileUrl);
}
