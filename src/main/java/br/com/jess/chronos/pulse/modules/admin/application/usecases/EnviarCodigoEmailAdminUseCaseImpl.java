package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.EnviarCodigoEmailAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EnviarCodigoEmailAdminUseCaseImpl implements EnviarCodigoEmailAdminUseCase {

    private static final long VALIDADE_MINUTOS = 15;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    @Override
    public void executar(Comando comando) {
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

        AdminPlataforma admin = repositoryPort.buscarPorId(java.util.UUID.fromString(adminId))
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!admin.isAtivo()) {
            throw new IllegalArgumentException("Conta desativada");
        }
        if (admin.isLoginBloqueado()) {
            throw new IllegalStateException("Conta temporariamente bloqueada por excesso de tentativas");
        }
        if (admin.getEmail() == null || admin.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-mail de recuperação não cadastrado");
        }

        String codigo = String.format("%08d", SECURE_RANDOM.nextInt(90_000_000) + 10_000_000);
        admin.definirCodigoEmail(
                passwordEncoder.encode(codigo),
                Instant.now().plus(Duration.ofMinutes(VALIDADE_MINUTOS)));
        repositoryPort.salvar(admin);

        emailRecuperacaoSenhaService.enviarCodigoRecuperacaoAsync(admin.getEmail(), codigo);
    }
}
