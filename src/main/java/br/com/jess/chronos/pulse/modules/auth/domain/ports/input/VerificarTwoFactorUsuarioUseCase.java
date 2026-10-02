package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

/// 2FA do colaborador na etapa de login: valida o TOTP (Google Authenticator)
/// contra o tempToken emitido por POST /auth/login.
public interface VerificarTwoFactorUsuarioUseCase {
    record Comando(String tempToken, String codigo) {}
    AutenticarUsuarioUseCase.Resultado executar(Comando comando);
}
