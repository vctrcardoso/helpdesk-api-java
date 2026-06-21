package com.paulo.helpdesk_api_java.config;

import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdminConfig {

    @Bean
    @ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
    CommandLineRunner bootstrapAdmin(
            UserRepository repository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.name}") String name,
            @Value("${app.bootstrap-admin.email}") String email,
            @Value("${app.bootstrap-admin.password}") String password) {
        return args -> {
            if (!repository.existsByEmailIgnoreCase(email)) {
                User admin = new User(
                        null,
                        name,
                        email,
                        passwordEncoder.encode(password),
                        UserRoles.ROLE_ADMIN);
                repository.save(admin);
            }
        };
    }
}
