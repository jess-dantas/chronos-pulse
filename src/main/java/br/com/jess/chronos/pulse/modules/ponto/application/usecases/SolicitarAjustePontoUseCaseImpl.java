package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.SolicitarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.service.GeradorHashService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class SolicitarAjustePontoUseCaseImpl implements SolicitarAjustePontoUseCase {

    /** Regra: um dia com ajuste aprovado fica fechado para novas solicitações. */
    private static final ZoneId FUSO_PONTO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final RegistroPontoRepositoryPort repositoryPort;

    public SolicitarAjustePontoUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public RegistroPonto executar(Comando comando) {
        if (comando.justificativa() == null || comando.justificativa().trim().isEmpty()) {
            throw new IllegalArgumentException("A justificativa é obrigatória para solicitação de ajuste de ponto.");
        }
        if (comando.colaboradorId() == null || comando.tenantId() == null) {
            throw new IllegalArgumentException("ColaboradorId e TenantId são obrigatórios.");
        }
        if (comando.dataHora() == null || comando.tipoRegistro() == null) {
            throw new IllegalArgumentException("Data/hora e tipo de registro são obrigatórios.");
        }

        verificarDiaSemAjusteAprovado(comando);

        Long nsrLogico = repositoryPort.obterProximoNsrLogico(comando.colaboradorId(), comando.tenantId());
        Long nsr = repositoryPort.obterProximoNsr();

        RegistroPonto registro = new RegistroPonto(
                UUID.randomUUID(),
                comando.colaboradorId(),
                comando.tenantId(),
                comando.dataHora(),
                Instant.now(),
                comando.tipoRegistro(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                false,
                nsr,
                nsrLogico,
                true,
                comando.justificativa().trim(),
                comando.observacao() != null ? comando.observacao().trim() : null,
                AjusteStatus.PENDENTE,
                null,
                null,
                null
        );

        String hash = GeradorHashService.gerarHashRegistro(registro, comando.cpf());
        registro.atribuirHash(hash);

        return repositoryPort.salvar(registro);
    }

    /**
     * Bloqueia nova solicitação no mesmo dia (fuso America/Sao_Paulo) de um ajuste
     * já aprovado: o dia fica fechado para alterações.
     */
    private void verificarDiaSemAjusteAprovado(Comando comando) {
        LocalDate dia = comando.dataHora().atZone(FUSO_PONTO).toLocalDate();
        Instant inicio = dia.atStartOfDay(FUSO_PONTO).toInstant();
        Instant fim = LocalDateTime.of(dia, java.time.LocalTime.MAX).atZone(FUSO_PONTO).toInstant();

        boolean jaAprovado = repositoryPort
                .listarPorColaboradorEPeriodo(comando.colaboradorId(), comando.tenantId(), inicio, fim)
                .stream()
                .anyMatch(r -> r.getAjusteStatus() == AjusteStatus.APROVADO);

        if (jaAprovado) {
            throw new IllegalArgumentException(
                    "O dia " + dia.format(FORMATO_DIA)
                            + " já possui ajuste aprovado; novas solicitações de ajuste estão bloqueadas para este dia.");
        }
    }
}