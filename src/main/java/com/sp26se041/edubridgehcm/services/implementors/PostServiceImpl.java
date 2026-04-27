package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.CategoryPost;
import com.sp26se041.edubridgehcm.enums.PackageType;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.PlatformConfig;
import com.sp26se041.edubridgehcm.models.Post;
import com.sp26se041.edubridgehcm.models.SchoolSubscription;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.PlatformConfigRepo;
import com.sp26se041.edubridgehcm.repositories.PostRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolSubscriptionRepo;
import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.services.PostService;
import com.sp26se041.edubridgehcm.services.SupabaseStorageService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.AuthRequestUtil;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.post.PostValidation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final JWTService jwtService;

    private final AccountRepo accountRepo;

    private final PostRepo postRepo;

    private final SupabaseStorageService supabaseStorageService;

    private final PlatformConfigRepo platformConfigRepo;

    private final SchoolSubscriptionRepo schoolSubscriptionRepo;

    @Override
    public ResponseEntity<ResponseObject> createPost(CreatePostRequest request, HttpServletRequest httpRequest) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế chức năng", null);
        }

        Account acc = AuthRequestUtil.extractAuthenticatedAccount();
        if (acc == null) {
            acc = CookieUtil.extractAccountFromCookie(httpRequest, jwtService, accountRepo);
        }

        if (acc == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy thông tin tài khoản", null);
        }

        CategoryPost category;
        try {
            category = CategoryPost.valueOf(request.getCategoryPost().toUpperCase());
        } catch (Exception e) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Danh mục bài viết không hợp lệ", null);
        }

        if (acc.getRole() == Role.ADMIN) {

            if (!isAdminCategory(category)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Quản trị viên chỉ có thể đăng thông báo hệ thống hoặc tin tức giáo dục chung", null);
            }

        } else if (acc.getRole() == Role.SCHOOL) {

            Campus actorCampus = acc.getCampus();

            if (actorCampus == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin cơ sở trường học", null);
            }

            if (!actorCampus.getIsPrimaryBranch()) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ có cơ sở chính mới có quyền đăng bài viết", null);
            }

            if (!isCampusCategory(category)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Trường học chỉ được phép đăng thông tin tuyển sinh, sự kiện hoặc học bổng", null);
            }

            String quotaError = validateSchoolPostQuota(actorCampus.getSchool().getId());
            if (quotaError != null) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, quotaError, null);
            }
        } else {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền đăng bài viết", null);
        }

        //Lấy cấu hình Media từ hệ thống để validate định dạng file (Logo, Giấy phép, Avatar)
        Map<String, Object> mediaConfig = (Map<String, Object>) platformConfigRepo.findByKey("media")
                .map(PlatformConfig::getValue)
                .orElse(null);

        String error = PostValidation.createPostValidation(request, mediaConfig);

        if (error != null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, error, null);
        }

        Post post = Post.builder().hashTag(request.getHashTagList()).content(buildContentJson(request.getContent())).imageJson(buildImageJson(request.getImage())).thumbnail(request.getThumbnail()).totalPosition(request.getTotalPosition()).typeFile(request.getTypeFile()).categoryPost(category).status(Status.POST_ACTIVE).publishedDate(LocalDateTime.now()).author(acc).build();

        postRepo.save(post);

        return ResponseBuilder.build(HttpStatus.CREATED, "Đăng bài viết thành công", null);
    }

    @Override
    public ResponseEntity<ResponseObject> uploadDocumentPost(MultipartFile file, String categoryPostTemplate, HttpServletRequest request) {

        if (AccountRestrictionUtil.isRestrictedActor()) {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Tài khoản của bạn đang bị hạn chế chức năng", null);
        }

        Account acc = AuthRequestUtil.extractAuthenticatedAccount();
        if (acc == null) {
            acc = CookieUtil.extractAccountFromCookie(request, jwtService, accountRepo);
        }

        if (acc == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy thông tin tài khoản", null);
        }

        CategoryPost categoryTemplate;
        try {
            categoryTemplate = CategoryPost.valueOf(categoryPostTemplate.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Danh mục bài viết không hợp lệ", null);
        }

        String rootFolder;
        String subFolder;

        if (acc.getRole() == Role.ADMIN) {
            // Kiểm tra category cho ADMIN
            List<CategoryPost> adminCategories = List.of(CategoryPost.SYSTEM_NOTIFICATIONS, CategoryPost.GENERAL_EDUCATION_NEWS);
            if (!adminCategories.contains(categoryTemplate)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Quản trị viên chỉ có thể tải tài liệu cho thông báo hoặc tin tức giáo dục", null);
            }

            rootFolder = "ADMIN_POSTS";
            subFolder = categoryTemplate.name().toUpperCase();

        } else if (acc.getRole() == Role.SCHOOL) {
            Campus actorCampus = acc.getCampus();
            if (actorCampus == null || actorCampus.getSchool() == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin trường học/cơ sở", null);
            }

            if (!actorCampus.getIsPrimaryBranch()) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ cơ sở chính mới có quyền thực hiện thao tác này", null);
            }

            // Kiểm tra category cho SCHOOL
            if (!isCampusCategory(categoryTemplate)) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Trường học chỉ được tải tài liệu liên quan đến tuyển sinh, sự kiện hoặc học bổng.", null);
            }

            rootFolder = "SCHOOL_POSTS";
            // Folder con là tên trường đã được xử lý chuỗi + category
            subFolder = toSafeObjectKey(actorCampus.getSchool().getName()) + "/" + categoryTemplate.name().toUpperCase();

        } else {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này", null);
        }

        PlatformConfig media = platformConfigRepo.findByKey("media").orElse(null);

        if (media == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy cấu hình media", null);
        }

        try {
            if (file == null || file.isEmpty()) {
                return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "File không được để trống", null);
            }
            ConfigSystemUtil.validateFileSize(media, file, false);
            ConfigSystemUtil.validateFileFormat(media, file, false);
        } catch (RuntimeException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        }

        // 5. Thực hiện Upload
        try {
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
                    null
            );

            // 6. Trả về kết quả
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("fileUrl", uploadResult.get("url"));
            responseData.put("fileName", fileName);
            responseData.put("category", categoryTemplate);
            responseData.put("storagePath", finalPath);

            return ResponseBuilder.build(HttpStatus.OK, "Tải tệp lên thành công", responseData);

        } catch (IllegalArgumentException ex) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        } catch (Exception ex) {
            log.error("Upload document failed", ex);
            return ResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Tải tệp thất bại", null);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> disablePost(DisablePostRequest request, HttpServletRequest httpRequest) {

        Account acc = CookieUtil.extractAccountFromCookie(httpRequest, jwtService, accountRepo);

        if (acc == null) {
            acc = CookieUtil.extractAccountFromCookie(httpRequest, jwtService, accountRepo);
        }

        if (acc == null) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Không tìm thấy thông tin tài khoản", null);
        }

        if (acc.getRole() == Role.ADMIN) {

        } else if (acc.getRole() == Role.SCHOOL) {

            Campus actorCampus = acc.getCampus();

            if (actorCampus == null) {
                return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin cơ sở", null);
            }

            if (!actorCampus.getIsPrimaryBranch()) {
                return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Chỉ cơ sở chính mới có quyền ẩn bài viết", null);
            }

        } else {
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này", null);
        }

        Post post = postRepo.findById(request.getPostId()).orElse(null);

        if (post == null) {
            return ResponseBuilder.build(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết", null);
        }

        if (acc.getRole() != Role.ADMIN && !post.getAuthor().getId().equals(acc.getId())) {
            // ex: admin create post --> author will be admin
            return ResponseBuilder.build(HttpStatus.FORBIDDEN, "Bạn không phải là tác giả của bài viết này", null);
        }

        if (post.getStatus() == Status.POST_DISABLED) {
            return ResponseBuilder.build(HttpStatus.BAD_REQUEST, "Bài viết này đã được ẩn trước đó", null);
        }

        post.setStatus(Status.POST_DISABLED);
        postRepo.save(post);

        return ResponseBuilder.build(HttpStatus.OK, "Đã ẩn bài viết thành công", null);
    }

    @Override
    public ResponseEntity<ResponseObject> viewPostList(HttpServletRequest httpRequest) {

        List<Post> postList = postRepo.findAll();
        List<Map<String, Object>> data = postList.stream()
                .filter(s -> s.getStatus().equals(Status.POST_ACTIVE))
                .map(this::buildPostData)
                .toList();

        return ResponseBuilder.build(HttpStatus.OK, "", data);
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

    private boolean isAdminCategory(CategoryPost category) {
        return List.of(CategoryPost.SYSTEM_NOTIFICATIONS, CategoryPost.GENERAL_EDUCATION_NEWS)
                .contains(category);
    }

    //check
    private String validateSchoolPostQuota(Integer schoolId) {

        Optional<SchoolSubscription> activeSubOpt = schoolSubscriptionRepo
                .findBySchoolIdAndEndDateGreaterThanEqualAndIsSelectedTrue(schoolId, java.time.LocalDate.now());

        if (activeSubOpt.isEmpty() || activeSubOpt.get().getSubscription() == null) {
            return "Trường chưa có gói dịch vụ đang hoạt động để đăng bài";
        }

        SchoolSubscription activeSub = activeSubOpt.get();

        Integer postLimit = resolvePostLimitFromConfig(activeSub.getSubscription().getPackageType());

        if (postLimit == null || postLimit < 0) {
            return null;
        }

        LocalDateTime startDate = activeSub.getStartDate().atStartOfDay();
        LocalDateTime endDate = activeSub.getEndDate().atTime(23, 59, 59);

        long usedPosts = postRepo.countByAuthorCampusSchoolIdAndCategoryPostInAndPublishedDateBetween(
                schoolId,
                List.of(CategoryPost.CAMPUS_EVENTS, CategoryPost.CAMPUS_ADMISSION, CategoryPost.CAMPUS_SCHOLARSHIP),
                startDate,
                endDate
        );

        if (usedPosts >= postLimit) {
            return "Bạn đã đạt giới hạn đăng bài của gói hiện tại (" + postLimit + " bài/kỳ gói)";
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Integer resolvePostLimitFromConfig(PackageType packageType) {
        PlatformConfig businessConfig = platformConfigRepo.findByKey("business").orElse(null);
        if (businessConfig == null || !(businessConfig.getValue() instanceof Map<?, ?> businessMapRaw)) {
            return -1;
        }

        Map<String, Object> business = (Map<String, Object>) businessMapRaw;
        Object subscriptionPricingObj = business.get("subscriptionPricing");
        if (!(subscriptionPricingObj instanceof Map<?, ?> subscriptionPricingRaw)) {
            return -1;
        }

        Map<String, Object> subscriptionPricing = (Map<String, Object>) subscriptionPricingRaw;
        Object packageQuotasObj = subscriptionPricing.get("packageQuotas");
        if (!(packageQuotasObj instanceof Map<?, ?> packageQuotasRaw)) {
            return -1;
        }

        Map<String, Object> packageQuotas = (Map<String, Object>) packageQuotasRaw;
        String key = switch (packageType) {
            case TRIAL -> "trialPostLimit";
            case STANDARD -> "standardPostLimit";
            case ENTERPRISE -> "enterprisePostLimit";
        };

        Object limitObj = packageQuotas.get(key);
        if (limitObj == null) {
            return -1;
        }

        if (!(limitObj instanceof Number numberValue)) {
            throw new RuntimeException("Cấu hình giới hạn đăng bài không hợp lệ cho gói " + packageType);
        }

        return numberValue.intValue();
    }
}
