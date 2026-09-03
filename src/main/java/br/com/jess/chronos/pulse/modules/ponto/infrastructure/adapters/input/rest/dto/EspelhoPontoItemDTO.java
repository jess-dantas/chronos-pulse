package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EspelhoPontoItemDTO(
        UUID id,
        UUID colaboradorId,
        UUID tenantId,
        Instant dataHoraDispositivo,
        Instant dataHoraServidor,
        TipoRegistro tipoRegistro,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal precisaoGps,
        String fotoUrl,
        String hashIntegridade,
        Boolean sincronizadoOffline,
        Long nsr,
        Boolean ajusteManual,
        String justificativa,
        String observacao
) {
    public static EspelhoPontoItemDTO fromDomain(RegistroPonto r) {
        return new EspelhoPontoItemDTO(
                r.getId(),
                r.getColaboradorId(),
                r.getTenantId(),
                r.getDataHoraDispositivo(),
                r.getDataHoraServidor(),
                r.getTipoRegistro(),
                r.getLatitude(),
                r.getLongitude(),
                r.getPrecisaoGps(),
                r.getFotoUrl(),
                r.getHashIntegridade(),
                r.getSincronizadoOffline(),
                r.getNsr(),
                r.getAjusteManual(),
                r.getJustificativa(),
                r.getObservacao()
        );
    }
}
