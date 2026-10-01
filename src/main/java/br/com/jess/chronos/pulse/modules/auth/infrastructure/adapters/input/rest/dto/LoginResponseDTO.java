package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
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
        List<String> modulos,
        Boolean requiresTwoFactor,
        String tempToken
) {
    public static LoginResponseDTO dePendente(LoginResponseDTO dados) {
        return new LoginResponseDTO(
                null, null, dados.role(), dados.cpf(), dados.cpcId(), dados.nome(), dados.email(),
                dados.tenantId(), dados.tenantSlug(), dados.acessoEstoque(), dados.acessoPatrimonio(),
                dados.acessoFrota(), dados.acessoProtocolo(), dados.foto(), dados.modulos(),
                true, dados.tempToken());
    }
}
