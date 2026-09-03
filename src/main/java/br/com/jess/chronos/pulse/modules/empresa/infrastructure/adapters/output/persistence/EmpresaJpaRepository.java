package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaJpaEntity, UUID> {
    Optional<EmpresaJpaEntity> findByCnpj(String cnpj);
    boolean existsByCnpj(String cnpj);
}
