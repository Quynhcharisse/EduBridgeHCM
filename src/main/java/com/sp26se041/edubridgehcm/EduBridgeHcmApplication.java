package com.sp26se041.edubridgehcm;

import com.sp26se041.edubridgehcm.enums.Gender;
import com.sp26se041.edubridgehcm.enums.Role;
import com.sp26se041.edubridgehcm.enums.Status;
import com.sp26se041.edubridgehcm.models.Account;
import com.sp26se041.edubridgehcm.repositories.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
@RequiredArgsConstructor
public class EduBridgeHcmApplication {

    private final AccountRepo accountRepo;

    public static void main(String[] args) {
        SpringApplication.run(EduBridgeHcmApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (accountRepo.count() == 0) {
                accountRepo.save(
                        Account.builder()
                                .email("systemteacher08@gmail.com")
                                .role(Role.ADMIN)
                                .name("System Admin")
                                .firstLogin(true)
                                .registerDate(LocalDate.now())
                                .phone("0703345054")
                                .address("FPT University, Ho Chi Minh city")
                                .gender(Gender.BOY)
                                .status(Status.ACCOUNT_ACTIVE)
                                .build()
                );
            }
        };
    }

}
