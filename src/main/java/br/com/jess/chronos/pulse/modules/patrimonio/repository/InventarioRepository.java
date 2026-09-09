package br.com.jess.chronos.pulse.modules.patrimonio.repository;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, UUID> {
    Optional<Inventario> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Inventario> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
}