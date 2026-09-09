package br.com.jess.chronos.pulse.modules.compras.repository;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CotacaoRepository extends JpaRepository<CotacaoCompra, UUID> {
    Optional<CotacaoCompra> findByIdAndTenantId(UUID id, UUID tenantId);
    List<CotacaoCompra> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
    Optional<CotacaoCompra> findByRequisicaoIdAndTenantId(UUID requisicaoId, UUID tenantId);
}