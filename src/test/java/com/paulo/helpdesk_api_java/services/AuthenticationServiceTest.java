package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.dto.auth.RegisterDTO;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

    @Test
    void publicRegistrationMustAlwaysCreateARegularUser() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        TokenService tokenService = mock(TokenService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);

        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthenticationService service = new AuthenticationService(
                repository,
                passwordEncoder,
                authenticationManager,
                tokenService,
                refreshTokenService);

        User registered = service.register(
                new RegisterDTO("Paulo", "paulo@example.com", "password"));

        assertEquals(UserRoles.ROLE_USER, registered.getRole());
        verify(repository).save(registered);
    }
}
