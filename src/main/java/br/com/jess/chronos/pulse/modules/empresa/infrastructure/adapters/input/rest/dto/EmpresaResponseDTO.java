package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest.dto;

import java.util.UUID;

public record EmpresaResponseDTO(UUID id, String cnpj, String nome) {}
