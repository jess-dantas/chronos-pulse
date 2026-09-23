package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;

import java.util.List;

public interface GerenciarTwoFactorAdminUseCase {

    record StatusResultado(boolean enabled) {}

    record SetupResultado(String secret, String otpauthUri) {}

    /**
     * Resultado do confirm: 8 códigos de recuperação em texto puro,
     * exibidos uma única vez ao usuário.
     */
    record ConfirmacaoResultado(AdminPlataforma admin, List<String> recoveryCodes) {}

    StatusResultado status(String adminId);

    SetupResultado setup(String adminId);

    ConfirmacaoResultado confirmar(String adminId, String codigo);

    void desabilitar(String adminId, String codigo);
}
