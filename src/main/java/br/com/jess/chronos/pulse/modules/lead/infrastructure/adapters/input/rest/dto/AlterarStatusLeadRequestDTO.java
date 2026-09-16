package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresaStatus;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusLeadRequestDTO(
        @NotNull(message = "Status é obrigatório.") LeadEmpresaStatus status
) {
}