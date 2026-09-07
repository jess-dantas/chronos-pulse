package br.com.jess.chronos.pulse.modules.empresa.application.usecases;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.AtualizarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;

public class AtualizarEmpresaUseCaseImpl implements AtualizarEmpresaUseCase {

    private final EmpresaRepositoryPort repositoryPort;

    public AtualizarEmpresaUseCaseImpl(EmpresaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Empresa executar(Comando comando) {
        return repositoryPort.atualizar(comando.id(), comando.nome(), comando.ativo());
    }
}
