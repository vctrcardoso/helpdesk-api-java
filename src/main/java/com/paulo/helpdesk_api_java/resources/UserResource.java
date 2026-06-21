package com.paulo.helpdesk_api_java.resources;

import com.paulo.helpdesk_api_java.dto.user.UserCreateDTO;
import com.paulo.helpdesk_api_java.dto.user.UserResponseDTO;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.repositories.UserRepository;
import com.paulo.helpdesk_api_java.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/users")
public class UserResource {

    private final UserService service;

    UserResource(UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        List<UserResponseDTO> obj = service.findAll();
        return ResponseEntity.ok().body(obj);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        UserResponseDTO obj = service.findById(id);
        return ResponseEntity.ok().body(obj);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> insert(@RequestBody UserCreateDTO obj) {
        UserResponseDTO response = service.insert(obj);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);

    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Void> insert(@PathVariable Long id, @RequestBody UserCreateDTO obj) {
        service.update(id, obj);
        return ResponseEntity.ok().build();

    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();

    }

}
