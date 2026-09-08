package br.com.jess.chronos.pulse.modules.patrimonio.repository;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Desfazimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesfazimentoRepository extends JpaRepository<Desfazimento, UUID> {
    Page<Desfazimento> findAllByTenantId(UUID tenantId, Pageable pageable);
    Optional<Desfazimento> findByIdAndTenantId(UUID id, UUID tenantId);
}