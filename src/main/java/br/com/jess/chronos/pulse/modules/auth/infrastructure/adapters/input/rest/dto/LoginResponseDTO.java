package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import java.util.List;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String role,
        String cpf,
        String cpcId,
        String nome,
        String email,
        String tenantId,
        String tenantSlug,
        boolean acessoEstoque,
        boolean acessoPatrimonio,
        boolean acessoFrota,
        boolean acessoProtocolo,
        String foto,
        List<String> modulos
) {}
