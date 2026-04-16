package com.sp26se041.edubridgehcm.services.implementors;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.sp26se041.edubridgehcm.responses.StorageTreeNode;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public Map<String, String> uploadDocument(MultipartFile file, String folderName, String fileName,
                                 List<String> allowedExt) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được trống");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        // ✅ lấy extension
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();


        if (!allowedExt.contains(ext)) {
            throw new IllegalArgumentException(
                    "Chỉ cho các định dạng file: " + String.join(", ", allowedExt)
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
            case "docx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "xlsx" -> MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
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

        Map<String, String> data = new HashMap<>();
        data.put("fileName" ,fileName);
        data.put("fileUrl", supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + folderName + "/" + fileName);
        return data;
    }

    @Override
    public String copyFileFromTemplate(String templatePath, String newFilePath) {
        byte[] fileBytes = downloadTemplate(templatePath);

        String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + newFilePath;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.set("x-upsert", "true");
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );

        HttpEntity<byte[]> entity = new HttpEntity<>(fileBytes, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Copy file failed");
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + newFilePath;
    }

    @Override
    public String generateDocFileFromTemplate(Map<String, Object> data, String templatePath, String folderName, String fileName) throws Exception {

        byte[] templateBytes = downloadTemplate(templatePath);

        byte[] generatedDocx;

        try {
           generatedDocx = replaceDocx(templateBytes, data);
        } catch (Exception e)   {
            throw new Exception("Failed to generate document from template", e);
        }


        String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + folderName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));

        HttpEntity<byte[]> entity = new HttpEntity<>(generatedDocx, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Upload failed");
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + folderName + "/" + fileName;

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

        ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Move file failed");
            }

            System.out.println("Move success: " + fromPath + " -> " + toPath);

            return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + toPath;

    }

    @Override
    public String extractObjectPath(String fileUrl) {
        String prefix = "/storage/v1/object/public/" + bucketName + "/";
        return fileUrl.substring(fileUrl.indexOf(prefix) + prefix.length());
    }

    @Override
    public void removeFile(String folderName, String fileName) {
        String url = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + folderName + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Delete file failed");
        }
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
            System.out.println("facilityItems = " + data.get("facilityItems"));

            Configure config = Configure.builder()
                    .bind("facilityItems", new LoopRowTableRenderPolicy(true))
                    .build();

            XWPFTemplate template = XWPFTemplate.compile(in, config).render(data);
            template.write(out);
            return out.toByteArray();

        } catch (Exception ex) {
            throw new Exception("Failed to render DOCX template", ex);
        }
    }

    @Override
    public StorageTreeNode getStorageTree(String folderPath){
        return buildTree(folderPath);
    }

    private StorageTreeNode buildTree(String currentPath) {

        StorageTreeNode currentNode = StorageTreeNode.builder()
                .name(extractNameFromPath(currentPath))
                .path(currentPath)
                .type("folder")
                .children(new ArrayList<>())
                .build();

        List<Map<String, Object>> items = listObjects(currentPath);

        for (Map<String, Object> item : items) {

            String name = Objects.toString(item.get("name"), "").trim();
            if (name.isEmpty()) continue;

            String childPath = buildChildPath(currentPath, name);

            if (isFolder(item)) {

                currentNode.getChildren().add(buildTree(childPath));

            } else {

                currentNode.getChildren().add(
                        StorageTreeNode.builder()
                                .name(name)
                                .path(childPath)
                                .type("file")
                                .publicUrl(buildPublicUrl(childPath))
                                .children(Collections.emptyList())
                                .build()
                );
            }
        }

        // sort: folder trước, file sau
        currentNode.getChildren().sort((a, b) -> {
            if (!a.getType().equals(b.getType())) {
                return a.getType().equals("folder") ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });

        return currentNode;
    }

    private String buildPublicUrl(String objectPath) {
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + objectPath;
    }

    private List<Map<String, Object>> listObjects(String folderPath) {

        List<Map<String, Object>> allItems = new ArrayList<>();

        int limit = 100;
        int offset = 0;

        while (true) {
            List<Map<String, Object>> page = listObjectsPage(folderPath, limit, offset);

            if (page == null || page.isEmpty()) break;

            allItems.addAll(page);

            if (page.size() < limit) break;

            offset += limit;
        }

        return allItems;
    }

    private List<Map<String, Object>> listObjectsPage(String folderPath, int limit, int offset) {

        String url = supabaseUrl + "/storage/v1/object/list/" + bucketName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apikey", serviceRoleKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
          "prefix": "%s",
          "limit": %d,
          "offset": %d,
          "sortBy": {
            "column": "name",
            "order": "asc"
          }
        }
        """.formatted(folderPath, limit, offset);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                List.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("List objects failed");
        }
        return (List<Map<String, Object>>) response.getBody();
    }

    private boolean isFolder(Map<String, Object> item) {
        return item.get("id") == null;
    }

    private String buildChildPath(String parentPath, String childName) {
        return normalizeFolderPath(parentPath) + childName;
    }

    private String normalizeFolderPath(String folderPath) {
        if (folderPath == null || folderPath.isBlank()) return "";

        String normalized = folderPath.trim().replace("\\", "/");
        normalized = normalized.replaceAll("/+", "/");

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        if (!normalized.isEmpty() && !normalized.endsWith("/")) {
            normalized += "/";
        }

        return normalized;
    }

    private String extractNameFromPath(String path) {
        String tmp = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlash = tmp.lastIndexOf('/');
        return lastSlash >= 0 ? tmp.substring(lastSlash + 1) : tmp;
    }

}
