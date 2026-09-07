package br.com.jess.chronos.pulse.modules.modulo.web.dto;

public record ModuloResponseDTO(
        String codigo,
        String nome,
        String descricao,
        boolean ativo
) {}