package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AlterarSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.service.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlterarSenhaAdminUseCaseImpl implements AlterarSenhaAdminUseCase {

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void executar(Comando comando) {
        AdminPlataforma admin;
        try {
            admin = repositoryPort.buscarPorId(UUID.fromString(comando.adminId()))
                    .orElseThrow(() -> new IllegalArgumentException("Admin não encontrado"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("Admin não encontrado")) {
                throw e;
            }
            throw new IllegalArgumentException("Admin não encontrado");
        }

        if (!passwordEncoder.matches(comando.senhaAtual(), admin.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual incorreta");
        }

        Optional<String> erro = PasswordPolicy.validar(comando.novaSenha(), Role.ADMIN_PLATAFORMA);
        if (erro.isPresent()) {
            throw new IllegalArgumentException(erro.get());
        }

        if (passwordEncoder.matches(comando.novaSenha(), admin.getSenhaHash())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da atual");
        }

        admin.setSenhaHash(passwordEncoder.encode(comando.novaSenha()));
        admin.setAtualizadoEm(Instant.now());
        repositoryPort.salvar(admin);
    }
}
