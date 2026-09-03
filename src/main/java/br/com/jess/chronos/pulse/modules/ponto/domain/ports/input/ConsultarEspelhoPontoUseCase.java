package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import java.util.List;
import java.util.UUID;

public interface ConsultarEspelhoPontoUseCase {
    List<RegistroPonto> consultar(UUID colaboradorId, UUID tenantId, Integer mes, Integer ano);
}
