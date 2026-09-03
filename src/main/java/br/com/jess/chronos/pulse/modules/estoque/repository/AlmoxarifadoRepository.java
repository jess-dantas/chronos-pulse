package br.com.jess.chronos.pulse.modules.estoque.repository;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Almoxarifado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlmoxarifadoRepository extends JpaRepository<Almoxarifado, UUID> {
    Optional<Almoxarifado> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Almoxarifado> findAllByTenantId(UUID tenantId);
    List<Almoxarifado> findAllByTenantIdAndAtivoTrue(UUID tenantId);
}
