package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.service.GeradorHashService;

public class RegistrarPontoUseCaseImpl implements RegistrarPontoUseCase {

    private final RegistroPontoRepositoryPort repositoryPort;

    public RegistrarPontoUseCaseImpl(RegistroPontoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public RegistroPonto executar(RegistroPonto registro, String cpfColaborador) {
        // 1. Obtém o próximo Número Sequencial de Registro (NSR) exigido por lei
        Long proximoNsr = repositoryPort.obterProximoNsr();

        // 2. Gera o Hash SHA-256 de integridade da batida
        String hash = GeradorHashService.gerarHashRegistro(registro, cpfColaborador);
        registro.atribuirHash(hash);

        // 3. Persiste o registro via porta de saída
        return repositoryPort.salvar(registro);
    }
}