package br.com.jess.chronos.pulse.modules.auth.domain.ports.input;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/// Status do vínculo de dispositivo no modo sem login: se o 2FA está ativo
/// para o dono e quando o vínculo expira (aviso antes de bater ponto).
public interface ConsultarStatusDeviceUseCase {

    record StatusResultado(UUID cpcId, String nome, boolean twoFactorEnabled,
                           boolean vinculoAtivo, Instant expiraEm) {}

    Optional<StatusResultado> executar(String deviceTokenBruto);
}
