package com.paulo.helpdesk_api_java.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.paulo.helpdesk_api_java.dto.auth.RegisterDTO;
import com.paulo.helpdesk_api_java.dto.user.UserCreateDTO;
import com.paulo.helpdesk_api_java.entities.enums.UserRoles;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "tb_user")
public class User implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRoles role;

    @JsonIgnore
    @OneToMany(mappedBy = "client")
    private List<Ticket> ticketsCreated;

    @JsonIgnore
    @OneToMany(mappedBy = "attendant")
    private List<Ticket> ticketsAssigned;

    public User() {
    }

    public User(Long id, String name, String email, String password, UserRoles role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User fromCreateDTO(UserCreateDTO data, String encryptedPassword) {
        User user = new User();
        user.setName(data.name());
        user.setEmail(data.email());
        user.setPassword(encryptedPassword);
        user.setRole(UserRoles.valueOf(data.role()));

        return user;
    }

    public static User fromRegisterDTO(RegisterDTO data, String encryptedPassword) {
        User user = new User();
        user.setName(data.name());
        user.setEmail(data.email());
        user.setPassword(encryptedPassword);
        user.setRole(UserRoles.ROLE_USER);
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
}
