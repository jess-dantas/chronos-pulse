package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

/// Valida o OTP por e-mail na etapa de login do colaborador e emite os
/// tokens finais.
public interface VerificarCodigoEmailUsuarioUseCase {
    record Comando(String tempToken, String codigo) {}
    AutenticarUsuarioUseCase.Resultado executar(Comando comando);
}
