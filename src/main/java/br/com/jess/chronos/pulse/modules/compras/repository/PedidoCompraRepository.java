package br.com.jess.chronos.pulse.modules.compras.repository;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, UUID> {
    Optional<PedidoCompra> findByIdAndTenantId(UUID id, UUID tenantId);
    List<PedidoCompra> findAllByTenantIdOrderByDataEmissaoDesc(UUID tenantId);
}