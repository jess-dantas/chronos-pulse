package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

import java.util.List;

public interface AutenticarUsuarioUseCase {
    record Comando(String cpf, String senha) {}
    record Resultado(
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
    Resultado executar(Comando comando);
}
