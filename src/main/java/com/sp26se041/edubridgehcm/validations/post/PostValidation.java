package com.sp26se041.edubridgehcm.validations.post;

import com.sp26se041.edubridgehcm.enums.CategoryPost;
import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.utils.ConfigSystemUtil;

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
            String urlErr = ConfigSystemUtil.validateUrlFormat(mediaConfig, img.getUrl(), true);
            if (urlErr != null) return "Hình ảnh thứ " + (i + 1) + ": " + urlErr;
        }

        if (isBlank(request.getThumbnail())) return "Ảnh đại diện (Thumbnail) bài viết là bắt buộc.";

        String thumbnailErr = ConfigSystemUtil.validateUrlFormat(mediaConfig, request.getThumbnail(), true);

        if (thumbnailErr != null) return "Ảnh đại diện: " + thumbnailErr;

        if (isBlank(request.getTypeFile())) return "Loại tệp không được để trống.";

        String typeFileErr = ConfigSystemUtil.validateUrlFormat(mediaConfig, request.getTypeFile(), false);

        if (typeFileErr != null) return typeFileErr;

        if (parseCategoryPost(request.getCategoryPost()) == null)
            return "Danh mục bài viết không hợp lệ. Vui lòng chọn một danh mục hợp lệ.";
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
