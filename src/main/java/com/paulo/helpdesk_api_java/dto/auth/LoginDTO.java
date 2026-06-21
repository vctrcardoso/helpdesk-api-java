package com.paulo.helpdesk_api_java.dto.auth;

import lombok.Getter;

@Getter
public class LoginDTO {

    private String email;
    private String password;

    public LoginDTO() {
    }

    public LoginDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

}