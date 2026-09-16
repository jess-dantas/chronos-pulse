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
        String enderecoLogradouro,
        String enderecoNumero,
        String enderecoComplemento,
        String enderecoBairro,
        String enderecoCidade,
        String enderecoUf,
        String enderecoCep,
        String observacao,
        String status,
        Instant criadoEm
) {

    public static LeadEmpresaResponseDTO of(LeadEmpresa lead) {
        return new LeadEmpresaResponseDTO(
                lead.getId(), lead.getCnpj(), lead.getRazaoSocial(), lead.getContatoNome(),
                lead.getContatoEmail(), lead.getContatoTelefone(), lead.getContatoCelular(),
                lead.getEnderecoLogradouro(), lead.getEnderecoNumero(), lead.getEnderecoComplemento(),
                lead.getEnderecoBairro(), lead.getEnderecoCidade(), lead.getEnderecoUf(),
                lead.getEnderecoCep(), lead.getObservacao(),
                lead.getStatus() != null ? lead.getStatus().name() : null,
                lead.getCriadoEm()
        );
    }
}