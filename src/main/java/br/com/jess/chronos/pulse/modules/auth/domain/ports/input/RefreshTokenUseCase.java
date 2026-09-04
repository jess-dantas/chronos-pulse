package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

public interface RefreshTokenUseCase {

    record Comando(String refreshToken) {}

    record Resultado(
            String accessToken,
            String role,
            String cpcId,
            String nome,
            String email,
            String tenantId,
            boolean acessoEstoque,
            String foto
    ) {}

    Resultado executar(Comando comando);
}
