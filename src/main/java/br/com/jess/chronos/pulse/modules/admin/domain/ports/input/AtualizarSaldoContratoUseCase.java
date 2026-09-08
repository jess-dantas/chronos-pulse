package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AtualizarSaldoContratoUseCase {
    record Comando(
            UUID contratoId,
            BigDecimal valorEmpenhado,
            BigDecimal valorLiquidado,
            String empenhoNumero,
            Integer vencimentoAvisoDias
    ) {}
    Optional<Contrato> executar(Comando comando);
}