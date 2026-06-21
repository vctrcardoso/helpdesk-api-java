package com.paulo.helpdesk_api_java.dto.user;

public record UserCreateDTO(
        String name,
        String email,
        String password,
        String role
) {}
