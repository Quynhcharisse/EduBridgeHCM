package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface SupabaseStorageService {
    String uploadPdfFile(MultipartFile file, String folderName,String objectPath) throws IOException;
}
