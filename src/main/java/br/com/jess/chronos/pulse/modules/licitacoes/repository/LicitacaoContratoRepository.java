package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoLicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicitacaoContratoRepository extends JpaRepository<ContratoLicitacao, UUID> {
    Optional<ContratoLicitacao> findByIdAndTenantId(UUID id, UUID tenantId);

    List<ContratoLicitacao> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);

    Optional<ContratoLicitacao> findByLicitacaoIdAndTenantId(UUID licitacaoId, UUID tenantId);
}