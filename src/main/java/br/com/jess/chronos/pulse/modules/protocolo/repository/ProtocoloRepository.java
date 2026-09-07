package br.com.jess.chronos.pulse.modules.protocolo.repository;

import br.com.jess.chronos.pulse.modules.protocolo.domain.entity.Protocolo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProtocoloRepository extends JpaRepository<Protocolo, UUID> {
    Optional<Protocolo> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Protocolo> findAllByTenantId(UUID tenantId);
    List<Protocolo> findAllByTenantIdAndAtivoTrue(UUID tenantId);
    Page<Protocolo> findAllByTenantId(UUID tenantId, Pageable pageable);
    boolean existsByNumeroProtocoloAndTenantId(String numeroProtocolo, UUID tenantId);
}
