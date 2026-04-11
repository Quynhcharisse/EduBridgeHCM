package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface PostService {

    ResponseEntity<ResponseObject> createPost(CreatePostRequest request, HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> viewPostList(HttpServletRequest httpRequest);

    ResponseEntity<ResponseObject> disablePost(DisablePostRequest request, HttpServletRequest httpRequest);
}
