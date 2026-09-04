package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ContratoJpaRepository extends JpaRepository<ContratoJpaEntity, UUID> {
    List<ContratoJpaEntity> findByTenantId(UUID tenantId);
    List<ContratoJpaEntity> findByStatus(String status);
    long countByStatus(String status);
}
