package com.paulo.helpdesk_api_java.entities.enums;

public enum UserRoles {
    ROLE_USER("user"),
    ROLE_ADMIN("admin");

    private String role;

    UserRoles(String role) {
        this.role = role;
    }

    public String getRole()
    {
        return role;
    }
}
