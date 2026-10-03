package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDispositivoRequestDTO {

    // Opcional: nome exibido do aparelho (ex.: "MacBook do Administrator").
    @Size(max = 120, message = "deviceName deve ter no máximo 120 caracteres")
    private String deviceName;
}
