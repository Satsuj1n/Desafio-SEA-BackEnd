package com.felipelima.clientmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.felipelima.clientmanager.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Main security configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF: API is stateless, uses JWT instead of cookies
                .csrf().disable()

                // No server-side sessions: JWT handles authentication state
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // Route-level authorization rules
                .authorizeRequests()
                // Public endpoints (no authentication needed)
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/swagger-ui/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs/**").permitAll()

                // Read endpoints: any authenticated user (ADMIN or USER)
                .antMatchers(HttpMethod.GET, "/clients/**").authenticated()
                .antMatchers(HttpMethod.GET, "/cep/**").authenticated()

                // Write endpoints: ADMIN only
                .antMatchers(HttpMethod.POST, "/clients/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/clients/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/clients/**").hasRole("ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
                .and()

                // Allow H2 console to render in frames
                .headers().frameOptions().sameOrigin()
                .and()

                // Register our JWT filter BEFORE Spring's default authentication filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Password encoder: BCrypt is the industry standard.
     * Same algorithm Django uses by default (BCryptSHA256PasswordHasher).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager: processes login attempts.
     * Automatically connects to CustomUserDetailsService + PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
