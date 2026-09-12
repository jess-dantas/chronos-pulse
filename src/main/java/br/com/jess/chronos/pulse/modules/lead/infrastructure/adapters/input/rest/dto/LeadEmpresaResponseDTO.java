package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;

import java.time.Instant;
import java.util.UUID;

public record LeadEmpresaResponseDTO(
        UUID id,
        String cnpj,
        String razaoSocial,
        String contatoNome,
        String contatoEmail,
        String contatoTelefone,
        String contatoCelular,
        String enderecoCidade,
        String enderecoUf,
        String status,
        Instant criadoEm
) {

    public static LeadEmpresaResponseDTO of(LeadEmpresa lead) {
        return new LeadEmpresaResponseDTO(
                lead.getId(), lead.getCnpj(), lead.getRazaoSocial(), lead.getContatoNome(),
                lead.getContatoEmail(), lead.getContatoTelefone(), lead.getContatoCelular(),
                lead.getEnderecoCidade(), lead.getEnderecoUf(),
                lead.getStatus() != null ? lead.getStatus().name() : null,
                lead.getCriadoEm()
        );
    }
}