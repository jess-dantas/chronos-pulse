package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;

public interface RefreshAdminTokenUseCase {

    record Comando(String refreshToken) {}

    /**
     * Rotação: emite um par novo de access+refresh a partir de um refresh
     * token de admin válido (typ=refresh + claim adminId).
     */
    record Resultado(AdminPlataforma admin, String accessToken, String refreshToken) {}

    Resultado executar(Comando comando);
}
