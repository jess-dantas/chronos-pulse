package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import java.util.UUID;

public record PlanejamentoLicitacaoResponseDTO(
        UUID licitacaoId,
        String numero,
        String licitacaoStatus,
        EtpResponseDTO etp,
        TrResponseDTO tr,
        EditalResponseDTO edital
) {}