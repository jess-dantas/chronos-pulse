package br.com.jess.chronos.pulse.modules.compras.web.dto;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoCompra;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CotacaoResponseDTO(
        UUID id,
        UUID tenantId,
        String numero,
        UUID requisicaoId,
        String requisicaoNumero,
        LocalDate dataLimite,
        String observacoes,
        String status,
        Boolean pedidoGerado,
        List<FornecedorCotacaoDTO> fornecedores,
        List<PropostaResponseDTO> propostas,
        Instant criadoEm
) {
    public static CotacaoResponseDTO from(CotacaoCompra cotacao, String requisicaoNumero,
                                          List<FornecedorCotacaoDTO> fornecedores,
                                          List<PropostaResponseDTO> propostas) {
        return new CotacaoResponseDTO(
                cotacao.getId(), cotacao.getTenantId(), cotacao.getNumero(),
                cotacao.getRequisicao().getId(), requisicaoNumero, cotacao.getDataLimite(),
                cotacao.getObservacoes(), cotacao.getStatus().name(), cotacao.getPedidoGerado(),
                fornecedores, propostas, cotacao.getCriadoEm());
    }
}