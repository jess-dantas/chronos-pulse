package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

/// Envia o OTP de 8 dígitos (15 min) por e-mail na etapa de login do
/// colaborador, autenticado pelo tempToken do 2FA.
public interface EnviarCodigoEmailUsuarioUseCase {
    record Comando(String tempToken) {}
    void executar(Comando comando);
}
