package br.com.jess.chronos.pulse.modules.compras.repository;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RequisicaoCompraRepository extends JpaRepository<RequisicaoCompra, UUID> {

    Optional<RequisicaoCompra> findByIdAndTenantId(UUID id, UUID tenantId);

    List<RequisicaoCompra> findAllByTenantIdOrderByDataRequisicaoDesc(UUID tenantId);

    long countByTenantId(UUID tenantId);

    List<RequisicaoCompra> findAllByTenantIdAndStatus(UUID tenantId, String status);
}