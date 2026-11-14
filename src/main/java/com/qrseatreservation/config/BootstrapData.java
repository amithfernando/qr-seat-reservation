package com.qrseatreservation.config;

import com.qrseatreservation.core.enums.EventStatus;
import com.qrseatreservation.core.model.Event;
import com.qrseatreservation.core.repository.EventRepository;
import java.util.HashSet;
import java.util.Set;

import com.qrseatreservation.core.enums.Role;
import com.qrseatreservation.core.model.User;
import com.qrseatreservation.core.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapData {

    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder, EventRepository events) {
        return args -> {
            if (users.count() == 0) {
                Set<Event> active = events.findFirstByStatus(EventStatus.ACTIVE).map(Set::of).orElse(Set.of());
                users.save(User.builder()
                        .username("admin")
                        .password(encoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .events(new HashSet<>(active))
                        .build());
                users.save(User.builder()
                        .username("entrance")
                        .password(encoder.encode("entrance123"))
                        .role(Role.ENTRANCE)
                        .enabled(true)
                        .events(new HashSet<>(active))
                        .build());
            }
        };
    }
}