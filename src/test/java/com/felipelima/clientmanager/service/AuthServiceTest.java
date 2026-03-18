package com.felipelima.clientmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.felipelima.clientmanager.dto.request.LoginRequest;
import com.felipelima.clientmanager.dto.response.LoginResponse;
import com.felipelima.clientmanager.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("admin", "123qwel@#");

        User userDetails = new User(
                "admin",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("Should login successfully and return JWT token")
    void loginSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("fake-jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("ADMIN", response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for invalid credentials")
    void loginInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should return USER role when user logs in")
    void loginAsUser() {
        LoginRequest userLogin = new LoginRequest("user", "123qwe123");

        User userDetails = new User(
                "user",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        Authentication userAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(userAuth);
        when(jwtTokenProvider.generateToken(userAuth)).thenReturn("user-jwt-token");

        LoginResponse response = authService.login(userLogin);

        assertEquals("USER", response.getRole());
        assertEquals("user-jwt-token", response.getToken());
    }
}
