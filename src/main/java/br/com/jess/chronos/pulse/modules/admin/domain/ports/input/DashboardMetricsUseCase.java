package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import java.util.Map;

public interface DashboardMetricsUseCase {
    Map<String, Object> executar();
}
