package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank String cpf,
        // Opcional (2FA-first): sem senha o login exige 2FA habilitado e
        // emite tempToken para /auth/2fa/verify.
        String senha
) {}
