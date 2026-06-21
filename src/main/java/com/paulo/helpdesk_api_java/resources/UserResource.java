package com.paulo.helpdesk_api_java.resources;

import com.paulo.helpdesk_api_java.config.ApiPaths;
import com.paulo.helpdesk_api_java.dto.user.UserCreateDTO;
import com.paulo.helpdesk_api_java.dto.user.UserResponseDTO;
import com.paulo.helpdesk_api_java.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.USERS)
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
@PreAuthorize("hasRole('ADMIN')")
public class UserResource {

    private final UserService service;

    UserResource(UserService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar usuários")
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        List<UserResponseDTO> obj = service.findAll();
        return ResponseEntity.ok().body(obj);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        UserResponseDTO obj = service.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping
    @Operation(summary = "Criar usuário")
    public ResponseEntity<UserResponseDTO> insert(@RequestBody @Valid UserCreateDTO obj) {
        UserResponseDTO response = service.insert(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);

    }

    @PutMapping(value = "/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody @Valid UserCreateDTO obj) {
        service.update(id, obj);
        return ResponseEntity.ok().build();

    }

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Excluir usuário")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();

    }

}
