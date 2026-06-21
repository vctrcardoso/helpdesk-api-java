package com.paulo.helpdesk_api_java.resources;

import com.paulo.helpdesk_api_java.dto.auth.LoginDTO;
import com.paulo.helpdesk_api_java.dto.auth.LoginResponseDTO;
import com.paulo.helpdesk_api_java.dto.user.UserCreateDTO;
import com.paulo.helpdesk_api_java.entities.User;
import com.paulo.helpdesk_api_java.services.AuthenticationService;
import com.paulo.helpdesk_api_java.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthenticationResource {

    private final AuthenticationService service;

    public AuthenticationResource(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Valid UserCreateDTO data) {
        service.register(data);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginDTO data) {
        String login = service.login(data);
        return ResponseEntity.ok(new LoginResponseDTO(login));
    }
}
