package br.com.jess.chronos.pulse.modules.empresa.domain.ports.output;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.ConfiguracaoJornada;

import java.util.Optional;
import java.util.UUID;

public interface ConfiguracaoJornadaRepositoryPort {

    Optional<ConfiguracaoJornada> buscarPorId(UUID id);

    Optional<ConfiguracaoJornada> buscarPorIdETenant(UUID id, UUID tenantId);
}