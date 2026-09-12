package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoSancao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContratoSancaoRepository extends JpaRepository<ContratoSancao, UUID> {
    List<ContratoSancao> findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(UUID contratoId, UUID tenantId);
}