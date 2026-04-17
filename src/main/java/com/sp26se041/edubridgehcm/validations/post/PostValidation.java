package com.sp26se041.edubridgehcm.validations.post;

import com.sp26se041.edubridgehcm.enums.CategoryPost;
import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PostValidation {

    public static String createPostValidation(CreatePostRequest request, Map<String, Object> mediaConfig) {

        if (request.getHashTagList() == null || request.getHashTagList().isEmpty())
            return "Vui lòng nhập ít nhất một hashtag.";

        if (request.getTotalPosition() <= 0) {
            return "Tổng số vị trí (Total position) phải lớn hơn 0.";
        }

        CreatePostRequest.Content content = request.getContent();

        if (content == null) return "Nội dung bài viết không được để trống.";

        if (content.getType() == null || content.getType().isBlank()) return "Loại nội dung không được để trống.";

        if (content.getShortDescription() == null || content.getShortDescription().isBlank())
            return "Mô tả ngắn không được để trống.";

        List<CreatePostRequest.ContentData> contentDataList = request.getContent().getContentDataList();

        if (contentDataList == null) return "Danh sách dữ liệu nội dung bị thiếu.";

        for (int i = 0; i < contentDataList.size(); i++) {

            var item = contentDataList.get(i);

            if (item.getText() == null || item.getText().trim().isEmpty()) {
                return "Văn bản tại mục nội dung thứ " + (i + 1) + " không được để trống.";
            }

            if (item.getPosition() < 0) {
                return "Vị trí tại mục nội dung thứ " + (i + 1) + " phải là số không âm.";
            }
        }

        CreatePostRequest.Image image = request.getImage();

        if (image == null) return "Thông tin hình ảnh bị thiếu.";

        List<CreatePostRequest.ImageItem> imageItems = image.getImageItemList();

        if (imageItems == null || imageItems.isEmpty()) return "Danh sách hình ảnh không được để trống.";

        for (int i = 0; i < imageItems.size(); i++) {
            var img = imageItems.get(i);
            String urlError = validateMediaFormat(img.getUrl(), mediaConfig, "imgFormat");
            if (urlError != null) return "Hình ảnh thứ " + (i + 1) + ": " + urlError;
        }

        if (isBlank(request.getThumbnail())) return "Ảnh đại diện (Thumbnail) bài viết là bắt buộc.";
        String thumbnailError = validateMediaFormat(request.getThumbnail(), mediaConfig, "imgFormat");
        if (thumbnailError != null) return "Ảnh đại diện: " + thumbnailError;

        if (request.getTypeFile() == null || request.getTypeFile().isBlank()) return "Loại tệp không được để trống.";

        if (isBlank(request.getTypeFile())) return "Loại tệp không được để trống.";
        String typeFileError = validateTypeFileFormat(request.getTypeFile(), mediaConfig);
        if (typeFileError != null) return typeFileError;

        if (parseCategoryPost(request.getCategoryPost()) == null)
            return "Danh mục bài viết không hợp lệ. Vui lòng chọn một danh mục hợp lệ.";
        return null;
    }

    private static String validateTypeFileFormat(String typeFile, Map<String, Object> mediaConfig) {
        if (mediaConfig == null) return null;

        // Thu thập tất cả định dạng hợp lệ từ Admin (Ảnh, Video, Tài liệu)
        List<String> allAllowed = new ArrayList<>();
        allAllowed.addAll(ConfigSystemUtil.getAllowedFormats(mediaConfig, "imgFormat"));
        allAllowed.addAll(ConfigSystemUtil.getAllowedFormats(mediaConfig, "videoFormat"));
        allAllowed.addAll(ConfigSystemUtil.getAllowedFormats(mediaConfig, "docFormat"));

        if (allAllowed.isEmpty()) return null;

        // Chuẩn hóa typeFile để so sánh (ví dụ: "pdf" -> ".pdf")
        String ext = typeFile.trim().toLowerCase();
        if (!ext.startsWith(".")) ext = "." + ext;

        if (!allAllowed.contains(ext)) {
            return "Loại tệp '" + typeFile + "' không hỗ trợ. Chỉ chấp nhận: " + String.join(", ", allAllowed);
        }
        return null;
    }

    private static String validateMediaFormat(String url, Map<String, Object> mediaConfig, String formatKey) {
        if (isBlank(url)) return "Đường dẫn không được để trống.";
        if (!url.startsWith("http")) return "Đường dẫn không hợp lệ.";

        List<String> allowedFormats = ConfigSystemUtil.getAllowedFormats(mediaConfig, formatKey);
        if (!allowedFormats.isEmpty()) {
            boolean isValid = allowedFormats.stream().anyMatch(url.toLowerCase()::endsWith);
            if (!isValid) {
                return "định dạng không hỗ trợ. Chỉ chấp nhận: " + String.join(", ", allowedFormats);
            }
        }
        return null;
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

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
