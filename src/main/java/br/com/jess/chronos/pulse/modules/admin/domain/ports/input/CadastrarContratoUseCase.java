package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CadastrarContratoUseCase {
    record Comando(
        UUID tenantId,
        String numero,
        String objeto,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorMensal,
        BigDecimal valorTotal,
        String observacoes
    ) {}
    Contrato executar(Comando comando);
}
