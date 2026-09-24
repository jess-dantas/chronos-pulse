package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.SolicitarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.service.GeradorHashService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SolicitarAjustePontoUseCaseImpl implements SolicitarAjustePontoUseCase {

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
}