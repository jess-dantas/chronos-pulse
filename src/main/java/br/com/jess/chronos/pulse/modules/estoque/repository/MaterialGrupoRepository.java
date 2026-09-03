package br.com.jess.chronos.pulse.modules.estoque.repository;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.MaterialGrupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialGrupoRepository extends JpaRepository<MaterialGrupo, UUID> {
    Optional<MaterialGrupo> findByIdAndTenantId(UUID id, UUID tenantId);
    List<MaterialGrupo> findAllByTenantId(UUID tenantId);
    List<MaterialGrupo> findAllByTenantIdAndAtivoTrue(UUID tenantId);
}
