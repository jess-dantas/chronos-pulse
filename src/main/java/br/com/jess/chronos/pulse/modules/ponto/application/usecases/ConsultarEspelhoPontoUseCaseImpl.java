package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class ConsultarEspelhoPontoUseCaseImpl implements ConsultarEspelhoPontoUseCase {

    /** Janela do mês em fuso local (Brasil), igual aos demais use cases de ponto —
     *  em UTC, marcações das 21h–24h do último dia sairiam do mês. */
    private static final ZoneId FUSO_PONTO = ZoneId.of("America/Sao_Paulo");

    private final RegistroPontoRepositoryPort repositoryPort;

    public ConsultarEspelhoPontoUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public List<RegistroPonto> consultar(UUID colaboradorId, UUID tenantId, Integer mes, Integer ano) {
        if (colaboradorId == null || tenantId == null) {
            throw new IllegalArgumentException("ColaboradorId e TenantId são obrigatórios para consulta de espelho de ponto.");
        }

        if (mes != null && ano != null) {
            YearMonth ym = YearMonth.of(ano, mes);
            var inicio = ym.atDay(1).atStartOfDay(FUSO_PONTO).toInstant();
            var fim = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999).atZone(FUSO_PONTO).toInstant();
            return repositoryPort.listarPorColaboradorEPeriodo(colaboradorId, tenantId, inicio, fim);
        }

        return repositoryPort.listarPorColaborador(colaboradorId, tenantId);
    }
}
