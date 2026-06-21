package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.entities.RefreshToken;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import com.paulo.helpdesk_api_java.repositories.RefreshTokenRepository;
import com.paulo.helpdesk_api_java.services.exceptions.InvalidRefreshTokenException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    @Test
    void consumingTokenMustRevokeItAndReturnItsUser() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshToken token = activeToken();
        when(repository.findByTokenHash(any())).thenReturn(Optional.of(token));
        RefreshTokenService service = new RefreshTokenService(repository, 3600);

        RefreshTokenService.TokenRotation result = service.rotate("raw-token");

        assertSame(token.getUser(), result.user());
        assertNotNull(token.getRevokedAt());
        assertNotNull(result.refreshToken());
    }

    @Test
    void revokedTokenCannotBeReused() {
        RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
        RefreshToken token = activeToken();
        token.setRevokedAt(Instant.now());
        when(repository.findByTokenHash(any())).thenReturn(Optional.of(token));
        RefreshTokenService service = new RefreshTokenService(repository, 3600);

        assertThrows(InvalidRefreshTokenException.class, () -> service.rotate("raw-token"));
    }

    private RefreshToken activeToken() {
        RefreshToken token = new RefreshToken();
        token.setTokenHash("hash");
        token.setUser(new User(1L, "User", "user@example.com", "secret", UserRoles.ROLE_USER));
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        return token;
    }
}
