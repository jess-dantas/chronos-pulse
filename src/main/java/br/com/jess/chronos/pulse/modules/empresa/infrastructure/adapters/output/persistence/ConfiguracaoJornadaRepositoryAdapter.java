package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.ConfiguracaoJornada;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.ConfiguracaoJornadaRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ConfiguracaoJornadaRepositoryAdapter implements ConfiguracaoJornadaRepositoryPort {

    private final ConfiguracaoJornadaJpaRepository jpaRepository;
    private final ConfiguracaoJornadaMapper mapper;

    public ConfiguracaoJornadaRepositoryAdapter(ConfiguracaoJornadaJpaRepository jpaRepository,
                                                ConfiguracaoJornadaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<ConfiguracaoJornada> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Optional<ConfiguracaoJornada> buscarPorIdETenant(UUID id, UUID tenantId) {
        return jpaRepository.findByIdAndTenantId(id, tenantId).map(mapper::toModel);
    }
}