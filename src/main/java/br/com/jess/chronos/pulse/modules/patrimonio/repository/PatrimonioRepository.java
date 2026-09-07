package br.com.jess.chronos.pulse.modules.patrimonio.repository;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatrimonioRepository extends JpaRepository<Patrimonio, UUID> {
    Optional<Patrimonio> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Patrimonio> findAllByTenantId(UUID tenantId);
    List<Patrimonio> findAllByTenantIdAndAtivoTrue(UUID tenantId);
    Page<Patrimonio> findAllByTenantId(UUID tenantId, Pageable pageable);
}
