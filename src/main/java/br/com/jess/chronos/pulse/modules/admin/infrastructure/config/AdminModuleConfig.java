package br.com.jess.chronos.pulse.modules.admin.infrastructure.config;

import br.com.jess.chronos.pulse.modules.admin.application.usecases.*;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.*;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminModuleConfig {

    @Bean
    public CadastrarContratoUseCase cadastrarContratoUseCase(
            ContratoRepositoryPort contratoRepositoryPort,
            EmpresaRepositoryPort empresaRepositoryPort) {
        return new CadastrarContratoUseCaseImpl(contratoRepositoryPort, empresaRepositoryPort);
    }

    @Bean
    public ListarContratosUseCase listarContratosUseCase(ContratoRepositoryPort repositoryPort) {
        return new ListarContratosUseCaseImpl(repositoryPort);
    }

    @Bean
    public AdicionarEventoContratoUseCase adicionarEventoContratoUseCase(ContratoRepositoryPort repositoryPort) {
        return new AdicionarEventoContratoUseCaseImpl(repositoryPort);
    }

    @Bean
    public ListarEventosContratoUseCase listarEventosContratoUseCase(ContratoRepositoryPort repositoryPort) {
        return new ListarEventosContratoUseCaseImpl(repositoryPort);
    }

    @Bean
    public DashboardMetricsUseCase dashboardMetricsUseCase(ContratoRepositoryPort repositoryPort) {
        return new DashboardMetricsUseCaseImpl(repositoryPort);
    }
}
