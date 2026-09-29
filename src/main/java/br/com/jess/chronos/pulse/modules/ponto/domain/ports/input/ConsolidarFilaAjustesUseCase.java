package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.FilaAjusteItem;

import java.util.List;
import java.util.UUID;

public interface ConsolidarFilaAjustesUseCase {

    record Comando(
            UUID tenantId,
            UUID gestorId
    ) {}

    List<FilaAjusteItem> executar(Comando comando);
}
