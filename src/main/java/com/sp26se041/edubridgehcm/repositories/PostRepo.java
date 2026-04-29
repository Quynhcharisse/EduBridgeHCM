package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.CategoryPost;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepo extends JpaRepository<Post, Integer> {

    List<Post> findAllByStatusOrderByPublishedDateDesc(Status status);

//check
    long countByAuthorCampusSchoolIdAndCategoryPostInAndPublishedDateBetween(
            Integer schoolId,
            List<CategoryPost> categories,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    long countByAuthor_Campus_Id(Integer campusId);
}
