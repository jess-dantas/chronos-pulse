package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTwoFactorStatusDTO {
    private boolean enabled;
}
