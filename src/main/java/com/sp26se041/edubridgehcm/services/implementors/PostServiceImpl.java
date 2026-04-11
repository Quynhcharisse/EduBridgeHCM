package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.CategoryPost;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.Post;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.PostRepo;
import com.sp26se041.edubridgehcm.repositories.TemplateDocxRepo;
import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.services.PostService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.post.PostValidation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final JWTService jwtService;

    private final AccountRepo accountRepo;

    private final PostRepo postRepo;

    private final TemplateDocxRepo templateDocxRepo;

    private final SupabaseStorageService supabaseStorageService;

    @Override
    public ResponseEntity<ResponseObject> createPost(CreatePostRequest request, HttpServletRequest httpRequest) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Account acc = CookieUtil.extractAccountFromCookie(httpRequest, jwtService, accountRepo);

        if (acc == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Account not found", null);
        }

        if (acc.getRole() == Role.ADMIN) {

        } else if (acc.getRole() == Role.SCHOOL) {

            Campus actorCampus = acc.getCampus();

            if (actorCampus == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
            }

            if (!actorCampus.getIsPrimaryBranch()) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can add new campus", null);
            }

            if (!isCampusCategory(CategoryPost.valueOf(request.getCategoryPost()))) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Schools are only allowed to post information about admissions, events, or scholarships.", null);
            }
        } else {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You do not have permission to post", null);
        }

        String error = PostValidation.createPostValidation(request);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        Post post = Post.builder().hashTag(request.getHashTagList()).content(buildContentJson(request.getContent())).imageJson(buildImageJson(request.getImage())).thumbnail(request.getThumbnail()).totalPosition(request.getTotalPosition()).typeFile(request.getTypeFile()).categoryPost(CategoryPost.valueOf(request.getCategoryPost())).status(Status.POST_ACTIVE).publishedDate(LocalDateTime.now()).author(acc).build();

        postRepo.save(post);

        return ResponseBuilder.build(HttpStatus.CREATED, "Created post successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> uploadDocumentPost(MultipartFile file, String categoryPostTemplate, HttpServletRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Your account is restricted", null);
        }

        Account acc = CookieUtil.extractAccountFromCookie(request, jwtService, accountRepo);

        if (acc == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Account not found", null);
        }

        CategoryPost categoryTemplate;
        try {
            categoryTemplate = CategoryPost.valueOf(categoryPostTemplate.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Invalid post category", null);
        }

        String rootFolder;
        String subFolder;

        if (acc.getRole() == Role.ADMIN) {
            // Kiểm tra category cho ADMIN
            List<CategoryPost> adminCategories = List.of(CategoryPost.SYSTEM_NOTIFICATIONS, CategoryPost.GENERAL_EDUCATION_NEWS);
            if (!adminCategories.contains(categoryTemplate)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Admin can only post system notifications or general news", null);
            }

            rootFolder = "ADMIN_POSTS";
            subFolder = categoryTemplate.name().toUpperCase();

        } else if (acc.getRole() == Role.SCHOOL) {
            Campus actorCampus = acc.getCampus();
            if (actorCampus == null || actorCampus.getSchool() == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school/campus information found", null);
            }

            if (!actorCampus.getIsPrimaryBranch()) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can perform this action", null);
            }

            // Kiểm tra category cho SCHOOL
            if (!isCampusCategory(categoryTemplate)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Schools are only allowed to post admissions, events, or scholarships.", null);
            }

            rootFolder = "SCHOOL_POSTS";
            // Folder con là tên trường đã được xử lý chuỗi + category
            subFolder = toSafeObjectKey(actorCampus.getSchool().getName()) + "/" + categoryTemplate.name().toUpperCase();

        } else {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You do not have permission to post", null);
        }

        // 5. Thực hiện Upload
        try {
            List<String> allowedExtensions = List.of("docx", "xlsx", "pdf");

            String originalFilename = file.getOriginalFilename();
            // Làm sạch tên file gốc trước khi gắn UUID để tránh lỗi ký tự đặc biệt trên Cloud
            String safeOriginalName = toSafeObjectKey(StringUtils.stripFilenameExtension(originalFilename));
            String extension = StringUtils.getFilenameExtension(originalFilename);

            String fileName = UUID.randomUUID().toString() + "_" + safeOriginalName + "." + extension;

            // Cấu trúc đường dẫn cuối cùng
            String finalPath = rootFolder + "/" + subFolder;

            Map<String, String> uploadResult = supabaseStorageService.uploadDocument(
                    file,
                    finalPath,
                    fileName,
                    allowedExtensions
            );

            // 6. Trả về kết quả
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fileUrl", uploadResult.get("url"));
            responseData.put("fileName", fileName);
            responseData.put("category", categoryTemplate);
            responseData.put("storagePath", finalPath);

            return ResponseBuilder.build(HttpStatus.OK, "Upload successfully", responseData);

        } catch (IllegalArgumentException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        } catch (Exception ex) {
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + ex.getMessage(), null);
        }
    }

    private String toSafeObjectKey(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // 1. normalize Unicode (tách dấu ra)
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);

        // 2. remove dấu (accent)
        String noAccent = normalized.replaceAll("\\p{M}", "");

        // 3. xử lý riêng đ/Đ
        noAccent = noAccent.replace("đ", "d").replace("Đ", "d");

        // 4. lowercase
        String lower = noAccent.toLowerCase(Locale.ROOT);

        // 5. replace ký tự không hợp lệ -> _
        String safe = lower.replaceAll("[^a-z0-9]+", "_");

        // 6. cleanup: nhiều _ -> 1
        safe = safe.replaceAll("_+", "_");

        // 7. remove _ đầu/cuối
        safe = safe.replaceAll("^_+|_+$", "");

        return safe;
    }

    private Map<String, Object> buildContentJson(CreatePostRequest.Content content) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", content.getType());
        data.put("shortDescription", content.getShortDescription());
        data.put("contentDataList", content.getContentDataList().stream().map(c -> {
            Map<String, Object> contentData = new HashMap<>();
            contentData.put("text", c.getText());
            contentData.put("position", c.getPosition());
            return contentData;
        }).toList());

        return data;
    }

    private Map<String, Object> buildImageJson(CreatePostRequest.Image image) {
        Map<String, Object> data = new HashMap<>();
        data.put("imageItemList", image.getImageItemList().stream().map(item -> {
            Map<String, Object> itemDate = new HashMap<>();
            itemDate.put("url", item.getUrl());
            itemDate.put("position", item.getPosition());
            return itemDate;
        }).toList());
        return data;
    }

    private boolean isCampusCategory(CategoryPost category) {
        return category == CategoryPost.CAMPUS_EVENTS || category == CategoryPost.CAMPUS_ADMISSION || category == CategoryPost.CAMPUS_SCHOLARSHIP;
    }

    public static CategoryPost parseCategoryPost(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        return Arrays.stream(CategoryPost.values())
                .filter(r -> r.getValue().equalsIgnoreCase(normalizedValue) || r.name().equalsIgnoreCase(normalizedValue))
                .findFirst()
                .orElse(null);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public ResponseEntity<ResponseObject> disablePost(DisablePostRequest request, HttpServletRequest httpRequest) {

        Account acc = CookieUtil.extractAccountFromCookie(httpRequest, jwtService, accountRepo);

        if (acc == null) {
            return ResponseBuilder.build(HttpStatus.UNAUTHORIZED, "Account not found", null);
        }

        if (acc.getRole() == Role.ADMIN) {

        } else if (acc.getRole() == Role.SCHOOL) {

            Campus actorCampus = acc.getCampus();

            if (actorCampus == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "No school campus account found", null);
            }

            if (!actorCampus.getIsPrimaryBranch()) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Only primary campus can add new campus", null);
            }

        } else {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You do not have permission to post", null);
        }

        Post post = postRepo.findById(request.getPostId()).orElse(null);

        if (post == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Post not found", null);
        }

        if (acc.getRole() != Role.ADMIN && !post.getAuthor().getId().equals(acc.getId())) {
            // ex: admin create post --> author will be admin
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "You are not the author of this post", null);
        }

        if (post.getStatus() == Status.POST_DISABLED) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Post is already disabled", null);
        }

        post.setStatus(Status.POST_DISABLED);
        postRepo.save(post);

        return ResponseBuilder.build(HttpStatus.OK, "Post has been disabled successfully", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewPostList(HttpServletRequest httpRequest) {

        Account acc = CookieUtil.extractAccountFromCookie(httpRequest, jwtService, accountRepo);

        List<Post> postList;

        if (acc == null) {
            // guest: Chỉ xem bài viết đang Active
            postList = postRepo.findAllByStatus(Status.POST_ACTIVE);
        } else if (acc.getRole() == Role.ADMIN) {
            postList = postRepo.findAll();
        } else if (acc.getRole() == Role.SCHOOL) {
            postList = postRepo.findAllByAuthorIdOrStatus(acc.getId(), Status.POST_ACTIVE);
        } else {
            // parent, counsellor
            postList = postRepo.findAllByStatus(Status.POST_ACTIVE);
        }

        List<Map<String, Object>> data = postList.stream().map(this::buildPostData).toList();

        return ResponseBuilder.build(HttpStatus.OK, "Get post list successfully", data);
    }

    private Map<String, Object> buildPostData(Post post) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", post.getId());
        data.put("hashTag", post.getHashTag());
        data.put("content", post.getContent());
        data.put("imageJson", post.getImageJson());
        data.put("thumbnail", post.getThumbnail());
        data.put("totalPosition", post.getTotalPosition());
        data.put("typeFile", post.getTypeFile());
        data.put("categoryPost", post.getCategoryPost());
        data.put("status", post.getStatus());
        data.put("publishedDate", post.getPublishedDate());

        Account author = post.getAuthor();
        Map<String, Object> authorInfo = new HashMap<>();
        authorInfo.put("name", (author.getRole() == Role.ADMIN) ? "Hệ thống Edubridge" : (author.getRole() == Role.SCHOOL && author.getCampus() != null) ? author.getCampus().getSchool().getName() : author.getEmail());
        data.put("author", authorInfo);
        return data;
    }
}
