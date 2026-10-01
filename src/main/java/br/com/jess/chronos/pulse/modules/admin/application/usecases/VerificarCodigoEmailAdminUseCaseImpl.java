package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarCodigoEmailAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificarCodigoEmailAdminUseCaseImpl implements VerificarCodigoEmailAdminUseCase {

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public Resultado executar(Comando comando) {
        Claims claims;
        try {
            claims = jwtService.extrairClaims(comando.tempToken());
        } catch (Exception e) {
            throw new IllegalArgumentException("Sessão expirada. Refaça o login.");
        }
        if (!jwtService.isTwoFactorToken(claims)) {
            throw new IllegalArgumentException("Token inválido");
        }

        String adminId = claims.get("adminId", String.class);
        if (adminId == null) {
            throw new IllegalArgumentException("Token inválido");
        }

        AdminPlataforma admin = repositoryPort.buscarPorId(UUID.fromString(adminId))
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!admin.isAtivo()) {
            throw new IllegalArgumentException("Conta desativada");
        }
        if (!admin.isCodigoEmailValido()) {
            throw new IllegalArgumentException("Código expirado ou não solicitado");
        }
        if (!passwordEncoder.matches(comando.codigo(), admin.getRecuperacaoEmailHash())) {
            admin.registrarTentativaCodigoEmail();
            repositoryPort.salvar(admin);
            throw new IllegalArgumentException("Código inválido");
        }

        admin.limparCodigoEmail();
        admin.registrarLoginSucesso();
        admin.setUltimoLogin(Instant.now());
        repositoryPort.salvar(admin);

        String accessToken = jwtService.gerarAccessTokenAdmin(admin.getUsername(), admin.getId().toString());
        String refreshToken = jwtService.gerarRefreshTokenAdmin(admin.getUsername(), admin.getId().toString());

        return new Resultado(admin, accessToken, refreshToken);
    }
}
