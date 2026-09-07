package br.com.jess.chronos.pulse.modules.frota.web.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CadastrarAbastecimentoDTO(
        @NotNull(message = "Veículo é obrigatório")
        UUID veiculoId,

        OffsetDateTime dataHora,

        @NotNull(message = "Litros é obrigatório")
        BigDecimal litros,

        @NotNull(message = "Valor por litro é obrigatório")
        BigDecimal valorLitro,

        BigDecimal odometroKm,

        String posto,

        String observacoes
) {}
