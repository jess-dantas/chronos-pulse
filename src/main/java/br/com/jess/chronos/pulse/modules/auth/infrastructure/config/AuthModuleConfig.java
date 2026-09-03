package br.com.jess.chronos.pulse.modules.auth.infrastructure.config;

import br.com.jess.chronos.pulse.modules.auth.application.usecases.AutenticarUsuarioUseCaseImpl;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthModuleConfig {

    @Bean
    public AutenticarUsuarioUseCase autenticarUsuarioUseCase(
            CpcUsuarioRepositoryPort repositoryPort,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        return new AutenticarUsuarioUseCaseImpl(repositoryPort, jwtService, passwordEncoder);
    }
}
