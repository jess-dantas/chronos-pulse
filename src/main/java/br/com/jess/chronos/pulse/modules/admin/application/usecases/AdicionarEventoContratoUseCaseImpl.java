package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.ContratoEvento;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AdicionarEventoContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;

public class AdicionarEventoContratoUseCaseImpl implements AdicionarEventoContratoUseCase {

    private final ContratoRepositoryPort repositoryPort;

    public AdicionarEventoContratoUseCaseImpl(ContratoRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public ContratoEvento executar(Comando comando) {
        if (comando.contratoId() == null) {
            throw new IllegalArgumentException("Contrato é obrigatório.");
        }
        if (repositoryPort.buscarPorId(comando.contratoId()).isEmpty()) {
            throw new IllegalArgumentException("Contrato não encontrado: " + comando.contratoId());
        }
        if (comando.tipo() == null || comando.tipo().isBlank()) {
            throw new IllegalArgumentException("Tipo do evento é obrigatório.");
        }
        if (comando.descricao() == null || comando.descricao().isBlank()) {
            throw new IllegalArgumentException("Descrição do evento é obrigatória.");
        }

        ContratoEvento evento = new ContratoEvento(
                null,
                comando.contratoId(),
                comando.tipo(),
                comando.descricao(),
                comando.criadoPor()
        );

        return repositoryPort.salvarEvento(evento);
    }
}
