package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.ContratoEvento;
import java.util.UUID;

public interface AdicionarEventoContratoUseCase {
    record Comando(UUID contratoId, String tipo, String descricao, UUID criadoPor) {}
    ContratoEvento executar(Comando comando);
}
