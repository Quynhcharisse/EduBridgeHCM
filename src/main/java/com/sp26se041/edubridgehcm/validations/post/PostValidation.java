package com.sp26se041.edubridgehcm.validations.post;

import com.sp26se041.edubridgehcm.enums.CategoryPost;
import com.sp26se041.edubridgehcm.requests.CreatePostRequest;

import java.util.Arrays;
import java.util.List;

public class PostValidation {

    public static String createPostValidation(CreatePostRequest request) {

        if (request.getHashTagList() == null || request.getHashTagList().isEmpty())
            return "At least one hashtag is required.";

        if (request.getTotalPosition() <= 0) {
            return "Total position must be greater than 0.";
        }

        CreatePostRequest.Content content = request.getContent();

        if (content == null) return "Content body is missing.";

        if (content.getType() == null || content.getType().isBlank()) return "Content type is required.";

        if (content.getShortDescription() == null || content.getShortDescription().isBlank())
            return "Content short description is required.";

        List<CreatePostRequest.ContentData> contentDataList = request.getContent().getContentDataList();

        if (contentDataList == null) return "Content data list is missing.";

        for (int i = 0; i < contentDataList.size(); i++) {

            var item = contentDataList.get(i);

            if (item.getText() == null || item.getText().trim().isEmpty()) {
                return "Text at content item " + (i + 1) + " cannot be empty.";
            }

            if (item.getPosition() < 0) {
                return "Position at content item " + (i + 1) + " must be non-negative.";
            }
        }

        CreatePostRequest.Image image = request.getImage();

        if (image == null) return "Image object is missing.";

        List<CreatePostRequest.ImageItem> imageItems = image.getImageItemList();

        if (imageItems == null || imageItems.isEmpty()) return "Image list cannot be empty.";

        for (int i = 0; i < imageItems.size(); i++) {
            var img = imageItems.get(i);
            if (img.getUrl() == null || img.getUrl().isBlank()) {
                return "URL at image item " + (i + 1) + " cannot be empty.";
            }
            if (!img.getUrl().startsWith("http")) {
                return "URL at image item " + (i + 1) + " is invalid.";
            }
        }

        if (request.getThumbnail() == null || request.getThumbnail().isBlank()) return "Thumbnail URL is required.";

        if (request.getTypeFile() == null || request.getTypeFile().isBlank()) return "Type file is required.";

        if (parseCategoryPost(request.getCategoryPost()) == null)
            return "Invalid category post. Please provide a valid category name or value.";

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
}
