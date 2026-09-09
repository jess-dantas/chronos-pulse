package br.com.jess.chronos.pulse.modules.patrimonio.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CadastrarPatrimonioDTO(
        String tombamento,

        @NotBlank(message = "Descrição é obrigatória")
        String descricao,

        String categoria,

        @NotBlank(message = "Estado é obrigatório")
        String estado,

        String localizacao,

        LocalDate dataAquisicao,

        BigDecimal valorAquisicao,

        String responsavelNome,

        String numeroNotaFiscal,

        String observacoes,

        Integer vidaUtilMeses,

        LocalDate dataInicioDepreciacao
) {}
