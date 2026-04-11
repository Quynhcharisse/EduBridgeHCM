package com.sp26se041.edubridgehcm.controllers;

import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.PostService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL')")
    public ResponseEntity<ResponseObject> createPost(@RequestBody CreatePostRequest request, HttpServletRequest httpRequest) {
        return postService.createPost(request, httpRequest);
    }

    @PutMapping("/status/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL')")
    public ResponseEntity<ResponseObject> disablePost(@RequestBody DisablePostRequest request, HttpServletRequest httpRequest) {
        return postService.disablePost(request, httpRequest);
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseObject> viewPostList(HttpServletRequest httpRequest) {
        return postService.viewPostList(httpRequest);
    }
}
