package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import java.util.UUID;

public record LicitacaoParticipanteResponseDTO(
        UUID fornecedorId,
        String fornecedorNome,
        Boolean habilitado
) {}