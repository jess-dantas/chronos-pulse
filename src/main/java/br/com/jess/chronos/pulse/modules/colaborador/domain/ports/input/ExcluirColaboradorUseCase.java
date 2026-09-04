package br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input;

import java.util.UUID;

public interface ExcluirColaboradorUseCase {
    void executar(UUID colaboradorId);
}
