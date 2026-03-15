package com.sp26se041.edubridgehcm.services.implementors;

import com.sp26se041.edubridgehcm.requests.CreatePostRequest;
import com.sp26se041.edubridgehcm.requests.DisablePostRequest;
import com.sp26se041.edubridgehcm.requests.UpdatePostRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    @Override
    public ResponseEntity<ResponseObject> createPost(CreatePostRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> updatePost(UpdatePostRequest request) {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewPostList() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> disablePost(DisablePostRequest request) {
        return null;
    }
}
