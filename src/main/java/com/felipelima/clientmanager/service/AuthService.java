package com.felipelima.clientmanager.service;

import com.felipelima.clientmanager.dto.request.LoginRequest;
import com.felipelima.clientmanager.dto.response.LoginResponse;
import com.felipelima.clientmanager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * Flow:
     * 1. AuthenticationManager calls CustomUserDetailsService.loadUserByUsername()
     * 2. Compares the provided password with the BCrypt hash from the database
     * 3. If valid, returns an Authentication object with user details
     * 4. JwtTokenProvider generates a signed token from the authenticated user
     * 5. Returns the token + role to the client
     *
     * If credentials are wrong, Spring Security throws BadCredentialsException automatically.
     */
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication);

        String role = authentication.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");

        return new LoginResponse(token, role);
    }
}