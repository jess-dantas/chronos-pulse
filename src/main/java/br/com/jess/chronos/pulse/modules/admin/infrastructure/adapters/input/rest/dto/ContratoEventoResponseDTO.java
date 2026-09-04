package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.admin.domain.model.ContratoEvento;

import java.time.Instant;
import java.util.UUID;

public record ContratoEventoResponseDTO(
        UUID id,
        UUID contratoId,
        String tipo,
        String descricao,
        UUID criadoPor,
        Instant criadoEm
) {
    public static ContratoEventoResponseDTO fromDomain(ContratoEvento e) {
        return new ContratoEventoResponseDTO(
                e.getId(), e.getContratoId(), e.getTipo(),
                e.getDescricao(), e.getCriadoPor(), e.getCriadoEm()
        );
    }
}
