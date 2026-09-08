package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DesfazimentoResponseDTO(
        UUID id,
        UUID tenantId,
        UUID patrimonioId,
        String patrimonioDescricao,
        String tombamento,
        String estadoBem,
        String tipoDesfazimento,
        String justificativa,
        String responsavelSolicitacao,
        OffsetDateTime dataSolicitacao,
        String parecerComissao,
        Boolean aprovado,
        OffsetDateTime dataAprovacao,
        OffsetDateTime dataBaixa,
        String status,
        String processoNumero,
        String observacoes,
        List<MembroResponse> comissao
) {
    public record MembroResponse(
            UUID id,
            String nome,
            String cargo,
            String cpf,
            Boolean relator,
            String parecer
    ) {}
}