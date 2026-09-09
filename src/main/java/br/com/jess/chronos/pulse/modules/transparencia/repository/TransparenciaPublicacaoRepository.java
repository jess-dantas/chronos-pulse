package br.com.jess.chronos.pulse.modules.transparencia.repository;

import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.StatusPublicacaoTransparencia;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.TransparenciaPublicacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransparenciaPublicacaoRepository extends JpaRepository<TransparenciaPublicacao, UUID> {

    List<TransparenciaPublicacao> findAllByTenantIdOrderByCompetenciaDesc(UUID tenantId);

    Optional<TransparenciaPublicacao> findByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, StatusPublicacaoTransparencia status);
}