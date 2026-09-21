package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;

import java.util.List;
import java.util.UUID;

public interface ListarAjustesPendentesUseCase {

    record Comando(
            UUID tenantId,
            UUID gestorId
    ) {}

    List<RegistroPonto> executar(Comando comando);
}