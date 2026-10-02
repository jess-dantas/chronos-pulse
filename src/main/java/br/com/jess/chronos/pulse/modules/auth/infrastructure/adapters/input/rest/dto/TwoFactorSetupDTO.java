package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto;

public record TwoFactorSetupDTO(String secret, String otpauthUri) {}
