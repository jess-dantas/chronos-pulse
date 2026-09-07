package br.com.jess.chronos.pulse.modules.modulo.repository;

import br.com.jess.chronos.pulse.modules.modulo.domain.entity.EmpresaModulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmpresaModuloRepository extends JpaRepository<EmpresaModulo, UUID> {

    List<EmpresaModulo> findByTenantId(UUID tenantId);

    void deleteByTenantId(UUID tenantId);

    boolean existsByTenantIdAndModulo_Id(UUID tenantId, UUID moduloId);

    @Query("select m.codigo from EmpresaModulo em join em.modulo m " +
            "where em.tenantId = :tenantId and m.ativo = true order by m.codigo")
    List<String> findCodigosAtivos(@Param("tenantId") UUID tenantId);

    @Query("select case when count(em) > 0 then true else false end " +
            "from EmpresaModulo em join em.modulo m " +
            "where em.tenantId = :tenantId and m.codigo = :codigo and m.ativo = true")
    boolean isTenantModuloAtivo(@Param("tenantId") UUID tenantId, @Param("codigo") String codigo);
}