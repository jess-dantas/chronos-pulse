package br.com.jess.chronos.pulse.modules.lead.application.usecases;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.ListarLeadsEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.output.LeadEmpresaRepositoryPort;

import java.util.List;

public class ListarLeadsEmpresaUseCaseImpl implements ListarLeadsEmpresaUseCase {

    private final LeadEmpresaRepositoryPort repositoryPort;

    public ListarLeadsEmpresaUseCaseImpl(LeadEmpresaRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<LeadEmpresa> executar() {
        return repositoryPort.listarTodos();
    }
}