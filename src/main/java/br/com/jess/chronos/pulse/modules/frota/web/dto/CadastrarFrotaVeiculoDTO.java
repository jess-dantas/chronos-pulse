package br.com.jess.chronos.pulse.modules.frota.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CadastrarFrotaVeiculoDTO(
        @NotBlank(message = "Placa é obrigatória")
        String placa,

        String renavam,

        String marca,

        String modelo,

        Integer anoFabricacao,

        Integer anoModelo,

        String tipo,

        String combustivel,

        String status,

        BigDecimal odometroAtual,

        String observacoes
) {}
