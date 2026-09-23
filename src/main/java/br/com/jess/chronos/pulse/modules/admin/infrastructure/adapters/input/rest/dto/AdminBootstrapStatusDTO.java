package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminBootstrapStatusDTO {
    private final boolean bootstrapAvailable;
}
