package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;

import java.util.List;

public interface RecuperarAcessoAdminUseCase {

    record Comando(String username, String senha, String recoveryCode) {}

    record Resultado(
            AdminPlataforma admin,
            String accessToken,
            String refreshToken,
            List<String> novosRecoveryCodes
    ) {}

    Resultado executar(Comando comando);
}
