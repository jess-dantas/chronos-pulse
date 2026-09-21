package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;

import java.util.UUID;

public interface AprovarAjustePontoUseCase {

    record Comando(
            UUID registroId,
            UUID aprovadorId,
            UUID tenantId
    ) {}

    RegistroPonto executar(Comando comando);
}