package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import java.util.List;
import java.util.UUID;

public interface ListarContratosUseCase {
    List<Contrato> executar(UUID tenantId);
}
