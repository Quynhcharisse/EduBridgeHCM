package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.requests.UpdatePostRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface PostService {

    ResponseEntity<ResponseObject> createPost(CreatePostRequest request);

    ResponseEntity<ResponseObject> updatePost(UpdatePostRequest request);

    ResponseEntity<ResponseObject> viewPostList();

    ResponseEntity<ResponseObject> disablePost(DisablePostRequest request);
}
