package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.BootstrapAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class BootstrapAdminUseCaseImpl implements BootstrapAdminUseCase {

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public BootstrapAdminUseCaseImpl(
            AdminPlataformaRepositoryPort repositoryPort,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.repositoryPort = repositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public boolean disponivel() {
        return repositoryPort.count() == 0;
    }

    @Override
    @Transactional
    public Resultado executar(Comando comando) {
        if (repositoryPort.count() > 0) {
            throw new IllegalStateException("Provisionamento já concluído");
        }
        if (repositoryPort.existsByUsername(comando.username())) {
            throw new IllegalArgumentException("Username já existe");
        }
        if (comando.email() != null && repositoryPort.buscarPorEmail(comando.email()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        AdminPlataforma admin = AdminPlataforma.builder()
                .username(comando.username())
                .senhaHash(passwordEncoder.encode(comando.senha()))
                .nomeCompleto(comando.nomeCompleto())
                .email(comando.email())
                .twoFactorEnabled(false)
                .criadoEm(Instant.now())
                .build();
        admin = repositoryPort.salvar(admin);

        String tempToken = jwtService.gerarTempTokenTwoFactor(admin.getId().toString());
        return new Resultado(tempToken);
    }
}
