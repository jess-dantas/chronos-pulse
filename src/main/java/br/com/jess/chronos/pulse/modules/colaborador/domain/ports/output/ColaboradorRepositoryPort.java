package br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output;

import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColaboradorRepositoryPort {
    Colaborador salvar(Colaborador colaborador);
    Optional<Colaborador> buscarPorId(UUID id);
    Optional<Colaborador> buscarPorCpcUsuarioId(UUID cpcUsuarioId);
    List<Colaborador> listarPorTenant(UUID tenantId);
    List<Colaborador> listarTodos();
}
