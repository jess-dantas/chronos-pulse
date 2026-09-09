package br.com.jess.chronos.pulse.modules.licitacoes.service;

import java.time.Instant;

/**
 * Resultado da publicação do aviso de licitação no PNCP.
 */
public record PncpResultado(
        String protocolo,
        Instant publicadoEm
) {
}