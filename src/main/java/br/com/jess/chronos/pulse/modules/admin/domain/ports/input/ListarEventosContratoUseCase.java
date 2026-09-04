package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.ContratoEvento;
import java.util.List;
import java.util.UUID;

public interface ListarEventosContratoUseCase {
    List<ContratoEvento> executar(UUID contratoId);
}
