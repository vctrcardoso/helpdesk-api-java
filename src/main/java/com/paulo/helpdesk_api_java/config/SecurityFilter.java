package com.paulo.helpdesk_api_java.config;

import com.paulo.helpdesk_api_java.repositories.UserRepository;
import com.paulo.helpdesk_api_java.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    final TokenService tokenService;

    final UserRepository repository;

    public SecurityFilter(TokenService tokenService, UserRepository repository) {
        this.tokenService = tokenService;
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recoverToken(request);

        if (token != null) {
            String login = tokenService.validateToken(token);

            if (login != null && !login.isBlank()) {
                UserDetails user = repository.findByEmail(login);

                if (user != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);

    }

    private String recoverToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header == null || header.isBlank()) {
            return null;
        }

        if (!header.startsWith("Bearer ")) {
            return null;
        }

        return header
                .substring(7)
                .replace("\"", "")
                .trim();
    }
}
