package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RegistroPontoDTO(
        UUID idLocal,
        @NotNull Instant dataHoraDispositivo,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal precisaoGps,
        String fotoUrl,
        String hashLocal
) {
    public RegistroPonto toDomain(UUID colaboradorId, UUID tenantId) {
        return new RegistroPonto(
                this.idLocal(),
                colaboradorId,
                tenantId,
                this.dataHoraDispositivo(),
                null,
                null, // tipoRegistro será determinado pelo use case
                this.latitude(),
                this.longitude(),
                this.precisaoGps(),
                this.fotoUrl(),
                true,
                null
        );
    }
}
