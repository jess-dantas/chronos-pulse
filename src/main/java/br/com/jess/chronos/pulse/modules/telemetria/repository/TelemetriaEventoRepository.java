package br.com.jess.chronos.pulse.modules.telemetria.repository;

import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TelemetriaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface TelemetriaEventoRepository
        extends JpaRepository<TelemetriaEvento, UUID>, JpaSpecificationExecutor<TelemetriaEvento> {

    @Modifying
    @Query("DELETE FROM TelemetriaEvento e WHERE e.criadoEm < :corte")
    int excluirAnterioresA(Instant corte);
}