package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AutenticarAdminPlataformaUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AutenticarAdminPlataformaUseCaseImpl implements AutenticarAdminPlataformaUseCase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AutenticarAdminPlataformaUseCaseImpl.class);

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final boolean twoFactorRequired;

    @Autowired
    public AutenticarAdminPlataformaUseCaseImpl(
            AdminPlataformaRepositoryPort repositoryPort,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${chronos.admin.two-factor-required:true}") boolean twoFactorRequired) {
        this.repositoryPort = repositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.twoFactorRequired = twoFactorRequired;
    }

    @Override
    public Resultado executar(Comando comando) {
        AdminPlataforma admin = repositoryPort.buscarPorUsername(comando.username())
                .orElseThrow(() -> {
                    log.warn("Falha de login admin: usuário não encontrado (username={})", comando.username());
                    return new IllegalArgumentException("Credenciais inválidas");
                });

        if (!admin.isAtivo()) {
            log.warn("Falha de login admin: conta desativada (username={})", comando.username());
            throw new IllegalStateException("Conta desativada");
        }

        if (!passwordEncoder.matches(comando.senha(), admin.getSenhaHash())) {
            log.warn("Falha de login admin: senha inválida (username={})", comando.username());
            admin.registrarFalhaLogin();
            repositoryPort.salvar(admin);
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        if (admin.isLoginBloqueado()) {
            log.warn("Falha de login admin: conta bloqueada por excesso de tentativas (username={})", comando.username());
            throw new IllegalStateException("Conta temporariamente bloqueada por excesso de tentativas");
        }

        if (admin.isTwoFactorEnabled()) {
            String tempToken = jwtService.gerarTempTokenTwoFactor(admin.getId().toString());
            return new Resultado(admin, null, null, true, tempToken, false);
        }

        if (twoFactorRequired) {
            // 2FA obrigatório e ainda não configurado: força o wizard
            // (/2fa/setup + /2fa/confirm) antes de emitir tokens finais.
            String tempToken = jwtService.gerarTempTokenTwoFactor(admin.getId().toString());
            return new Resultado(admin, null, null, true, tempToken, true);
        }

        admin.registrarLoginSucesso();
        admin.setUltimoLogin(java.time.Instant.now());
        repositoryPort.salvar(admin);

        String accessToken = jwtService.gerarAccessTokenAdmin(admin.getUsername(), admin.getId().toString());
        String refreshToken = jwtService.gerarRefreshTokenAdmin(admin.getUsername(), admin.getId().toString());

        return new Resultado(admin, accessToken, refreshToken, false, null, false);
    }
}