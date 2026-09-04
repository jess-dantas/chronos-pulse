package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.ContratoEvento;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.ListarEventosContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;

import java.util.List;
import java.util.UUID;

public class ListarEventosContratoUseCaseImpl implements ListarEventosContratoUseCase {

    private final ContratoRepositoryPort repositoryPort;

    public ListarEventosContratoUseCaseImpl(ContratoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<ContratoEvento> executar(UUID contratoId) {
        return repositoryPort.listarEventosPorContrato(contratoId);
    }
}
