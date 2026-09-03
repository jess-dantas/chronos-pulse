package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.service.GeradorHashService;

import java.util.UUID;

public class RegistrarPontoUseCaseImpl implements RegistrarPontoUseCase {

    private static final TipoRegistro[] SEQUENCIA = {
        TipoRegistro.ENTRADA, TipoRegistro.INTERVALO, TipoRegistro.RETORNO, TipoRegistro.SAIDA
    };

    private final RegistroPontoRepositoryPort repositoryPort;

    public RegistrarPontoUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public RegistroPonto executar(RegistroPonto registro, String cpfColaborador, UUID tenantId) {
        TipoRegistro proximoTipo = determinarProximoTipo(registro.getColaboradorId(), tenantId);
        registro.atribuirTipo(proximoTipo);

        Long nsr = repositoryPort.obterProximoNsr();
        registro.atribuirNsr(nsr);

        String hash = GeradorHashService.gerarHashRegistro(registro, cpfColaborador);
        registro.atribuirHash(hash);

        return repositoryPort.salvar(registro);
    }

    private TipoRegistro determinarProximoTipo(UUID colaboradorId, UUID tenantId) {
        return repositoryPort.buscarUltimoTipoPorColaborador(colaboradorId, tenantId)
                .map(ultimo -> SEQUENCIA[(indexOf(ultimo) + 1) % SEQUENCIA.length])
                .orElse(TipoRegistro.ENTRADA);
    }

    private int indexOf(TipoRegistro tipo) {
        for (int i = 0; i < SEQUENCIA.length; i++) {
            if (SEQUENCIA[i] == tipo) return i;
        }
        return SEQUENCIA.length - 1;
    }
}
