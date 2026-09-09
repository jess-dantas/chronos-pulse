package br.com.jess.chronos.pulse.modules.patrimonio.repository;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.InventarioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventarioItemRepository extends JpaRepository<InventarioItem, UUID> {
    Optional<InventarioItem> findByInventarioIdAndPatrimonioId(UUID inventarioId, UUID patrimonioId);
}