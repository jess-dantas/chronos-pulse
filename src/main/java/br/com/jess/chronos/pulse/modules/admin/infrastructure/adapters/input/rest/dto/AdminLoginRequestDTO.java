package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginRequestDTO {

    @NotBlank(message = "Username é obrigatório")
    @Size(max = 20, message = "Username deve ter no máximo 20 caracteres")
    private String username;

    // Opcional (2FA-first): sem senha o login exige 2FA habilitado e emite
    // tempToken direto; com senha o fluxo é o tradicional.
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    private String senha;

    // Opcional (biometria-first): dispositivo confiável. Quando presente e
    // válido, autentica direto pulando senha e 2FA; a biometria é confirmada
    // no aparelho antes de o cliente enviar o token.
    @Size(max = 200, message = "deviceToken deve ter no máximo 200 caracteres")
    private String deviceToken;
}