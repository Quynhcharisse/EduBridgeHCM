package com.sp26se041.edubridgehcm;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.Relationship;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.models.Parent;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import com.sp26se041.edubridgehcm.repositories.ParentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.time.LocalDate;

@SpringBootApplication
@EnableAspectJAutoProxy // Enable AOP for restricted account checks
@RequiredArgsConstructor
public class EduBridgeHcmApplication {

    private final AccountRepo accountRepo;

    private final ParentRepo parentRepo;

    public static void main(String[] args) {
        SpringApplication.run(EduBridgeHcmApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (accountRepo.count() == 0) {
                // Admin account
                accountRepo.save(
                        Account.builder()
                                .email("systemteacher08@gmail.com")
                                .role(Role.ADMIN)
                                .firstLogin(true)
                                .registerDate(LocalDate.now())
                                .phone("0703345054")
                                .address("FPT University, Ho Chi Minh city")
                                .status(Status.ACCOUNT_ACTIVE)
                                .build()
                );

                Account account = accountRepo.save(
                        Account.builder()
                                .email("quynhpvnse182895@fpt.edu.vn")
                                .role(Role.PARENT)
                                .firstLogin(false)
                                .registerDate(LocalDate.now())
                                .phone("0901234567")
                                .address("District 1, Ho Chi Minh city")
                                .status(Status.ACCOUNT_ACTIVE)
                                .parent(null)
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
        };
    }

}
