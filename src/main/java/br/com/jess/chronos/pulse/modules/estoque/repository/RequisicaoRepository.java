package br.com.jess.chronos.pulse.modules.estoque.repository;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Requisicao;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.RequisicaoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequisicaoRepository extends JpaRepository<Requisicao, UUID> {
    Optional<Requisicao> findByIdAndTenantId(UUID id, UUID tenantId);
    Page<Requisicao> findAllByTenantIdOrderByDataSolicitacaoDesc(UUID tenantId, Pageable pageable);
    Page<Requisicao> findAllByTenantIdAndStatusOrderByDataSolicitacaoDesc(UUID tenantId, RequisicaoStatus status, Pageable pageable);
}
