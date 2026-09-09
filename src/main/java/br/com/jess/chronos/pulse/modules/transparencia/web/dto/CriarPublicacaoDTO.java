package br.com.jess.chronos.pulse.modules.transparencia.web.dto;

import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.TipoPublicacaoTransparencia;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CriarPublicacaoDTO(
        @NotBlank(message = "Competência obrigatória no formato AAAA-MM")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Competência deve estar no formato AAAA-MM")
        String competencia,

        @NotNull(message = "Tipo de publicação obrigatório")
        TipoPublicacaoTransparencia tipoPublicacao,

        @DecimalMin(value = "0.0", message = "Valor total não pode ser negativo")
        BigDecimal valorTotal,

        @Min(value = 0, message = "Quantidade de itens não pode ser negativa")
        Integer itensCount,

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
        String observacoes
) {
}