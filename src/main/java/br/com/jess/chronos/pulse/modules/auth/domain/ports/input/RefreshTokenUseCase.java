package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

import java.util.List;

public interface RefreshTokenUseCase {

    record Comando(String refreshToken) {}

    record Resultado(
            String accessToken,
            String role,
            String cpf,
            String cpcId,
            String nome,
            String email,
            String tenantId,
            boolean acessoEstoque,
            boolean acessoPatrimonio,
            boolean acessoFrota,
            boolean acessoProtocolo,
            String foto,
            List<String> modulos
    ) {}

    Resultado executar(Comando comando);
}
