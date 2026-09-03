package br.com.jess.chronos.pulse.modules.estoque.web.dto;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.RequisicaoStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RequisicaoResponseDTO(
        UUID id,
        UUID almoxarifadoId,
        String almoxarifadoNome,
        UUID solicitanteCpcId,
        String departamento,
        String justificativa,
        RequisicaoStatus status,
        OffsetDateTime dataSolicitacao,
        OffsetDateTime dataAtendimento,
        UUID atendenteCpcId,
        List<ItemRequisicaoResponseDTO> itens
) {}
