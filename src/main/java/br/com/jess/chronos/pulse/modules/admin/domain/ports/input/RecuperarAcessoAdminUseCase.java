package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;

import java.util.List;

public interface RecuperarAcessoAdminUseCase {

    /**
     * senha e novaSenha são opcionais: o recovery code autentica sozinho
     * (R1); senha pode ser exigida como fator extra e novaSenha troca a
     * senha no mesmo passo.
     */
    record Comando(String username, String senha, String recoveryCode, String novaSenha) {
        public Comando(String username, String senha, String recoveryCode) {
            this(username, senha, recoveryCode, null);
        }
    }

    record Resultado(
            AdminPlataforma admin,
            String accessToken,
            String refreshToken,
            List<String> novosRecoveryCodes
    ) {}

    Resultado executar(Comando comando);
}
