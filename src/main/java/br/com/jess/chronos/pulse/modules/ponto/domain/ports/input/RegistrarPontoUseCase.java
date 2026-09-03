package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;

import java.util.UUID;

public interface RegistrarPontoUseCase {
    RegistroPonto executar(RegistroPonto registro, String cpfColaborador, UUID tenantId);
}
