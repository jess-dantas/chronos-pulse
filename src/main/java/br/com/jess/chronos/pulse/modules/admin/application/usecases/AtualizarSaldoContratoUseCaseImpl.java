package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AtualizarSaldoContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;

import java.math.BigDecimal;
import java.util.Optional;

public class AtualizarSaldoContratoUseCaseImpl implements AtualizarSaldoContratoUseCase {

    private final ContratoRepositoryPort contratoRepositoryPort;

    public AtualizarSaldoContratoUseCaseImpl(ContratoRepositoryPort contratoRepositoryPort) {
        this.contratoRepositoryPort = contratoRepositoryPort;
    }

    @Override
    public Optional<Contrato> executar(Comando comando) {
        var contrato = contratoRepositoryPort.buscarPorId(comando.contratoId());
        if (contrato.isEmpty()) {
            return Optional.empty();
        }
        if (comando.valorLiquidado() != null
                && comando.valorEmpenhado() != null
                && comando.valorLiquidado().compareTo(comando.valorEmpenhado()) > 0) {
            throw new IllegalArgumentException("Valor liquidado não pode superar o valor empenhado.");
        }
        var atualizado = contrato.get();
        atualizado.atualizarSaldo(
                comando.valorEmpenhado(),
                comando.valorLiquidado(),
                comando.empenhoNumero(),
                comando.vencimentoAvisoDias());
        return Optional.of(contratoRepositoryPort.salvar(atualizado));
    }
}