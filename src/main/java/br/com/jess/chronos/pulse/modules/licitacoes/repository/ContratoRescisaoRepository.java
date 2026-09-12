package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoRescisao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContratoRescisaoRepository extends JpaRepository<ContratoRescisao, UUID> {
    Optional<ContratoRescisao> findByContratoIdAndTenantId(UUID contratoId, UUID tenantId);
}