package com.SwitchBoard.AuthService.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())  // disable CSRF for APIs
                .authorizeHttpRequests(auth -> auth
                        // Permit all for auth endpoints and Swagger
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/auth/account/**",
                                "/.well-known/jwks.json",  // JWKS endpoint for JWT validation
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()  // everything else requires auth
                )
                .httpBasic(httpBasic -> httpBasic.disable())  // disable basic login popup
                .formLogin(form -> form.disable());           // disable default login form

        return http.build();
    }
}
