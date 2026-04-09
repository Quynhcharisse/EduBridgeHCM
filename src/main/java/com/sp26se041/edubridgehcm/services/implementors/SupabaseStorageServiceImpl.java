package com.sp26se041.edubridgehcm.services.implementors;

import com.deepoove.poi.XWPFTemplate;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class SupabaseStorageServiceImpl implements SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${SUPABASE_BUCKET_NAME}")
    private String bucketName;

    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public String uploadDocument(MultipartFile file, String folderName, String fileName,
                                 List<String> allowedExt) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name");
        }

        // ✅ lấy extension
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();


        if (!allowedExt.contains(ext)) {
            throw new IllegalArgumentException(
                    "Only allowed file types: " + String.join(", ", allowedExt)
            );
        }

        fileName = fileName.replaceAll("\\s+", "_");

        fileName = fileName + "." + ext;

        String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + folderName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);

        MediaType mediaType = switch (ext) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "doc" -> MediaType.parseMediaType("application/msword");
            case "docx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };

        headers.setContentType(mediaType);

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed: " + response.getBody());
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + folderName + "/" + fileName;
    }

    @Override
    public void generatePdfFileFromTemplateDocx(Map<String, Object> data, String templatePath, String folderName, String fileName) throws Exception {

        byte[] templateBytes = downloadTemplate(templatePath);

        byte[] generatedPdf;

        try {
            byte[] generatedDocx = replaceDocx(templateBytes, data);
            generatedPdf = convertToPdf(generatedDocx);
        } catch (Exception e)   {
            throw new Exception("Failed to generate PDF document from template", e);
        }


        String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + folderName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.setContentType(MediaType.APPLICATION_PDF);

        HttpEntity<byte[]> entity = new HttpEntity<>(generatedPdf, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

    }

    @Override
    public String moveFile(String fromPath, String toPath) throws RuntimeException {

        String url = supabaseUrl + "/storage/v1/object/move";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
          "bucketId": "%s",
          "sourceKey": "%s",
          "destinationKey": "%s"
        }
        """.formatted(bucketName, fromPath, toPath);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            System.out.println("Move success: " + fromPath + " -> " + toPath);

        } catch (Exception e) {
            throw new RuntimeException("Move file failed", e);

        }
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + toPath;
    }

    @Override
    public String extractObjectPath(String fileUrl) {
        String prefix = "/storage/v1/object/public/" + bucketName + "/";
        return fileUrl.substring(fileUrl.indexOf(prefix) + prefix.length());
    }

    public byte[] downloadTemplate(String objectPath) {

        String url = supabaseUrl + "/storage/v1/object/" + bucketName+ "/" + objectPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Get template docs failed");
        }

        return response.getBody();
    }

    private byte[] replaceDocx(byte[] templateBytes, Map<String, Object> data) throws Exception {
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(templateBytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            XWPFTemplate template = XWPFTemplate.compile(in).render(data);
            template.write(out);
            return out.toByteArray();
        }
    }

    private byte[] convertToPdf(byte[] docxBytes) throws Exception {
        try (
                ByteArrayInputStream in = new ByteArrayInputStream(docxBytes);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(in);
            Docx4J.toPDF(wordMLPackage, out);
            return out.toByteArray();
        }
    }

    public String uploadPdfBytes(byte[] pdfBytes, String bucket, String objectPath) {
        String url = supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.setContentType(MediaType.APPLICATION_PDF);

        HttpEntity<byte[]> entity = new HttpEntity<>(pdfBytes, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload PDF failed: " + response.getBody());
        }
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

}
