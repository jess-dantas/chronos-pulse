package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.application.service.LoginSessionFactory;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.telemetria.application.LoginMetricsRecorder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AutenticarUsuarioUseCaseImpl implements AutenticarUsuarioUseCase {

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final LoginMetricsRecorder loginMetricsRecorder;
    private final LoginSessionFactory loginSessionFactory;

    public AutenticarUsuarioUseCaseImpl(CpcUsuarioRepositoryPort repositoryPort,
                                        PasswordEncoder passwordEncoder,
                                        LoginMetricsRecorder loginMetricsRecorder,
                                        LoginSessionFactory loginSessionFactory) {
        this.repositoryPort = repositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.loginMetricsRecorder = loginMetricsRecorder;
        this.loginSessionFactory = loginSessionFactory;
    }

    @Override
    public Resultado executar(Comando comando) {
        CpcUsuario usuario = repositoryPort.buscarPorCpf(comando.cpf())
                .orElseGet(() -> {
                    loginMetricsRecorder.registrarFalha(comando.cpf(), "USUARIO_NAO_ENCONTRADO", false, null, null);
                    return null;
                });

        if (usuario == null) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        if (usuario.isLoginBloqueado()) {
            loginMetricsRecorder.registrarFalha(comando.cpf(), "USUARIO_BLOQUEADO", true,
                    usuario.getTenantId(), usuario.getCpcId());
            throw new IllegalArgumentException("Muitas tentativas de login. Tente novamente em instantes.");
        }

        if (!usuario.isAtivo()) {
            loginMetricsRecorder.registrarFalha(comando.cpf(), "USUARIO_INATIVO", true,
                    usuario.getTenantId(), usuario.getCpcId());
            // Resposta genérica para não enumerar contas desativadas (M1).
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        // 2FA-first: sem senha, exige 2FA já habilitado e o tempToken
        // autentica a segunda etapa em /auth/2fa/verify.
        if (comando.senha() == null) {
            if (!usuario.isTwoFactorEnabled()) {
                loginMetricsRecorder.registrarFalha(comando.cpf(), "SENHA_INVALIDA", true,
                        usuario.getTenantId(), usuario.getCpcId());
                throw new IllegalArgumentException("Senha é obrigatória");
            }
            return loginSessionFactory.pendente(usuario, loginSessionFactory.tempTokenDe(usuario));
        }

        if (!passwordEncoder.matches(comando.senha(), usuario.getSenhaHash())) {
            usuario.registrarFalhaLogin();
            repositoryPort.atualizar(usuario);
            loginMetricsRecorder.registrarFalha(comando.cpf(), "SENHA_INVALIDA", true,
                    usuario.getTenantId(), usuario.getCpcId());
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        if (usuario.isTwoFactorEnabled()) {
            return loginSessionFactory.pendente(usuario, loginSessionFactory.tempTokenDe(usuario));
        }

        usuario.registrarLoginSucesso();
        repositoryPort.atualizar(usuario);

        return loginSessionFactory.montar(usuario);
    }
}
