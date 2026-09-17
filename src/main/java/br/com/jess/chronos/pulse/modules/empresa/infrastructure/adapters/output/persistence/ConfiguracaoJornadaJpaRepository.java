package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConfiguracaoJornadaJpaRepository extends JpaRepository<ConfiguracaoJornadaJpaEntity, UUID> {

    Optional<ConfiguracaoJornadaJpaEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}