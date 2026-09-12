package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoAditivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ContratoAditivoRepository extends JpaRepository<ContratoAditivo, UUID> {
    List<ContratoAditivo> findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(UUID contratoId, UUID tenantId);
}