package br.com.jess.chronos.pulse.modules.admin.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class AdminSecurityConfig {

    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http,
            br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .securityMatcher("/admin/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // login/logout/verify anônimos; bootstrap/status/recover/
                        // setup/confirm aceitam tempToken (typ=two_factor) e ficam
                        // abertos — a validação de token é feita no controller;
                        // status/alterar-senha/disable exigem ADMIN_PLATAFORMA.
                        .requestMatchers(
                                "/admin/auth/login",
                                "/admin/auth/logout",
                                "/admin/auth/2fa/verify",
                                "/admin/auth/bootstrap",
                                "/admin/auth/bootstrap/status",
                                "/admin/auth/2fa/setup",
                                "/admin/auth/2fa/confirm",
                                "/admin/auth/2fa/recover"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN_PLATAFORMA")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}