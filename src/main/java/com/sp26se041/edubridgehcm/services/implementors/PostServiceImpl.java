package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.enums.CategoryPost;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.Post;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.PostRepo;
import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.JWTService;
import com.sp26se041.edubridgehcm.services.PostService;
import com.sp26se041.edubridgehcm.utils.AccountRestrictionUtil;
import com.sp26se041.edubridgehcm.utils.CookieUtil;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import com.sp26se041.edubridgehcm.validations.post.PostValidation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final JWTService jwtService;

    private final AccountRepo accountRepo;

    private final PostRepo postRepo;

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
