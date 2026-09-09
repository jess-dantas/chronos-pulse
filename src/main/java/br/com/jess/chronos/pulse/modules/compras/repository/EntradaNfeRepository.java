package br.com.jess.chronos.pulse.modules.compras.repository;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.EntradaNfe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EntradaNfeRepository extends JpaRepository<EntradaNfe, UUID> {
    Optional<EntradaNfe> findByTenantIdAndChaveNfe(UUID tenantId, String chaveNfe);
    List<EntradaNfe> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
}