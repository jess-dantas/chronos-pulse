package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminRecoveryCode;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RecuperarAcessoAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminRecoveryCodeRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.AdminRecoveryCodeService;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecuperarAcessoAdminUseCaseImpl implements RecuperarAcessoAdminUseCase {

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final AdminRecoveryCodeRepositoryPort recoveryCodeRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AdminRecoveryCodeService recoveryCodeService;

    @Override
    public Resultado executar(Comando comando) {
        AdminPlataforma admin = repositoryPort.buscarPorUsername(comando.username())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!admin.isAtivo()) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        if (!passwordEncoder.matches(comando.senha(), admin.getSenhaHash())) {
            admin.registrarFalhaLogin();
            repositoryPort.salvar(admin);
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        if (admin.isLoginBloqueado()) {
            throw new IllegalStateException("Conta temporariamente bloqueada por excesso de tentativas");
        }

        List<AdminRecoveryCode> codigos = recoveryCodeRepositoryPort.listarPorAdmin(admin.getId());
        AdminRecoveryCode usado = codigos.stream()
                .filter(c -> !c.isUsado() && recoveryCodeService.confere(comando.recoveryCode(), c.getCodeHash()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Código de recuperação inválido"));
        usado.marcarComoUsado();
        recoveryCodeRepositoryPort.salvar(usado);

        recoveryCodeRepositoryPort.removerPorAdmin(admin.getId());
        List<String> novosCodigos = recoveryCodeService.gerarCodigos();
        List<AdminRecoveryCode> novasEntidades = novosCodigos.stream().map(codigo -> {
            AdminRecoveryCode entidade = new AdminRecoveryCode();
            entidade.setAdminId(admin.getId());
            entidade.setCodeHash(recoveryCodeService.hash(codigo));
            return entidade;
        }).toList();
        recoveryCodeRepositoryPort.salvarTodos(novasEntidades);

        admin.registrarLoginSucesso();
        admin.setUltimoLogin(Instant.now());
        repositoryPort.salvar(admin);

        String accessToken = jwtService.gerarAccessTokenAdmin(admin.getUsername(), admin.getId().toString());
        String refreshToken = jwtService.gerarRefreshTokenAdmin(admin.getUsername(), admin.getId().toString());

        return new Resultado(admin, accessToken, refreshToken, novosCodigos);
    }
}
