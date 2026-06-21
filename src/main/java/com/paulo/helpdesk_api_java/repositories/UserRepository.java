package com.paulo.helpdesk_api_java.repositories;

import com.paulo.helpdesk_api_java.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<User, Long> {

    UserDetails findByEmail(String email);
}
