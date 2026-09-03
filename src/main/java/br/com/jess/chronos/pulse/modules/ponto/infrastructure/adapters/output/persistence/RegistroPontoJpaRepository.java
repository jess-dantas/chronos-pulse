package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroPontoJpaRepository extends JpaRepository<RegistroPontoJpaEntity, UUID> {

    @Query("SELECT COALESCE(MAX(r.nsr), 0) + 1 FROM RegistroPontoJpaEntity r")
    Long obterProximoNsr();

    @Query("SELECT r.tipoRegistro FROM RegistroPontoJpaEntity r " +
           "WHERE r.colaboradorId = :colaboradorId AND r.tenantId = :tenantId " +
           "ORDER BY r.dataHoraServidor DESC LIMIT 1")
    Optional<TipoRegistro> buscarUltimoTipoPorColaborador(
            @Param("colaboradorId") UUID colaboradorId,
            @Param("tenantId") UUID tenantId);

    List<RegistroPontoJpaEntity> findByColaboradorIdAndTenantIdOrderByDataHoraDispositivoAsc(
            UUID colaboradorId, UUID tenantId);

    List<RegistroPontoJpaEntity> findByColaboradorIdAndTenantIdAndDataHoraDispositivoBetweenOrderByDataHoraDispositivoAsc(
            UUID colaboradorId, UUID tenantId, Instant inicio, Instant fim);

    List<RegistroPontoJpaEntity> findByTenantIdOrderByDataHoraDispositivoAsc(UUID tenantId);
}
