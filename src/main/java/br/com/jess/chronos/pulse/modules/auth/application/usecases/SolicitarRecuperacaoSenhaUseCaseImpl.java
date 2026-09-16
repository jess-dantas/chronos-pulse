package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.RecuperacaoSenha;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.SolicitarRecuperacaoSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.RecuperacaoSenhaRepositoryPort;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.security.SecureRandom;

public class SolicitarRecuperacaoSenhaUseCaseImpl implements SolicitarRecuperacaoSenhaUseCase {

    private static final long VALIDADE_MINUTOS = 15;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final RecuperacaoSenhaRepositoryPort recuperacaoSenhaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    public SolicitarRecuperacaoSenhaUseCaseImpl(CpcUsuarioRepositoryPort usuarioRepository,
                                                RecuperacaoSenhaRepositoryPort recuperacaoSenhaRepository,
                                                PasswordEncoder passwordEncoder,
                                                EmailRecuperacaoSenhaService emailRecuperacaoSenhaService) {
        this.usuarioRepository = usuarioRepository;
        this.recuperacaoSenhaRepository = recuperacaoSenhaRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailRecuperacaoSenhaService = emailRecuperacaoSenhaService;
    }

    @Override
    public void executar(Comando comando) {
        String cpf = comando.cpf().replaceAll("\\D", "");

        usuarioRepository.buscarPorCpf(cpf).ifPresent(usuario -> {
            String email = usuario.getEmailCorporativo() != null
                    ? usuario.getEmailCorporativo()
                    : usuario.getEmailPessoal();
            if (email == null || email.isBlank()) {
                return;
            }

            // Código de 8 dígitos com SecureRandom (10^8 combinações) + limite de
            // tentativas no RedefinirSenhaUseCaseImpl (C2 - security review).
            String codigo = String.format("%08d", SECURE_RANDOM.nextInt(90_000_000) + 10_000_000);
            String codigoHash = passwordEncoder.encode(codigo);

            recuperacaoSenhaRepository.salvar(new RecuperacaoSenha(
                    null, cpf, codigoHash,
                    Instant.now().plus(Duration.ofMinutes(VALIDADE_MINUTOS)),
                    false, Instant.now()));

            emailRecuperacaoSenhaService.enviarCodigoRecuperacaoAsync(email, codigo);
        });
    }
}