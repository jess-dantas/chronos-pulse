package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RejeitarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;

import java.util.UUID;

public class RejeitarAjustePontoUseCaseImpl implements RejeitarAjustePontoUseCase {

    private final RegistroPontoRepositoryPort repositoryPort;

    public RejeitarAjustePontoUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public RegistroPonto executar(Comando comando) {
        if (comando.motivoRejeicao() == null || comando.motivoRejeicao().trim().isEmpty()) {
            throw new IllegalArgumentException("O motivo da rejeição é obrigatório.");
        }

        RegistroPonto registro = repositoryPort.buscarPorId(comando.registroId())
                .orElseThrow(() -> new IllegalArgumentException("Registro de ponto não encontrado: " + comando.registroId()));

        if (!comando.tenantId().equals(registro.getTenantId())) {
            throw new SecurityException("Acesso negado: registro não pertence ao tenant.");
        }

        if (registro.getAjusteStatus() != AjusteStatus.PENDENTE) {
            throw new IllegalStateException("Apenas ajustes com status PENDENTE podem ser rejeitados. Status atual: " + registro.getAjusteStatus());
        }

        if (!Boolean.TRUE.equals(registro.getAjusteManual())) {
            throw new IllegalStateException("Apenas ajustes manuais podem ser rejeitados.");
        }

        // Atribui status rejeitado
        registro.atribuirAjusteStatus(AjusteStatus.REJEITADO, comando.motivoRejeicao().trim(), comando.aprovadorId());

        return repositoryPort.salvar(registro);
    }
}