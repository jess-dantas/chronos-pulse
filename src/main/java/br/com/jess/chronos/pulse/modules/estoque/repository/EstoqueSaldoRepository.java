package br.com.jess.chronos.pulse.modules.estoque.repository;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.EstoqueSaldo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstoqueSaldoRepository extends JpaRepository<EstoqueSaldo, UUID> {

    Optional<EstoqueSaldo> findByTenantIdAndAlmoxarifadoIdAndMaterialIdAndLote(
            UUID tenantId, UUID almoxarifadoId, UUID materialId, String lote);

    @Query("""
        SELECT s FROM EstoqueSaldo s
        JOIN s.material m
        WHERE s.tenantId = :tenantId
          AND (:almoxarifadoId IS NULL OR s.almoxarifado.id = :almoxarifadoId)
          AND (:grupoId IS NULL OR m.grupo.id = :grupoId)
          AND (:abaixoMinimo IS NULL OR (:abaixoMinimo = TRUE AND s.quantidadeAtual <= m.estoqueMinimo))
    """)
    Page<EstoqueSaldo> consultarSaldosComFiltro(
            @Param("tenantId") UUID tenantId,
            @Param("almoxarifadoId") UUID almoxarifadoId,
            @Param("grupoId") UUID grupoId,
            @Param("abaixoMinimo") Boolean abaixoMinimo,
            Pageable pageable
    );
}