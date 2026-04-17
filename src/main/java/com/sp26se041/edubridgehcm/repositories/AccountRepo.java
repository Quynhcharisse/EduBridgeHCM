package com.sp26se041.edubridgehcm.repositories;

import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepo extends JpaRepository<Account, Integer> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndStatus(String email, Status status);

    Page<Account> findByRoleOrderByIdDesc(Role role, Pageable pageable);

    List<Account> findByRole(Role role);

    boolean existsByEmail(String email);
}
