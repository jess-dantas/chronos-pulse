package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.config;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.application.usecases.AtualizarColaboradorUseCaseImpl;
import br.com.jess.chronos.pulse.modules.colaborador.application.usecases.CadastrarColaboradorUseCaseImpl;
import br.com.jess.chronos.pulse.modules.colaborador.application.usecases.ExcluirColaboradorUseCaseImpl;
import br.com.jess.chronos.pulse.modules.colaborador.application.usecases.ListarColaboradoresUseCaseImpl;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.AtualizarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.CadastrarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ExcluirColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ListarColaboradoresUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class ColaboradorModuleConfig {

    @Bean
    @Transactional
    public CadastrarColaboradorUseCase cadastrarColaboradorUseCase(
            ColaboradorRepositoryPort colaboradorRepository,
            CpcUsuarioRepositoryPort usuarioRepository,
            PasswordEncoder passwordEncoder,
            ModulosPort modulosPort) {
        return new CadastrarColaboradorUseCaseImpl(colaboradorRepository, usuarioRepository, passwordEncoder, modulosPort);
    }

    @Bean
    public ListarColaboradoresUseCase listarColaboradoresUseCase(
            ColaboradorRepositoryPort colaboradorRepository,
            CpcUsuarioRepositoryPort usuarioRepository) {
        return new ListarColaboradoresUseCaseImpl(colaboradorRepository, usuarioRepository);
    }

    @Bean
    @Transactional
    public AtualizarColaboradorUseCase atualizarColaboradorUseCase(
            ColaboradorRepositoryPort colaboradorRepository,
            CpcUsuarioRepositoryPort usuarioRepository,
            ModulosPort modulosPort) {
        return new AtualizarColaboradorUseCaseImpl(colaboradorRepository, usuarioRepository, modulosPort);
    }

    @Bean
    @Transactional
    public ExcluirColaboradorUseCase excluirColaboradorUseCase(
            ColaboradorRepositoryPort colaboradorRepository,
            CpcUsuarioRepositoryPort usuarioRepository) {
        return new ExcluirColaboradorUseCaseImpl(colaboradorRepository, usuarioRepository);
    }
}
