package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TransferenciaResponseDTO(
        UUID id,
        UUID patrimonioId,
        String localizacaoOrigem,
        String localizacaoDestino,
        String responsavelOrigem,
        String responsavelDestino,
        OffsetDateTime dataSolicitacao,
        LocalDate dataPrevista,
        OffsetDateTime dataEfetivacao,
        String status,
        String justificativa,
        String aprovadoPor,
        String solicitadoPor
) {}