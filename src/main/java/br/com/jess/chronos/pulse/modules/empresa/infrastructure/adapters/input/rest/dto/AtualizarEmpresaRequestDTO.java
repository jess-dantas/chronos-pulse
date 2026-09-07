package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest.dto;

public record AtualizarEmpresaRequestDTO(
        String nome,
        Boolean ativo
) {}
