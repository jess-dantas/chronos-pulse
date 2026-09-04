package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ExcluirColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;

import java.util.UUID;

public class ExcluirColaboradorUseCaseImpl implements ExcluirColaboradorUseCase {

    private final ColaboradorRepositoryPort colaboradorRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;

    public ExcluirColaboradorUseCaseImpl(ColaboradorRepositoryPort colaboradorRepository,
                                         CpcUsuarioRepositoryPort usuarioRepository) {
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void executar(UUID colaboradorId) {
        Colaborador colaborador = colaboradorRepository.buscarPorId(colaboradorId)
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado"));

        colaboradorRepository.desativarPorId(colaboradorId);
        usuarioRepository.desativarPorId(colaborador.getCpcUsuarioId());
    }
}
