package com.paulo.helpdesk_api_java.services;

import com.paulo.helpdesk_api_java.entities.RefreshToken;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.repositories.RefreshTokenRepository;
import com.paulo.helpdesk_api_java.services.exceptions.InvalidRefreshTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final Duration refreshTokenDuration;

    public RefreshTokenService(
            RefreshTokenRepository repository,
            @Value("${api.security.token.refresh-expiration-seconds:604800}") long refreshExpirationSeconds) {
        this.repository = repository;
        this.refreshTokenDuration = Duration.ofSeconds(refreshExpirationSeconds);
    }

    @Transactional
    public String create(User user) {
        String rawToken = generateSecureToken();
        save(user, rawToken);
        return rawToken;
    }

    @Transactional
    public TokenRotation rotate(String rawToken) {
        RefreshToken refreshToken = find(rawToken);
        Instant now = Instant.now();

        if (!refreshToken.isActive(now)) {
            throw new InvalidRefreshTokenException("O refresh token está expirado ou foi revogado.");
        }

        refreshToken.setRevokedAt(now);
        String newRawToken = generateSecureToken();
        save(refreshToken.getUser(), newRawToken);
        return new TokenRotation(refreshToken.getUser(), newRawToken);
    }

    private void save(User user, String rawToken) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenDuration));
        repository.save(refreshToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        RefreshToken refreshToken = find(rawToken);
        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(Instant.now());
        }
    }

    private RefreshToken find(String rawToken) {
        return repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido."));
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }

    public record TokenRotation(User user, String refreshToken) {
    }
}
