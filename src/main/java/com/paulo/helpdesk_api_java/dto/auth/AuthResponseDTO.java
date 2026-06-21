package com.paulo.helpdesk_api_java.dto.auth;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
