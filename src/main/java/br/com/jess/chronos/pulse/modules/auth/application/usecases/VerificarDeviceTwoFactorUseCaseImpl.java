package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import br.com.jess.chronos.pulse.modules.auth.application.service.DeviceTokenService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.VerificarDeviceTwoFactorUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificarDeviceTwoFactorUseCaseImpl implements VerificarDeviceTwoFactorUseCase {

    private static final long VALIDADE_MINUTOS = 15;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DeviceTokenService deviceTokenService;
    private final CpcUsuarioRepositoryPort repositoryPort;
    private final TotpService totpService;
    private final PasswordEncoder passwordEncoder;
    private final EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    @Override
    public Optional<Resultado> executar(Comando comando) {
        Optional<CpcUsuario> talvezUsuario = deviceTokenService.autenticar(comando.deviceTokenBruto());
        if (talvezUsuario.isEmpty()) {
            return Optional.empty();
        }
        CpcUsuario usuario = talvezUsuario.get();

        if (!usuario.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("2FA não está habilitado");
        }
        if (usuario.isLoginBloqueado()) {
            throw new IllegalStateException("Conta temporariamente bloqueada por excesso de tentativas");
        }

        if (comando.metodo() == Metodo.EMAIL) {
            if (comando.codigo() == null || comando.codigo().isBlank()) {
                return Optional.of(enviarCodigoEmail(usuario));
            }
            return Optional.of(validarCodigoEmail(usuario, comando.codigo()));
        }

        if (!totpService.validar(comando.codigo(), usuario.getTwoFactorSecret())) {
            usuario.registrarFalhaLogin();
            repositoryPort.atualizar(usuario);
            throw new IllegalArgumentException("Código inválido");
        }
        usuario.registrarLoginSucesso();
        repositoryPort.atualizar(usuario);
        return Optional.of(resultado(usuario, true, false, null));
    }

    private Resultado enviarCodigoEmail(CpcUsuario usuario) {
        String email = usuario.getEmailPreferencial();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail de recuperação não cadastrado");
        }
        String codigo = String.format("%08d", SECURE_RANDOM.nextInt(90_000_000) + 10_000_000);
        Instant expiraEm = Instant.now().plus(Duration.ofMinutes(VALIDADE_MINUTOS));
        usuario.definirCodigoEmail2FA(passwordEncoder.encode(codigo), expiraEm);
        repositoryPort.atualizar(usuario);
        emailRecuperacaoSenhaService.enviarCodigoRecuperacaoAsync(email, codigo);
        return resultado(usuario, false, true, expiraEm);
    }

    private Resultado validarCodigoEmail(CpcUsuario usuario, String codigo) {
        if (!usuario.isCodigoEmail2FAValido()) {
            throw new IllegalArgumentException("Código expirado ou não solicitado");
        }
        if (!passwordEncoder.matches(codigo, usuario.getTwoFactorEmailHash())) {
            usuario.registrarTentativaCodigoEmail2FA();
            repositoryPort.atualizar(usuario);
            throw new IllegalArgumentException("Código inválido");
        }
        usuario.limparCodigoEmail2FA();
        usuario.registrarLoginSucesso();
        repositoryPort.atualizar(usuario);
        return resultado(usuario, true, false, null);
    }

    private Resultado resultado(CpcUsuario usuario, boolean verificado, boolean enviado, Instant expiraEm) {
        return new Resultado(verificado, enviado, expiraEm,
                usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole() != null ? usuario.getRole().name() : null);
    }
}
