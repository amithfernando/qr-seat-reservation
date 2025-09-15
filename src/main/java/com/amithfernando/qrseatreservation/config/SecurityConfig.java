package com.amithfernando.qrseatreservation.config;

import com.amithfernando.qrseatreservation.core.repsitory.UserRepository;
import com.amithfernando.qrseatreservation.ui.view.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(u -> org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles(u.getRole().name()) // Spring Security expects role names without "ROLE_" prefix here
                        .disabled(!u.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Define custom authorization rules BEFORE calling super.configure(http)
        // Define only explicit matchers BEFORE super.configure(http). Do not use anyRequest() here.
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/login/**").permitAll()
                .requestMatchers("/qr", "/qr/**").hasAnyRole("ADMIN", "ENTRANCE")
        );

        // Let Vaadin configure internal endpoints and finalize the chain
        super.configure(http);

        // Use Vaadin login view
        setLoginView(http, LoginView.class);
    }
}
