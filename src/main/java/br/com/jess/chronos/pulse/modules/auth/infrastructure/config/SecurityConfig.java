package br.com.jess.chronos.pulse.modules.auth.infrastructure.config;

import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN_PLATAFORMA")
                        .requestMatchers("/api/v1/suporte/**").hasAnyRole("SUPORTE_N1", "SUPORTE_N2")
                        .requestMatchers(HttpMethod.POST, "/api/v1/empresas/**").hasRole("ADMIN_PLATAFORMA")
                        .requestMatchers(HttpMethod.POST, "/api/v1/colaboradores/**").hasRole("ADMIN_EMPRESA")
                        .requestMatchers("/api/v1/pontos/**").hasAnyRole("COLABORADOR", "ADMIN_EMPRESA")
                        .requestMatchers("/api/v1/fiscal/**").hasAnyRole("ADMIN_EMPRESA", "ADMIN_PLATAFORMA")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
