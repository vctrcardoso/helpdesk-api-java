package com.paulo.helpdesk_api_java.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "O refresh token é obrigatório")
        String refreshToken
) {
}
