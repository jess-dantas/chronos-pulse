package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ListarAjustesPendentesUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;

import java.util.List;
import java.util.UUID;

public class ListarAjustesPendentesUseCaseImpl implements ListarAjustesPendentesUseCase {

    private final RegistroPontoRepositoryPort repositoryPort;

    public ListarAjustesPendentesUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<RegistroPonto> executar(Comando comando) {
        return repositoryPort.listarAjustesPendentesPorTenant(comando.tenantId());
    }
}