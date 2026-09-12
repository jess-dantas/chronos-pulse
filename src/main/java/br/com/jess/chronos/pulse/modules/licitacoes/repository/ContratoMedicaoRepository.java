package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoMedicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContratoMedicaoRepository extends JpaRepository<ContratoMedicao, UUID> {
    List<ContratoMedicao> findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(UUID contratoId, UUID tenantId);
}