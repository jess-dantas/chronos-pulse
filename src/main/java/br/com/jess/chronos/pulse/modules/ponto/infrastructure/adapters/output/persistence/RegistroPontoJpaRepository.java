package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

public interface RegistroPontoJpaRepository extends JpaRepository<RegistroPontoJpaEntity, UUID> {

    @Query("SELECT COALESCE(MAX(r.nsr), 0) + 1 FROM RegistroPontoJpaEntity r")
    Long obterProximoNsr();
}
