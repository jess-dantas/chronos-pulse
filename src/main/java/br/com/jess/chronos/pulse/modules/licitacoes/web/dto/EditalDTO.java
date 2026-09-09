package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record EditalDTO(
        String numeroProcesso,
        String numeroEdital,
        String localSessao,
        LocalDate dataAberturaSessao,
        LocalTime horarioAbertura,
        String formaEntregaPropostas,
        String anexos,
        String observacoes
) {}