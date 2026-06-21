package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.dto.auth.LoginDTO;
import com.paulo.helpdesk_api_java.dto.user.UserCreateDTO;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceConflictException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthenticationService {

    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public User register(UserCreateDTO data) {
        if (repository.existsByEmailIgnoreCase(data.email())) {
            throw new ResourceConflictException(
                    "USER_EMAIL_ALREADY_EXISTS",
                    "Já existe um usuário cadastrado com este e-mail.");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());

        User user = User.fromCreateDTO(data, encryptedPassword);

        return repository.save(user);
    }

    public String login(LoginDTO data)
    {
        var login = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());
        Authentication authentication = this.authenticationManager.authenticate(login);
        return tokenService.generateToken((User) Objects.requireNonNull(authentication.getPrincipal()));
    }

}
