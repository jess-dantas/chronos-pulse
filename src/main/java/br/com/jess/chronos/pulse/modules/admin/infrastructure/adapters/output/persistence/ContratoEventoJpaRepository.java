package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ContratoEventoJpaRepository extends JpaRepository<ContratoEventoJpaEntity, UUID> {
    List<ContratoEventoJpaEntity> findByContratoIdOrderByCriadoEmDesc(UUID contratoId);
}
