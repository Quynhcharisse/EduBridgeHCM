package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.models.Campus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CampusRepo extends JpaRepository<Campus, Integer> {

    List<Campus> findBySchoolId(Integer schoolId);

    List<Campus> findBySchoolIdIn(List<Integer> schoolIds);

    Page<Campus> findBySchoolIdOrderByIsPrimaryBranchDescIdDesc(Integer schoolId, Pageable pageable);

    Optional<Campus> findByIdAndSchoolId(Integer id, Integer schoolId);

    boolean existsBySchoolIdAndNameEqualsIgnoreCase(Integer schoolId, String campusName);

    int countBySchoolId(Integer schoolId);

    //Vì đang lưu latitude và longitude kiểu Double,
    // nên sử dụng Native Query.
    // Việc tính toán tại DB giúp loại bỏ hàng nghìn bản ghi
    //  ==> không thỏa mãn trước khi dữ liệu đổ về RAM của server.
    @Query(value = "SELECT * FROM campus c " +
            "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) * " +
            "cos(radians(c.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(c.latitude)))) <= :radius " +
            "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude)) * " +
            "cos(radians(c.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(c.latitude)))) ASC",
            nativeQuery = true)
    List<Campus> findNearbyCampuses(@Param("lat") Double lat,
                                    @Param("lng") Double lng,
                                    @Param("radius") Double radius);

    List<Campus> findAllBySchoolId(Integer schoolId);
}
