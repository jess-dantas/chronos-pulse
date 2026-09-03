package br.com.jess.chronos.pulse.modules.ponto.infrastructure.config;

import br.com.jess.chronos.pulse.modules.ponto.application.usecases.AjustarPontoManualUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.ConsultarEspelhoPontoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.application.usecases.RegistrarPontoUseCaseImpl;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.AjustarPontoManualUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
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
    public AjustarPontoManualUseCase ajustarPontoManualUseCase(RegistroPontoRepositoryPort repositoryPort) {
        return new AjustarPontoManualUseCaseImpl(repositoryPort);
    }
}
