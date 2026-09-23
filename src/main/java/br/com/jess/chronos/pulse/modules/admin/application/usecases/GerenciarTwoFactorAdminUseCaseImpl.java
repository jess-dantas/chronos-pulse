package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminRecoveryCode;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.GerenciarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminRecoveryCodeRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.AdminRecoveryCodeService;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GerenciarTwoFactorAdminUseCaseImpl implements GerenciarTwoFactorAdminUseCase {

    private static final String EMISSOR = "Chronos Pulse";

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final AdminRecoveryCodeRepositoryPort recoveryCodeRepositoryPort;
    private final TotpService totpService;
    private final AdminRecoveryCodeService recoveryCodeService;

    @Autowired
    public GerenciarTwoFactorAdminUseCaseImpl(
            AdminPlataformaRepositoryPort repositoryPort,
            AdminRecoveryCodeRepositoryPort recoveryCodeRepositoryPort,
            TotpService totpService,
            AdminRecoveryCodeService recoveryCodeService) {
        this.repositoryPort = repositoryPort;
        this.recoveryCodeRepositoryPort = recoveryCodeRepositoryPort;
        this.totpService = totpService;
        this.recoveryCodeService = recoveryCodeService;
    }

    @Override
    public StatusResultado status(String adminId) {
        AdminPlataforma admin = buscar(adminId);
        return new StatusResultado(admin.isTwoFactorEnabled());
    }

    @Override
    public SetupResultado setup(String adminId) {
        AdminPlataforma admin = buscar(adminId);
        if (admin.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("2FA já está habilitado");
        }
        String segredo = totpService.gerarSegredo();
        admin.setTwoFactorSecret(segredo);
        admin.setTwoFactorEnabled(false);
        admin.setAtualizadoEm(Instant.now());
        repositoryPort.salvar(admin);

        String uri = totpService.gerarOtpauthUri(segredo, admin.getUsername(), EMISSOR);
        return new SetupResultado(segredo, uri);
    }

    @Override
    public ConfirmacaoResultado confirmar(String adminId, String codigo) {
        AdminPlataforma admin = buscar(adminId);
        if (admin.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("2FA já está habilitado");
        }
        if (admin.getTwoFactorSecret() == null || admin.getTwoFactorSecret().isBlank()) {
            throw new IllegalArgumentException("Execute a configuração primeiro");
        }
        if (!totpService.validar(codigo, admin.getTwoFactorSecret())) {
            throw new IllegalArgumentException("Código inválido");
        }
        admin.setTwoFactorEnabled(true);
        admin.setAtualizadoEm(Instant.now());
        repositoryPort.salvar(admin);

        // 8 códigos de recuperação, exibidos uma única vez.
        recoveryCodeRepositoryPort.removerPorAdmin(admin.getId());
        List<String> codigos = recoveryCodeService.gerarCodigos();
        List<AdminRecoveryCode> entidades = codigos.stream().map(bruto -> {
            AdminRecoveryCode entidade = new AdminRecoveryCode();
            entidade.setAdminId(admin.getId());
            entidade.setCodeHash(recoveryCodeService.hash(bruto));
            return entidade;
        }).toList();
        recoveryCodeRepositoryPort.salvarTodos(entidades);

        return new ConfirmacaoResultado(admin, codigos);
    }

    @Override
    public void desabilitar(String adminId, String codigo) {
        AdminPlataforma admin = buscar(adminId);
        if (!admin.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("2FA não está habilitado");
        }
        if (!totpService.validar(codigo, admin.getTwoFactorSecret())) {
            throw new IllegalArgumentException("Código inválido");
        }
        admin.setTwoFactorEnabled(false);
        admin.setTwoFactorSecret(null);
        admin.setAtualizadoEm(Instant.now());
        repositoryPort.salvar(admin);
    }

    private AdminPlataforma buscar(String adminId) {
        try {
            return repositoryPort.buscarPorId(UUID.fromString(adminId))
                    .orElseThrow(() -> new IllegalArgumentException("Admin não encontrado"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("Admin não encontrado")) {
                throw e;
            }
            throw new IllegalArgumentException("Admin não encontrado");
        }
    }
}
