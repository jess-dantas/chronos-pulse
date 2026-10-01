package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

/// Gestão do 2FA do colaborador (menu Segurança / perfil): status, setup do
/// segredo TOTP, confirmação e desativação. Sem recovery codes (decisão).
public interface GerenciarTwoFactorUsuarioUseCase {

    record StatusResultado(boolean enabled) {}
    record SetupResultado(String secret, String otpauthUri) {}

    StatusResultado status(String usuarioId);
    SetupResultado setup(String usuarioId);
    void confirmar(String usuarioId, String codigo);
    void desabilitar(String usuarioId, String codigo);
}
