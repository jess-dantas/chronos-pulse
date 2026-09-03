package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColaboradorJpaRepository extends JpaRepository<ColaboradorJpaEntity, UUID> {
    Optional<ColaboradorJpaEntity> findByCpcUsuarioId(UUID cpcUsuarioId);
    List<ColaboradorJpaEntity> findByTenantId(UUID tenantId);
}
