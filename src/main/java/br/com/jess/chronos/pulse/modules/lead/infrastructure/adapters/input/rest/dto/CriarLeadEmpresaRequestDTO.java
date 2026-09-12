package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarLeadEmpresaRequestDTO(
        @NotBlank @Size(min = 14, max = 18) String cnpj,
        @NotBlank @Size(max = 200) String razaoSocial,
        @NotBlank @Size(max = 120) String contatoNome,
        @NotBlank @Email @Size(max = 160) String contatoEmail,
        @Size(max = 20) String contatoTelefone,
        @Size(max = 20) String contatoCelular,
        @Size(max = 255) String enderecoLogradouro,
        @Size(max = 20) String enderecoNumero,
        @Size(max = 120) String enderecoComplemento,
        @Size(max = 120) String enderecoBairro,
        @Size(max = 120) String enderecoCidade,
        @Size(max = 2) String enderecoUf,
        @Size(max = 8) String enderecoCep,
        @Size(max = 500) String observacao
) {

    public LeadEmpresa toDomain() {
        return new LeadEmpresa(
                null, cnpj, razaoSocial, contatoNome, contatoEmail, contatoTelefone, contatoCelular,
                enderecoLogradouro, enderecoNumero, enderecoComplemento, enderecoBairro,
                enderecoCidade, enderecoUf, enderecoCep, observacao
        );
    }
}