package br.com.jess.chronos.pulse.modules.empresa.infrastructure.config;

import br.com.jess.chronos.pulse.modules.empresa.application.usecases.AtualizarEmpresaUseCaseImpl;
import br.com.jess.chronos.pulse.modules.empresa.application.usecases.CadastrarEmpresaUseCaseImpl;
import br.com.jess.chronos.pulse.modules.empresa.application.usecases.ListarEmpresasUseCaseImpl;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.AtualizarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.ListarEmpresasUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmpresaModuleConfig {

    @Bean
    public CadastrarEmpresaUseCase cadastrarEmpresaUseCase(
            EmpresaRepositoryPort repositoryPort,
            ModulosPort modulosPort) {
        return new CadastrarEmpresaUseCaseImpl(repositoryPort, modulosPort);
    }

    @Bean
    public ListarEmpresasUseCase listarEmpresasUseCase(EmpresaRepositoryPort repositoryPort) {
        return new ListarEmpresasUseCaseImpl(repositoryPort);
    }

    @Bean
    public AtualizarEmpresaUseCase atualizarEmpresaUseCase(EmpresaRepositoryPort repositoryPort) {
        return new AtualizarEmpresaUseCaseImpl(repositoryPort);
    }
}
