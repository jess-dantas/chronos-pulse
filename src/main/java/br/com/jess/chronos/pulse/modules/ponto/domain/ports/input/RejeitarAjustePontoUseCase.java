package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;

import java.util.UUID;

public interface RejeitarAjustePontoUseCase {

    record Comando(
            UUID registroId,
            UUID aprovadorId,
            UUID tenantId,
            String motivoRejeicao
    ) {}

    RegistroPonto executar(Comando comando);
}