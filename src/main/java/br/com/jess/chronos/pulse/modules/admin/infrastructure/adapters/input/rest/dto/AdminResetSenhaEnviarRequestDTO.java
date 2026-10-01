package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminResetSenhaEnviarRequestDTO {

    @NotBlank(message = "Username é obrigatório")
    private String username;
}
