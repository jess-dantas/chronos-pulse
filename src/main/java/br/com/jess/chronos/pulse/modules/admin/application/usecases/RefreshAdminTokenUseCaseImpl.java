package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RefreshAdminTokenUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshAdminTokenUseCaseImpl implements RefreshAdminTokenUseCase {

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final JwtService jwtService;

    @Override
    public Resultado executar(Comando comando) {
        if (!jwtService.isTokenValido(comando.refreshToken())) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        Claims claims = jwtService.extrairClaims(comando.refreshToken());
        // Access/temp tokens não valem como refresh.
        if (!jwtService.isRefreshToken(claims)) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        String adminId = claims.get("adminId", String.class);
        if (adminId == null) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        UUID id;
        try {
            id = UUID.fromString(adminId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        AdminPlataforma admin = repositoryPort.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido ou expirado"));

        if (!admin.isAtivo()) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        String accessToken = jwtService.gerarAccessTokenAdmin(admin.getUsername(), admin.getId().toString());
        String refreshToken = jwtService.gerarRefreshTokenAdmin(admin.getUsername(), admin.getId().toString());

        return new Resultado(admin, accessToken, refreshToken);
    }
}
