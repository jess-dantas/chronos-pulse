package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoApontamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContratoApontamentoRepository extends JpaRepository<ContratoApontamento, UUID> {
    List<ContratoApontamento> findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(UUID contratoId, UUID tenantId);

    Optional<ContratoApontamento> findByIdAndContratoIdAndTenantId(UUID id, UUID contratoId, UUID tenantId);
}