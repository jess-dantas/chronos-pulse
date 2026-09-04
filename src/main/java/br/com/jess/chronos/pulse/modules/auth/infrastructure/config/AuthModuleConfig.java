package br.com.jess.chronos.pulse.modules.auth.infrastructure.config;

import br.com.jess.chronos.pulse.modules.auth.application.usecases.AutenticarUsuarioUseCaseImpl;
import br.com.jess.chronos.pulse.modules.auth.application.usecases.BuscarPerfilUseCaseImpl;
import br.com.jess.chronos.pulse.modules.auth.application.usecases.CadastrarEmpresaCompletoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.auth.application.usecases.RefreshTokenUseCaseImpl;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.BuscarPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.CadastrarEmpresaCompletoUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RefreshTokenUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
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

    @Bean
    public CadastrarEmpresaCompletoUseCase cadastrarEmpresaCompletoUseCase(
            EmpresaRepositoryPort empresaRepository,
            CpcUsuarioRepositoryPort usuarioRepository,
            ColaboradorRepositoryPort colaboradorRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {
        return new CadastrarEmpresaCompletoUseCaseImpl(
                empresaRepository, usuarioRepository, colaboradorRepository, jwtService, passwordEncoder);
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            CpcUsuarioRepositoryPort repositoryPort,
            JwtService jwtService) {
        return new RefreshTokenUseCaseImpl(repositoryPort, jwtService);
    }

    @Bean
    public BuscarPerfilUseCase buscarPerfilUseCase(CpcUsuarioRepositoryPort repositoryPort) {
        return new BuscarPerfilUseCaseImpl(repositoryPort);
    }
}
