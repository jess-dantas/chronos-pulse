package br.com.jess.chronos.pulse.modules.estoque.repository;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {
    Optional<Material> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Material> findAllByTenantId(UUID tenantId);
    List<Material> findAllByTenantIdAndAtivoTrue(UUID tenantId);
    Page<Material> findAllByTenantId(UUID tenantId, Pageable pageable);
}
