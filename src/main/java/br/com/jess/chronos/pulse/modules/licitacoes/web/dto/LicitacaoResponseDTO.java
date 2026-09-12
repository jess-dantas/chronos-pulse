package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoLicitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoModalidade;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoTipoJulgamento;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LicitacaoResponseDTO(
        UUID id,
        UUID tenantId,
        String numero,
        LicitacaoModalidade modalidade,
        LicitacaoTipoJulgamento tipoJulgamento,
        String objeto,
        LocalDate dataAbertura,
        java.math.BigDecimal valorEstimado,
        String observacoes,
        String status,
        Boolean pedidoGerado,
        Boolean contratoGerado,
        String pncpStatus,
        String pncpProtocolo,
        Instant pncpPublicadoEm,
        String pncpErro,
        List<LicitacaoItemResponseDTO> itens,
        List<LicitacaoParticipanteResponseDTO> participantes,
        List<LicitacaoPropostaResponseDTO> propostas,
        List<LanceResponseDTO> lances,
        Instant criadoEm,
        UUID contratoId,
        String contratoNumero,
        String contratoStatus
) {
    public static LicitacaoResponseDTO from(Licitacao licitacao,
            List<LicitacaoItemResponseDTO> itens,
            List<LicitacaoParticipanteResponseDTO> participantes,
            List<LicitacaoPropostaResponseDTO> propostas,
            List<LanceResponseDTO> lances) {
        return from(licitacao, itens, participantes, propostas, lances, null);
    }

    public static LicitacaoResponseDTO from(Licitacao licitacao,
            List<LicitacaoItemResponseDTO> itens,
            List<LicitacaoParticipanteResponseDTO> participantes,
            List<LicitacaoPropostaResponseDTO> propostas,
            List<LanceResponseDTO> lances,
            ContratoLicitacao contrato) {
        return new LicitacaoResponseDTO(
                licitacao.getId(), licitacao.getTenantId(), licitacao.getNumero(),
                licitacao.getModalidade(), licitacao.getTipoJulgamento(),
                licitacao.getObjeto(), licitacao.getDataAbertura(), licitacao.getValorEstimado(),
                licitacao.getObservacoes(), licitacao.getStatus().name(), licitacao.getPedidoGerado(),
                licitacao.getContratoGerado(),
                licitacao.getPncpStatus() != null ? licitacao.getPncpStatus().name() : "NAO_PUBLICADO",
                licitacao.getPncpProtocolo(), licitacao.getPncpPublicadoEm(), licitacao.getPncpErro(),
                itens, participantes, propostas, lances, licitacao.getCriadoEm(),
                contrato != null ? contrato.getId() : null,
                contrato != null ? contrato.getNumero() : null,
                contrato != null ? contrato.getStatus() : null);
    }
}