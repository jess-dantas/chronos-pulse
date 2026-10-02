package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.EnviarCodigoEmailUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnviarCodigoEmailUsuarioUseCaseImpl implements EnviarCodigoEmailUsuarioUseCase {

    private static final long VALIDADE_MINUTOS = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    @Override
    public void executar(Comando comando) {
        CpcUsuario usuario = extrairUsuario(comando.tempToken());

        String email = usuario.getEmailPreferencial();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail de recuperação não cadastrado");
        }

        String codigo = String.format("%08d", SECURE_RANDOM.nextInt(90_000_000) + 10_000_000);
        usuario.definirCodigoEmail2FA(
                passwordEncoder.encode(codigo),
                Instant.now().plus(Duration.ofMinutes(VALIDADE_MINUTOS)));
        repositoryPort.atualizar(usuario);

        emailRecuperacaoSenhaService.enviarCodigoRecuperacaoAsync(email, codigo);
    }

    private CpcUsuario extrairUsuario(String tempToken) {
        Claims claims;
        try {
            claims = jwtService.extrairClaims(tempToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Sessão expirada. Refaça o login.");
        }
        if (!jwtService.isTwoFactorToken(claims)) {
            throw new IllegalArgumentException("Token inválido");
        }
        String usuarioId = claims.get("usuarioId", String.class);
        if (usuarioId == null) {
            throw new IllegalArgumentException("Token inválido");
        }
        CpcUsuario usuario = repositoryPort.buscarPorId(UUID.fromString(usuarioId))
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));
        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        if (usuario.isLoginBloqueado()) {
            throw new IllegalStateException("Conta temporariamente bloqueada por excesso de tentativas");
        }
        return usuario;
    }
}
