package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;

public interface VerificarTwoFactorAdminUseCase {

    record Comando(String tempToken, String codigo) {}

    record Resultado(AdminPlataforma admin, String accessToken, String refreshToken) {}

    Resultado executar(Comando comando);
}
