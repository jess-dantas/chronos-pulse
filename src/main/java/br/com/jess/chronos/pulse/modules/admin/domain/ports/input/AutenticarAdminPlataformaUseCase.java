package br.com.jess.chronos.pulse.modules.admin.domain.ports.input;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;

import java.util.UUID;

public interface AutenticarAdminPlataformaUseCase {

    record Comando(String username, String senha) {}

    /**
     * Quando requiresTwoFactor=true, accessToken/refreshToken vêm nulos e
     * tempToken contém o JWT temporário (typ=two_factor, 5 min).
     * setupRequired=true → o 2FA ainda não foi configurado e a configuração
     * é obrigatória (chronos.admin.two-factor-required=true): o cliente deve
     * chamar POST /admin/auth/2fa/setup + /2fa/confirm com o tempToken.
     * setupRequired=false → cliente deve chamar POST /admin/auth/2fa/verify.
     */
    record Resultado(
            AdminPlataforma admin,
            String accessToken,
            String refreshToken,
            boolean requiresTwoFactor,
            String tempToken,
            boolean setupRequired
    ) {
        public Resultado(AdminPlataforma admin, String accessToken,
                         String refreshToken, boolean requiresTwoFactor, String tempToken) {
            this(admin, accessToken, refreshToken, requiresTwoFactor, tempToken, false);
        }
    }

    Resultado executar(Comando comando);
}