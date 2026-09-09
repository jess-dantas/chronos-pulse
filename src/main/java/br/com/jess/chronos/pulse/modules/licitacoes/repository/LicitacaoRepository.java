package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicitacaoRepository extends JpaRepository<Licitacao, UUID> {

    Optional<Licitacao> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Licitacao> findAllByTenantIdOrderByCriadoEmDesc(UUID tenantId);
}