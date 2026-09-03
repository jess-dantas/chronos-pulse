package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;

import java.time.Instant;
import java.util.UUID;

public interface AjustarPontoManualUseCase {

    record Comando(
            UUID colaboradorId,
            UUID tenantId,
            String cpf,
            Instant dataHora,
            TipoRegistro tipoRegistro,
            String justificativa,
            String observacao
    ) {}

    RegistroPonto executar(Comando comando);
}
