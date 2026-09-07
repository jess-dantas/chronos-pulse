package br.com.jess.chronos.pulse.modules.protocolo.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProtocoloResponseDTO(
        UUID id,
        String numeroProtocolo,
        String tipo,
        String assunto,
        String descricao,
        String remetente,
        String destinatario,
        OffsetDateTime dataProtocolo,
        String status,
        String responsavel,
        String observacoes,
        Boolean ativo
) {}
