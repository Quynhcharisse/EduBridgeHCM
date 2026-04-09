package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.CategoryTemplate;
import com.sp26se041.edubridgehcm.models.TemplateDocx;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateDocxRepo extends JpaRepository<TemplateDocx, Long> {
    Optional<TemplateDocx> findTopByTypeOrderByVersionDesc(CategoryTemplate type);

    List<TemplateDocx> findByTypeOrderByVersionDesc(CategoryTemplate type);
}
