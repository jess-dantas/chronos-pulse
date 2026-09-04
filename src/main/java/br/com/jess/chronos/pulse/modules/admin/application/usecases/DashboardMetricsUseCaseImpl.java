package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.DashboardMetricsUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;

import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardMetricsUseCaseImpl implements DashboardMetricsUseCase {

    private final ContratoRepositoryPort repositoryPort;

    public DashboardMetricsUseCaseImpl(ContratoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Map<String, Object> executar() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("empresasAtivas", repositoryPort.contarTenantsAtivos());
        metrics.put("totalColaboradores", repositoryPort.contarTotalColaboradores());
        metrics.put("contratosAtivos", repositoryPort.contarAtivos());
        return metrics;
    }
}
