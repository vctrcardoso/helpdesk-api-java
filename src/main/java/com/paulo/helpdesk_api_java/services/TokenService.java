package com.paulo.helpdesk_api_java.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.services.exceptions.TokenGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.access-expiration-seconds:900}")
    private long accessExpirationSeconds;

    public String generateAccessToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getUsername())
                    .withClaim("role", user.getRole().name())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(Instant.now().plus(Duration.ofSeconds(accessExpirationSeconds)))
                    .sign(algorithm);
        } catch (JWTCreationException e) {
            throw new TokenGenerationException("Falha ao gerar o token de acesso.", e);
        }

    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (JWTVerificationException e) {
            return "";
        }
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }
}
