package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RegistroPontoDTO(
        UUID idLocal, // ID gerado no SQLite do aparelho mobile
        @NotNull UUID colaboradorId,
        @NotNull Instant dataHoraDispositivo,
        @NotNull TipoRegistro tipoRegistro,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal precisaoGps,
        String fotoUrl,
        Boolean sincronizadoOffline,
        String hashLocal // Hash SHA-256 gerado localmente pelo celular para auditoria
) {
    public RegistroPonto toDomain() {
        return new RegistroPonto(
                this.idLocal(),
                this.colaboradorId(),
                this.dataHoraDispositivo(),
                null, // dataHoraServidor será preenchido pelo backend no recebimento
                this.tipoRegistro(),
                this.latitude(),
                this.longitude(),
                this.precisaoGps(),
                this.fotoUrl(),
                true, // Sincronizado offline
                null
        );
    }
}
