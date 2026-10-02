package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/// Verificação do 2FA no modo sem login (ordem biometria → 2FA → vínculo):
/// valida TOTP (6 dígitos) ou OTP por e-mail (8 dígitos). Com `metodo=EMAIL`
/// e sem código, gera e envia o OTP (retorna `enviado=true`).
public interface VerificarDeviceTwoFactorUseCase {

    enum Metodo { TOTP, EMAIL }

    record Comando(String deviceTokenBruto, Metodo metodo, String codigo) {}
    record Resultado(boolean verificado, boolean enviado, Instant expiraEm,
                     UUID cpcId, String cpf, String role) {}

    Optional<Resultado> executar(Comando comando);
}
