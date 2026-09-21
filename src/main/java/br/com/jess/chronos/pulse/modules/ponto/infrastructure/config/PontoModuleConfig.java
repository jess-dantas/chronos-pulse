package br.com.jess.chronos.pulse.modules.ponto.infrastructure.config;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.ConfiguracaoJornadaRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.AjustarPontoManualUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.AprovarAjustePontoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.ConsultarEspelhoPontoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.ConsultarRelatorioEspelhoPontoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.ListarAjustesPendentesUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.RejeitarAjustePontoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.RegistrarPontoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.SolicitarAjustePontoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.AjustarPontoManualUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.AprovarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarRelatorioEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ListarAjustesPendentesUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RejeitarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.SolicitarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PontoModuleConfig {

    @Bean
    public RegistrarPontoUseCase registrarPontoUseCase(RegistroPontoRepositoryPort repositoryPort) {
        return new RegistrarPontoUseCaseImpl(repositoryPort);
    }

    @Bean
    public ConsultarEspelhoPontoUseCase consultarEspelhoPontoUseCase(RegistroPontoRepositoryPort repositoryPort) {
        return new ConsultarEspelhoPontoUseCaseImpl(repositoryPort);
    }

    @Bean
    public ConsultarRelatorioEspelhoPontoUseCase consultarRelatorioEspelhoPontoUseCase(
            RegistroPontoRepositoryPort repositoryPort,
            ColaboradorRepositoryPort colaboradorRepository,
            CpcUsuarioRepositoryPort usuarioRepository,
            EmpresaRepositoryPort empresaRepository,
            ConfiguracaoJornadaRepositoryPort jornadaRepository) {
        return new ConsultarRelatorioEspelhoPontoUseCaseImpl(repositoryPort, colaboradorRepository,
                usuarioRepository, empresaRepository, jornadaRepository);
    }

    @Bean
    public AjustarPontoManualUseCase ajustarPontoManualUseCase(RegistroPontoRepositoryPort repositoryPort) {
        return new AjustarPontoManualUseCaseImpl(repositoryPort);
    }

    @Bean
    public SolicitarAjustePontoUseCase solicitarAjustePontoUseCase(RegistroPontoRepositoryPort repositoryPort) {
        return new SolicitarAjustePontoUseCaseImpl(repositoryPort);
    }

    @Bean
    public AprovarAjustePontoUseCase aprovarAjustePontoUseCase(RegistroPontoRepositoryPort repositoryPort) {
        return new AprovarAjustePontoUseCaseImpl(repositoryPort);
    }

    @Bean
    public RejeitarAjustePontoUseCase rejeitarAjustePontoUseCase(RegistroPontoRepositoryPort repositoryPort) {
        return new RejeitarAjustePontoUseCaseImpl(repositoryPort);
    }

    @Bean
    public ListarAjustesPendentesUseCase listarAjustesPendentesUseCase(RegistroPontoRepositoryPort repositoryPort) {
        return new ListarAjustesPendentesUseCaseImpl(repositoryPort);
    }
}
