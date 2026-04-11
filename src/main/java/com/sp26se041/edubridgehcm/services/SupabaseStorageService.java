package com.sp26se041.edubridgehcm.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SupabaseStorageService {
    Map<String, String> uploadDocument(MultipartFile file, String folderName, String fileName,
                          List<String> allowedExt) throws Exception;
    String generateDocFileFromTemplate(Map<String, Object> data, String templatePath, String folderName, String fileName) throws Exception;
    String moveFile(String fromPath, String toPath) throws RuntimeException;
    String extractObjectPath(String fileUrl);
    void removeFile(String folderName, String fileName);
    String copyFileFromTemplate(String templatePath, String newFilePath);
}
