package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CpcUsuarioJpaRepository extends JpaRepository<CpcUsuarioJpaEntity, UUID> {
    Optional<CpcUsuarioJpaEntity> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    List<CpcUsuarioJpaEntity> findByTenantId(UUID tenantId);
}
