package com.paulo.helpdesk_api_java.dto.user;

import com.paulo.helpdesk_api_java.entities.User;

public record UserResponseDTO(
        Long id,
        String name,
        String email
) {
    public UserResponseDTO(User user) {
        this(user.getId(), user.getName(), user.getEmail());
    }
}