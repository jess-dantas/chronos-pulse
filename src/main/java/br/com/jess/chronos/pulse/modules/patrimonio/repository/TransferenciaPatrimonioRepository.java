package br.com.jess.chronos.pulse.modules.patrimonio.repository;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.TransferenciaPatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransferenciaPatrimonioRepository extends JpaRepository<TransferenciaPatrimonio, UUID> {
    Optional<TransferenciaPatrimonio> findByIdAndTenantId(UUID id, UUID tenantId);
    List<TransferenciaPatrimonio> findAllByTenantIdOrderByDataSolicitacaoDesc(UUID tenantId);
}