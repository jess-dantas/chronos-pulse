package br.com.jess.chronos.pulse.modules.compras.web.dto;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoCompra;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record RequisicaoResponseDTO(
        UUID id,
        UUID tenantId,
        String numero,
        UUID solicitanteCpcId,
        String solicitanteNome,
        String justificativa,
        LocalDate dataRequisicao,
        String observacoes,
        String status,
        List<RequisicaoItemResponseDTO> itens,
        Instant criadoEm
) {
    public static RequisicaoResponseDTO from(RequisicaoCompra requisicao, String solicitanteNome,
                                             List<RequisicaoItemResponseDTO> itens) {
        return new RequisicaoResponseDTO(
                requisicao.getId(), requisicao.getTenantId(), requisicao.getNumero(),
                requisicao.getSolicitanteCpcId(), solicitanteNome, requisicao.getJustificativa(),
                requisicao.getDataRequisicao(), requisicao.getObservacoes(), requisicao.getStatus().name(),
                itens, requisicao.getCriadoEm());
    }
}