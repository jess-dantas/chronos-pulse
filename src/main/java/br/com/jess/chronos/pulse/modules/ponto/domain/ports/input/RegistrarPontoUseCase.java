package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;

public interface RegistrarPontoUseCase {
    RegistroPonto executar(RegistroPonto registro, String cpfColaborador);
}
