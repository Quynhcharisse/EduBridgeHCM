package com.sp26se041.edubridgehcm;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.Relationship;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Campus;
import com.sp26se041.edubridgehcm.models.Counsellor;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.models.School;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.CampusRepo;
import com.sp26se041.edubridgehcm.repositories.CounsellorRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import com.sp26se041.edubridgehcm.repositories.SchoolRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
public class EduBridgeHcmApplication {

    private final AccountRepo accountRepo;

    private final ParentRepo parentRepo;

    private final SchoolRepo schoolRepo;

    private final CampusRepo campusRepo;

    private final CounsellorRepo counsellorRepo;

    public static void main(String[] args) {
        SpringApplication.run(EduBridgeHcmApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            initAdmin();
            initParent();
            initPrimaryCampusAndCounsellor();
        };
    }

    private void initAdmin() {
        if (accountRepo.findByEmail("systemteacher08@gmail.com").isPresent()) {
            return;
        }

        accountRepo.save(
                Account.builder()
                        .email("systemteacher08@gmail.com")
                        .role(Role.ADMIN)
                        .firstLogin(true)
                        .registerDate(LocalDate.now())
                        .status(Status.ACCOUNT_ACTIVE)
                        .isRestricted(false)
                        .build()
        );
    }

    private void initParent() {
        if (accountRepo.findByEmail("quynhpvnse182895@fpt.edu.vn").isPresent()) {
            return;
        }

        Account account = accountRepo.save(
                Account.builder()
                        .email("quynhpvnse182895@fpt.edu.vn")
                        .role(Role.PARENT)
                        .firstLogin(false)
                        .registerDate(LocalDate.now())
                        .status(Status.ACCOUNT_ACTIVE)
                        .isRestricted(false)
                        .build());

        parentRepo.save(Parent.builder()
                .account(account)
                .relationship(Relationship.FATHER)
                .name("John Doe")
                .gender(Gender.MALE)
                .idCardNumber("123456789")
                .workplace("ABC Company")
                .occupation("Engineer")
                .currentAddress("District 1, Ho Chi Minh city")
                .build());
    }

    private void initPrimaryCampusAndCounsellor() {
        School school = resolveOrCreateSeedSchool();

        Account primaryCampusAccount = accountRepo.findByEmail("main-campus@edubridge.local")
                .orElseGet(() -> accountRepo.save(Account.builder()
                        .email("main-campus@edubridge.local")
                        .role(Role.SCHOOL)
                        .firstLogin(true)
                        .registerDate(LocalDate.now())
                        .status(Status.ACCOUNT_ACTIVE)
                        .isRestricted(false)
                        .build()));

        Campus primaryCampus = campusRepo.findBySchoolId(school.getId()).stream()
                .filter(campus -> Boolean.TRUE.equals(campus.getIsPrimaryBranch()))
                .findFirst()
                .orElseGet(() -> campusRepo.save(Campus.builder()
                        .school(school)
                        .account(primaryCampusAccount)
                        .name("Campus Chinh")
                        .phoneNumber("0900000000")
                        .address("Quan 1, TP Ho Chi Minh")
                        .status(Status.VERIFIED)
                        .isPrimaryBranch(true)
                        .build()));

        Account counsellorAccount = accountRepo.findByEmail("counsellor-main@edubridge.local")
                .orElseGet(() -> accountRepo.save(Account.builder()
                        .email("counsellor-main@edubridge.local")
                        .role(Role.COUNSELLOR)
                        .firstLogin(true)
                        .registerDate(LocalDate.now())
                        .status(Status.ACCOUNT_ACTIVE)
                        .isRestricted(false)
                        .build()));

        Optional<Counsellor> existingCounsellor = counsellorRepo.findByCampusId(primaryCampus.getId()).stream()
                .filter(counsellor -> counsellor.getAccount() != null
                        && counsellor.getAccount().getId().equals(counsellorAccount.getId()))
                .findFirst();

        if (existingCounsellor.isPresent()) {
            return;
        }

        counsellorRepo.save(Counsellor.builder()
                .account(counsellorAccount)
                .campus(primaryCampus)
                .name("Counsellor Chinh")
                .employeeCode(UUID.randomUUID())
                .build());
    }

    private School resolveOrCreateSeedSchool() {
        String seedTaxCode = "0312345678";

        return schoolRepo.findAll().stream()
                .filter(school -> seedTaxCode.equals(school.getTaxCode()))
                .findFirst()
                .orElseGet(() -> schoolRepo.save(
                        School.builder()
                                .name("EduBridge Seed School")
                                .taxCode(seedTaxCode)
                                .websiteUrl("https://edubridge.local")
                                .hotline("0900000000")
                                .averageRating(BigDecimal.ZERO)
                                .isFeatured(false)
                                .foundingDate(LocalDate.of(2020, 1, 1))
                                .build()
                ));
    }

}
