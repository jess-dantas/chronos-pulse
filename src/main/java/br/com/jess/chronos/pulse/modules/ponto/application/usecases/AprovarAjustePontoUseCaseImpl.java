package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.AprovarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.service.GeradorHashService;

import java.time.Instant;
import java.util.UUID;

public class AprovarAjustePontoUseCaseImpl implements AprovarAjustePontoUseCase {

    private final RegistroPontoRepositoryPort repositoryPort;

    public AprovarAjustePontoUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public RegistroPonto executar(Comando comando) {
        RegistroPonto registro = repositoryPort.buscarPorId(comando.registroId())
                .orElseThrow(() -> new IllegalArgumentException("Registro de ponto não encontrado: " + comando.registroId()));

        if (!comando.tenantId().equals(registro.getTenantId())) {
            throw new SecurityException("Acesso negado: registro não pertence ao tenant.");
        }

        if (registro.getAjusteStatus() != AjusteStatus.PENDENTE) {
            throw new IllegalStateException("Apenas ajustes com status PENDENTE podem ser aprovados. Status atual: " + registro.getAjusteStatus());
        }

        if (!Boolean.TRUE.equals(registro.getAjusteManual())) {
            throw new IllegalStateException("Apenas ajustes manuais podem ser aprovados.");
        }

        // Atribui status aprovado
        registro.atribuirAjusteStatus(AjusteStatus.APROVADO, null, comando.aprovadorId());

        // Regenera hash com dados atualizados
        String hash = GeradorHashService.gerarHashRegistro(registro, ""); // CPF não necessário para hash de ajuste aprovado
        registro.atribuirHash(hash);

        return repositoryPort.salvar(registro);
    }
}