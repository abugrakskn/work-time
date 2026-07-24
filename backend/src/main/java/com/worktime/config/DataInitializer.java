package com.worktime.config;

import com.worktime.entity.User;
import com.worktime.entity.UserRole;
import com.worktime.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(UserRepository userRepository) {
        return args -> {

            if (userRepository.count() == 0) {

                User admin = User.builder()
                        .firstName("Ahmet")
                        .lastName("Keskin")
                        .email("ahmet@test.com")
                        .password("123456")
                        .role(UserRole.ADMIN)
                        .active(true)
                        .build();

                User employee = User.builder()
                        .firstName("Ayşe")
                        .lastName("Yılmaz")
                        .email("ayse@test.com")
                        .password("abcdef")
                        .role(UserRole.EMPLOYEE)
                        .active(true)
                        .build();

                userRepository.save(admin);
                userRepository.save(employee);
            }
        };
    }
}