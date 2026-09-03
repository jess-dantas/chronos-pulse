package br.com.jess.chronos.pulse.modules.empresa.infrastructure.config;

import br.com.jess.chronos.pulse.modules.empresa.application.usecases.CadastrarEmpresaUseCaseImpl;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmpresaModuleConfig {

    @Bean
    public CadastrarEmpresaUseCase cadastrarEmpresaUseCase(EmpresaRepositoryPort repositoryPort) {
        return new CadastrarEmpresaUseCaseImpl(repositoryPort);
    }
}
