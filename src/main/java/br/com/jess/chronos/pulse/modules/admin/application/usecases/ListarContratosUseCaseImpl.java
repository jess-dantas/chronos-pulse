package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.ListarContratosUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;

import java.util.List;
import java.util.UUID;

public class ListarContratosUseCaseImpl implements ListarContratosUseCase {

    private final ContratoRepositoryPort repositoryPort;

    public ListarContratosUseCaseImpl(ContratoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<Contrato> executar(UUID tenantId) {
        if (tenantId != null) {
            return repositoryPort.listarPorTenant(tenantId);
        }
        return repositoryPort.listarTodos();
    }
}
