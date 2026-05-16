package com.sp26se041.edubridgehcm.services;

import com.sp26se041.edubridgehcm.requests.AddFavouriteSchoolRequest;
import com.sp26se041.edubridgehcm.requests.AddStudentInfoRequest;
import com.sp26se041.edubridgehcm.requests.AutoFillTranscriptRequest;
import com.sp26se041.edubridgehcm.requests.CancelAdmissionFormRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionReservationFormRequest;
import com.sp26se041.edubridgehcm.requests.CreateAdmissionReservationFormTemplateRequest;
import com.sp26se041.edubridgehcm.requests.CreateConsultationOfflineRequest;
import com.sp26se041.edubridgehcm.requests.CreateConversationRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionReservationFormRequest;
import com.sp26se041.edubridgehcm.requests.UpdateAdmissionReservationFormTemplateRequest;
import com.sp26se041.edubridgehcm.requests.UpdateStudentInfoRequest;
import com.sp26se041.edubridgehcm.responses.ResponseObject;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface ParentService {

    ResponseEntity<ResponseObject> getConversations(Long cursorId);

    ResponseEntity<ResponseObject> getChatHistory(String parentEmail, int campusId, int studentProfileId, Long cursorId);
    //Personality types
    ResponseEntity<ResponseObject> getPersonalityTypes();

    //Majors
    ResponseEntity<ResponseObject> getAllMajors();
    //Subjects
    ResponseEntity<ResponseObject> getAllSubjects();

    ResponseEntity<ResponseObject> addStudentInfo(AddStudentInfoRequest request);

    ResponseEntity<ResponseObject> updateStudentInfo(UpdateStudentInfoRequest request);

    ResponseEntity<ResponseObject> getStudents();

    ResponseEntity<ResponseObject> autoFillTranscript(AutoFillTranscriptRequest request);

    ResponseEntity<ResponseObject> getStudentInfo(int studentProfileId);

    //Favourite school
    ResponseEntity<ResponseObject> addFavouriteSchool(AddFavouriteSchoolRequest request);

    ResponseEntity<ResponseObject> getFavouriteSchools(int page, int size);

    ResponseEntity<ResponseObject> removeFavouriteSchool(long favouriteSchoolId);

    ResponseEntity<ResponseObject> createConversation(CreateConversationRequest request);

    List<String> findCounsellorEmailsByCampusId(int campusId);

    ResponseEntity<ResponseObject> getSlots(int campusId, LocalDate startDate, LocalDate endDate);

    ResponseEntity<ResponseObject> createConsultationOfflineRequest(CreateConsultationOfflineRequest request);

    ResponseEntity<ResponseObject> getConsultationOfflineRequests(String status, int page, int size);

    ResponseEntity<ResponseObject> createAdmissionReservationForm(CreateAdmissionReservationFormRequest request);

    ResponseEntity<ResponseObject> createAdmissionReservationTemplateForm(CreateAdmissionReservationFormTemplateRequest request);

    ResponseEntity<ResponseObject> getAdmissionReservationTemplateForm(int studentProfileId);

    ResponseEntity<ResponseObject> updateAdmissionReservationFormTemplate(UpdateAdmissionReservationFormTemplateRequest request);

    ResponseEntity<ResponseObject> getReservationFeeFromCampusProgramOffering(int campusProgramOfferingId);

    ResponseEntity<ResponseObject> getRequiredDocuments();

    ResponseEntity<ResponseObject> updateAdmissionReservationForm(UpdateAdmissionReservationFormRequest request);

    ResponseEntity<ResponseObject> getDocumentRequirements( int campusProgramOfferingId);

    ResponseEntity<ResponseObject> getAdmissionReservationForms(String status);

    ResponseEntity<ResponseObject> cancelAdmissionReservationForm(CancelAdmissionFormRequest request);

    ResponseEntity<ResponseObject> getAvailableSchools(List<Integer> schoolIds, int studentProfileId);
}
