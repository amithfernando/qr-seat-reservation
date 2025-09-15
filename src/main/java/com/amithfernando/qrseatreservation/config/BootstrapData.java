package com.amithfernando.qrseatreservation.config;

import com.amithfernando.qrseatreservation.core.enums.Role;
import com.amithfernando.qrseatreservation.core.model.User;
import com.amithfernando.qrseatreservation.core.repsitory.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapData {

    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (users.count() == 0) {
                users.save(User.builder()
                        .username("admin")
                        .password(encoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build());
                users.save(User.builder()
                        .username("entrance")
                        .password(encoder.encode("entrance123"))
                        .role(Role.ENTRANCE)
                        .enabled(true)
                        .build());
            }
        };
    }
}