package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public class ConsultarEspelhoPontoUseCaseImpl implements ConsultarEspelhoPontoUseCase {

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
            var inicio = ym.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            var fim = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999).toInstant(ZoneOffset.UTC);
            return repositoryPort.listarPorColaboradorEPeriodo(colaboradorId, tenantId, inicio, fim);
        }

        return repositoryPort.listarPorColaborador(colaboradorId, tenantId);
    }
}
