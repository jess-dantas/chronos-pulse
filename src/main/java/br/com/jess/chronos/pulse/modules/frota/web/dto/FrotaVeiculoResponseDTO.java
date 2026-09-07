package br.com.jess.chronos.pulse.modules.frota.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record FrotaVeiculoResponseDTO(
        UUID id,
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
        String observacoes,
        Boolean ativo
) {}
