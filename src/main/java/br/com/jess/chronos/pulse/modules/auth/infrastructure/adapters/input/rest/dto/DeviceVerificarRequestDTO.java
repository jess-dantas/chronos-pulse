package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

/// Verificação do 2FA no modo sem login. `codigo` é opcional apenas quando
/// `metodo=EMAIL` (nesse caso o POST gera e envia o OTP e devolve enviado=true).
public record DeviceVerificarRequestDTO(
        String codigo,
        TwoFactorMetodo metodo
) {}
