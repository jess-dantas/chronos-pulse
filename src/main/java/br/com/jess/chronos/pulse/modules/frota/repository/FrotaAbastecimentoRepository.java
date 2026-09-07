package br.com.jess.chronos.pulse.modules.frota.repository;

import br.com.jess.chronos.pulse.modules.frota.domain.entity.FrotaAbastecimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FrotaAbastecimentoRepository extends JpaRepository<FrotaAbastecimento, UUID> {
    List<FrotaAbastecimento> findAllByTenantIdOrderByDataHoraDesc(UUID tenantId);
    Page<FrotaAbastecimento> findAllByTenantId(UUID tenantId, Pageable pageable);
    List<FrotaAbastecimento> findAllByTenantIdAndVeiculoIdOrderByDataHoraDesc(UUID tenantId, UUID veiculoId);
}
