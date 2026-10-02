package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.SolicitarResetSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SolicitarResetSenhaAdminUseCaseImpl implements SolicitarResetSenhaAdminUseCase {

    private static final long VALIDADE_MINUTOS = 15;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    @Override
    public void executar(Comando comando) {
        AdminPlataforma admin = repositoryPort.buscarPorUsername(comando.username())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!admin.isAtivo()) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        if (admin.isLoginBloqueado()) {
            throw new IllegalStateException(
                    "Conta temporariamente bloqueada por excesso de tentativas");
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
