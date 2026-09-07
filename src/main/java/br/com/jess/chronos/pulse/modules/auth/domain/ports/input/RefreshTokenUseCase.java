package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

import java.util.List;

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
            String foto,
            List<String> modulos
    ) {}

    Resultado executar(Comando comando);
}
