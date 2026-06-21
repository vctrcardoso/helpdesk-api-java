package com.paulo.helpdesk_api_java.resources;

import com.paulo.helpdesk_api_java.config.ApiPaths;
import com.paulo.helpdesk_api_java.dto.auth.AuthResponseDTO;
import com.paulo.helpdesk_api_java.dto.auth.LoginDTO;
import com.paulo.helpdesk_api_java.dto.auth.RefreshTokenRequestDTO;
import com.paulo.helpdesk_api_java.dto.auth.RegisterDTO;
import com.paulo.helpdesk_api_java.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.AUTH)
@Tag(name = "Autenticação", description = "Cadastro, login e emissão de token JWT")
public class AuthenticationResource {

    private final AuthenticationService service;

    public AuthenticationResource(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @Operation(summary = "Cadastrar usuário", description = "Cria uma nova conta de usuário.")
    @SecurityRequirements
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO data) {
        service.register(data);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida as credenciais e retorna um token JWT.")
    @SecurityRequirements
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginDTO data) {
        return ResponseEntity.ok(service.login(data));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar sessão", description = "Rotaciona o refresh token e emite um novo par de tokens.")
    @SecurityRequirements
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO data) {
        return ResponseEntity.ok(service.refresh(data.refreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerrar sessão", description = "Revoga o refresh token informado.")
    @SecurityRequirements
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequestDTO data) {
        service.logout(data.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
