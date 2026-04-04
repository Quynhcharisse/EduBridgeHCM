package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Conversation;
import com.sp26se041.edubridgehcm.models.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByParentEmailAndCounsellorEmail(String parentEmail, String counsellorEmail);

    Optional<Conversation> findByParentEmailAndCampusIdAndStudentProfile(String parentEmail, int campusId, StudentProfile studentProfile);

    List<Conversation> findTop20ByParentEmailAndStudentProfileIsNotNullOrderByIdDesc(String parentEmail);

    List<Conversation> findTop20ByParentEmailAndIdLessThanAndStudentProfileIsNotNullOrderByIdDesc(String parentEmail, Long idIsLessThan);

    List<Conversation> findTop20ByCounsellorEmailAndStatusAndStudentProfileIsNotNullOrderByIdDesc(String email, Status status);

    List<Conversation> findTop20ByCounsellorEmailAndStatusAndIdLessThanAndStudentProfileIsNotNullOrderByIdDesc(String counsellorEmail, Status status, Long idIsLessThan);

    List<Conversation> findTop20ByCampusIdAndStatusAndStudentProfileIsNotNullOrderByIdDesc(int campusId, Status status);

    List<Conversation> findTop20ByCampusIdAndStatusAndIdLessThanAndStudentProfileIsNotNullOrderByIdDesc(int campusId, Status status, Long idIsLessThan);
}
