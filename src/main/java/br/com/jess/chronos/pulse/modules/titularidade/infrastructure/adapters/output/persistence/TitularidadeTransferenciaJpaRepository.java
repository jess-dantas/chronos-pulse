package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeTransferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TitularidadeTransferenciaJpaRepository
        extends JpaRepository<TitularidadeTransferencia, UUID> {

    @Modifying
    @Query("update TitularidadeTransferencia t set t.status = 'CANCELADA' "
            + "where t.tenantId = :tenantId and t.solicitanteId = :solicitanteId "
            + "and t.status = 'EM_ANDAMENTO'")
    void cancelarAbertas(@Param("tenantId") UUID tenantId,
                         @Param("solicitanteId") UUID solicitanteId);
}
