package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginResponseDTO {

    private UUID id;
    private String username;
    private String nomeCompleto;
    private String email;
    private Instant ultimoLogin;
    private boolean ativo;
    private Instant criadoEm;
    private String accessToken;
    private String refreshToken;
    private boolean requiresTwoFactor;
    private String tempToken;
    private boolean setupRequired;
    private java.util.List<String> recoveryCodes;

    public static AdminLoginResponseDTO fromDomain(AdminPlataforma admin, String accessToken, String refreshToken) {
        return AdminLoginResponseDTO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nomeCompleto(admin.getNomeCompleto())
                .email(admin.getEmail())
                .ultimoLogin(admin.getUltimoLogin())
                .ativo(admin.isAtivo())
                .criadoEm(admin.getCriadoEm())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .requiresTwoFactor(false)
                .build();
    }

    public static AdminLoginResponseDTO twoFactorRequired(String tempToken) {
        return AdminLoginResponseDTO.builder()
                .requiresTwoFactor(true)
                .tempToken(tempToken)
                .build();
    }

    public static AdminLoginResponseDTO twoFactorSetupRequired(String tempToken) {
        return AdminLoginResponseDTO.builder()
                .requiresTwoFactor(true)
                .setupRequired(true)
                .tempToken(tempToken)
                .build();
    }
}