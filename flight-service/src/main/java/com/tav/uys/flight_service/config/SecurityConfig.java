package com.tav.uys.flight_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Dokümantasyon ve Swagger arayüzü açık kalsın[cite: 1, 2]
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // 2. Canlı veri akışı için WebSocket bağlantısı serbest olsun[cite: 1, 2]
                        .requestMatchers("/ws-uys/**").permitAll()
                        // 3. Login / Auth endpoint'leri serbest olsun[cite: 1, 2]
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 4. Uçuş operasyonları ve diğer tüm istekler Keycloak JWT Token'ı gerektirsin
                        .anyRequest().authenticated()
                )
                // Keycloak'tan gelen Bearer JWT token'ı doğrula
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}