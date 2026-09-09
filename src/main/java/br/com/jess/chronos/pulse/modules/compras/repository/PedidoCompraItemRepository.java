package br.com.jess.chronos.pulse.modules.compras.repository;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraItem;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PrecoMaterialProjecao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoCompraItemRepository extends JpaRepository<PedidoCompraItem, UUID> {

    List<PedidoCompraItem> findAllByTenantId(UUID tenantId);

    @Query("""
            SELECT new br.com.jess.chronos.pulse.modules.compras.web.dto.PrecoMaterialProjecao(
                    i.materialId, p.dataEmissao, i.valorUnitario)
            FROM PedidoCompraItem i
            JOIN i.pedido p
            WHERE p.tenantId = :tenantId
              AND p.status <> br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraStatus.CANCELADO
            ORDER BY p.dataEmissao DESC, p.criadoEm DESC
            """)
    List<PrecoMaterialProjecao> projecaoPrecosPorMaterial(@Param("tenantId") UUID tenantId);
}