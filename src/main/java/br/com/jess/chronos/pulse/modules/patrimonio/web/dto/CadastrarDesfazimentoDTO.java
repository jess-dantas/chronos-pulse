package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CadastrarDesfazimentoDTO(
        @NotNull UUID patrimonioId,
        @NotBlank String estadoBem,
        @NotBlank String tipoDesfazimento,
        @NotBlank String justificativa,
        @NotBlank String responsavelSolicitacao,
        String processoNumero,
        String observacoes,
        List<MembroDTO> membrosComissao
) {
    public record MembroDTO(
            @NotBlank String nome,
            String cargo,
            String cpf,
            Boolean relator,
            String parecer
    ) {}
}