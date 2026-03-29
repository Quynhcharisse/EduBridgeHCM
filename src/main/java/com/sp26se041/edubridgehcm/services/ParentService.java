package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.models.ChatMessage;
import com.sp26se041.edubridgehcm.requests.AddFavouriteSchoolRequest;
import com.sp26se041.edubridgehcm.requests.AddStudentInfoRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface ParentService {
    ResponseEntity<ResponseObject> getConversations(Long cursorId);

    //Personality types
    ResponseEntity<ResponseObject> getPersonalityTypes();

    //Majors
    ResponseEntity<ResponseObject> getAllMajors();
    //Subjects
    ResponseEntity<ResponseObject> getAllSubjects();

    ResponseEntity<ResponseObject> addStudentInfo(AddStudentInfoRequest request);

    ResponseEntity<ResponseObject> getStudents();
    //Favourite school

    ResponseEntity<ResponseObject> addFavouriteSchool(AddFavouriteSchoolRequest request);
    ResponseEntity<ResponseObject> getFavouriteSchools(int page, int size);
}
