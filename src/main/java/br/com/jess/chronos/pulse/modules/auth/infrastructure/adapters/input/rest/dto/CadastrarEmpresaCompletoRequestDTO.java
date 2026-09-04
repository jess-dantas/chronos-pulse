package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarEmpresaCompletoRequestDTO(
        @NotBlank @Size(min = 14, max = 14) String cnpj,
        @NotBlank String nomeEmpresa,
        @NotBlank String responsavelNome,
        @NotBlank @Size(min = 11, max = 11) String responsavelCpf,
        @NotBlank @Email String responsavelEmail,
        String responsavelCelular,
        @NotBlank @Size(min = 6) String responsavelSenha
) {}
