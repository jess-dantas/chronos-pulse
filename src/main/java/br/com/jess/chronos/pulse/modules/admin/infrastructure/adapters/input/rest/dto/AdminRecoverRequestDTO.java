package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminRecoverRequestDTO {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    // Opcional: o recovery code já é um segredo; a senha é fator adicional.
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    private String senha;

    @NotBlank(message = "Código de recuperação é obrigatório")
    private String recoveryCode;

    // Opcional (R1): troca a senha no mesmo passo da recuperação.
    @Size(min = 8, max = 100, message = "Nova senha deve ter entre 8 e 100 caracteres")
    private String novaSenha;
}
