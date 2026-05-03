package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.FavouriteSchool;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavouriteSchoolRepo extends JpaRepository<FavouriteSchool, Long> {

    boolean existsByParentAndSchool(Parent parent, School school);

    Page<FavouriteSchool> findByParentIdOrderByCreatedAtDesc(Integer parentId, Pageable pageable);

    List<FavouriteSchool> findByParentId(Integer parentId);

    List<FavouriteSchool> findBySchoolId(Integer schoolId);
}
