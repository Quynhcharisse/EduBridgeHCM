package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolRepo extends JpaRepository<School, Integer> {

    boolean existsByTaxCode(String taxCode);

    Page<School> findAllByOrderByIdDesc(Pageable pageable);

    List<School> findAllByOrderByIsFeaturedDescAverageRatingDesc();
}
