package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.service.GeradorHashService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class RegistrarPontoUseCaseImpl implements RegistrarPontoUseCase {

    private static final ZoneId FUSO_PONTO = ZoneId.of("America/Sao_Paulo");

    private static final TipoRegistro[] SEQUENCIA = {
        TipoRegistro.ENTRADA, TipoRegistro.INTERVALO, TipoRegistro.RETORNO, TipoRegistro.SAIDA
    };

    private final RegistroPontoRepositoryPort repositoryPort;

    public RegistrarPontoUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public RegistroPonto executar(RegistroPonto registro, String cpfColaborador, UUID tenantId) {
        TipoRegistro proximoTipo = determinarProximoTipo(registro, tenantId);
        registro.atribuirTipo(proximoTipo);

        Long nsrLogico = repositoryPort.obterProximoNsrLogico(registro.getColaboradorId(), tenantId);
        registro.atribuirNsrLogico(nsrLogico);

        Long nsr = repositoryPort.obterProximoNsr();
        registro.atribuirNsr(nsr);

        String hash = GeradorHashService.gerarHashRegistro(registro, cpfColaborador);
        registro.atribuirHash(hash);

        return repositoryPort.salvar(registro);
    }

    private TipoRegistro determinarProximoTipo(RegistroPonto registro, UUID tenantId) {
        Instant dataHora = registro.getDataHoraDispositivo();
        LocalDate dia = dataHora.atZone(FUSO_PONTO).toLocalDate();
        Instant inicio = dia.atStartOfDay(FUSO_PONTO).toInstant();
        Instant fim = dia.plusDays(1).atStartOfDay(FUSO_PONTO).toInstant();

        return repositoryPort
                .buscarUltimoTipoPorColaborador(registro.getColaboradorId(), tenantId, inicio, fim)
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
