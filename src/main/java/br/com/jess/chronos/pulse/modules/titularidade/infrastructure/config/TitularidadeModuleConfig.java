package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.config;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailCodigoTitularidadeService;
import br.com.jess.chronos.pulse.modules.titularidade.application.usecases.TransferirTitularidadeUseCaseImpl;
import br.com.jess.chronos.pulse.modules.titularidade.domain.ports.input.TransferirTitularidadeUseCase;
import br.com.jess.chronos.pulse.modules.titularidade.domain.ports.output.TitularidadeRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class TitularidadeModuleConfig {

    @Bean
    @Transactional
    public TransferirTitularidadeUseCase transferirTitularidadeUseCase(
            TitularidadeRepositoryPort titularidadeRepository,
            CpcUsuarioRepositoryPort usuarioRepository,
            ModulosPort modulosPort,
            PasswordEncoder passwordEncoder,
            EmailCodigoTitularidadeService emailCodigoTitularidadeService) {
        return new TransferirTitularidadeUseCaseImpl(
                titularidadeRepository, usuarioRepository, modulosPort,
                passwordEncoder, emailCodigoTitularidadeService);
    }
}
