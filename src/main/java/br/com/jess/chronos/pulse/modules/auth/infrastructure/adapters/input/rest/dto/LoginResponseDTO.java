package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import java.util.List;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String role,
        String cpcId,
        String nome,
        String email,
        String tenantId,
        boolean acessoEstoque,
        String foto,
        List<String> modulos
) {}
