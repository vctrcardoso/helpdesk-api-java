package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.dto.user.UserCreateDTO;
import com.paulo.helpdesk_api_java.dto.user.UserResponseDTO;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceConflictException;
import com.paulo.helpdesk_api_java.services.exceptions.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponseDTO> findAll() {
        return repository.findAll().stream().map(UserResponseDTO::new).toList();
    }

    public UserResponseDTO findById(Long id) {

        return repository.findById(id)
                .map(UserResponseDTO::new)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
    }

    public UserResponseDTO insert(UserCreateDTO user) {
        validateUniqueEmail(user.email(), null);
        User entity = new User();
        updateUser(entity, user);
        User savedUser = repository.save(entity);

        return new UserResponseDTO(savedUser);
    }

    public void update(Long id, UserCreateDTO user) {
        User entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", id));
        validateUniqueEmail(user.email(), id);
        updateUser(entity, user);
        repository.save(entity);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", id);
        }
        repository.deleteById(id);
    }

    private void updateUser(User entity, UserCreateDTO user) {
        entity.setName(user.name());
        entity.setEmail(user.email());
        entity.setPassword(passwordEncoder.encode(user.password()));
        entity.setRole(UserRoles.valueOf(user.role()));
    }

    private void validateUniqueEmail(String email, Long currentUserId) {
        boolean alreadyExists = currentUserId == null
                ? repository.existsByEmailIgnoreCase(email)
                : repository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId);

        if (alreadyExists) {
            throw new ResourceConflictException(
                    "USER_EMAIL_ALREADY_EXISTS",
                    "Já existe um usuário cadastrado com este e-mail.");
        }
    }
}
