package br.com.jess.chronos.pulse.modules.lead.infrastructure.config;

import br.com.jess.chronos.pulse.modules.lead.application.usecases.CriarLeadEmpresaUseCaseImpl;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.CriarLeadEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.output.LeadEmpresaRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeadModuleConfig {

    @Bean
    public CriarLeadEmpresaUseCase criarLeadEmpresaUseCase(LeadEmpresaRepositoryPort repositoryPort) {
        return new CriarLeadEmpresaUseCaseImpl(repositoryPort);
    }
}