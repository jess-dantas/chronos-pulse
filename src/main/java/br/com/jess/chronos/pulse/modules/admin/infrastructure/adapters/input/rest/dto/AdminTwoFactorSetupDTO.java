package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTwoFactorSetupDTO {
    private String secret;
    private String otpauthUri;
}
