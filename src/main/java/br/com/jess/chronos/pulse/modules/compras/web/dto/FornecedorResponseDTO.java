package br.com.jess.chronos.pulse.modules.compras.web.dto;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;

import java.util.UUID;

public record FornecedorResponseDTO(
        UUID id,
        UUID tenantId,
        String cnpj,
        String razaoSocial,
        String nomeFantasia,
        String inscricaoEstadual,
        String email,
        String telefone,
        String enderecoLogradouro,
        String enderecoNumero,
        String enderecoBairro,
        String enderecoCidade,
        String enderecoUf,
        String enderecoCep,
        String observacoes,
        Boolean ativo
) {
    public static FornecedorResponseDTO from(Fornecedor f) {
        return new FornecedorResponseDTO(
                f.getId(), f.getTenantId(), f.getCnpj(), f.getRazaoSocial(), f.getNomeFantasia(),
                f.getInscricaoEstadual(), f.getEmail(), f.getTelefone(),
                f.getEnderecoLogradouro(), f.getEnderecoNumero(), f.getEnderecoBairro(),
                f.getEnderecoCidade(), f.getEnderecoUf(), f.getEnderecoCep(),
                f.getObservacoes(), f.getAtivo());
    }
}