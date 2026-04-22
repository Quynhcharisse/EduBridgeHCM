package com.sp26se041.edubridgehcm.configurations;

import com.sp26se041.edubridgehcm.responses.ResponseObject;
import com.sp26se041.edubridgehcm.utils.ResponseBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size:100MB}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:100MB}")
    private String maxRequestSize;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseObject> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        String message = "Dung lượng file hoặc ảnh vượt quá giới hạn cho phép."
                + " Tối đa mỗi file: " + maxFileSize
                + ", tối đa mỗi request: " + maxRequestSize + ".";
        return ResponseBuilder.build(HttpStatus.BAD_REQUEST, message, null);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ResponseObject> handleMultipartException(MultipartException ex) {
        String message = "Dữ liệu upload không hợp lệ hoặc vượt quá giới hạn."
                + " Vui lòng kiểm tra định dạng và dung lượng file/ảnh (mỗi file <= " + maxFileSize + ").";
        return ResponseBuilder.build(HttpStatus.BAD_REQUEST, message, null);
    }
}
