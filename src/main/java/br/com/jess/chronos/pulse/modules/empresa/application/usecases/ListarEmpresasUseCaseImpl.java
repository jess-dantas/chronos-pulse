package br.com.jess.chronos.pulse.modules.empresa.application.usecases;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.ListarEmpresasUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;

import java.util.List;

public class ListarEmpresasUseCaseImpl implements ListarEmpresasUseCase {

    private final EmpresaRepositoryPort repositoryPort;

    public ListarEmpresasUseCaseImpl(EmpresaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<Empresa> executar() {
        return repositoryPort.listarTodos();
    }
}
