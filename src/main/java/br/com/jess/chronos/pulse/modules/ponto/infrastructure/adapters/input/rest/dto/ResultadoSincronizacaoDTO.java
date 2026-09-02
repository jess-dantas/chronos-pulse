package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import java.util.List;
import java.util.UUID;

public record ResultadoSincronizacaoDTO(
        List<UUID> idsSucesso,
        List<UUID> idsFalha
) {}
