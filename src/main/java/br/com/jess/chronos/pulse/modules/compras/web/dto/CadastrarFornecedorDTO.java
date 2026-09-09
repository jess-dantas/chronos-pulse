package br.com.jess.chronos.pulse.modules.compras.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastrarFornecedorDTO(
        @NotBlank(message = "CNPJ é obrigatório")
        @Size(max = 14, message = "CNPJ deve conter no máximo 14 caracteres")
        String cnpj,

        @NotBlank(message = "Razão social é obrigatória")
        @Size(max = 180, message = "Razão social deve conter no máximo 180 caracteres")
        String razaoSocial,

        @Size(max = 120)
        String nomeFantasia,

        @Size(max = 30)
        String inscricaoEstadual,

        @Email(message = "E-mail inválido")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String telefone,

        @Size(max = 180)
        String enderecoLogradouro,

        @Size(max = 20)
        String enderecoNumero,

        @Size(max = 80)
        String enderecoBairro,

        @Size(max = 80)
        String enderecoCidade,

        @Size(max = 2)
        String enderecoUf,

        @Size(max = 8)
        String enderecoCep,

        String observacoes
) {}