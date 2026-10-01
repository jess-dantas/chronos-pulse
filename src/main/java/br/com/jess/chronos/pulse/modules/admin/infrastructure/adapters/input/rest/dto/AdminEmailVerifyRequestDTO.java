package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEmailVerifyRequestDTO {

    @NotBlank(message = "Token temporário é obrigatório")
    private String tempToken;

    @NotBlank(message = "Código é obrigatório")
    @Size(min = 8, max = 8, message = "Código deve ter 8 dígitos")
    private String codigo;
}
