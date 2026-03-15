package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.requests.UpdatePostRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.implementors.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
@Tag(name = "Post")
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL')")
    public ResponseEntity<ResponseObject> createPost(@RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @PutMapping("/post")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL')")
    public ResponseEntity<ResponseObject> updatePost(@RequestBody UpdatePostRequest request) {
        return postService.updatePost(request);
    }

    @PutMapping("post/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL')")
    public ResponseEntity<ResponseObject> disablePost(@RequestBody DisablePostRequest request) {
        return postService.disablePost(request);
    }

    @GetMapping("/post/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL')")
    public ResponseEntity<ResponseObject> viewPostList() {
        return postService.viewPostList();
    }
}
