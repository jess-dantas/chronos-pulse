package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RedefinirSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.service.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedefinirSenhaAdminUseCaseImpl implements RedefinirSenhaAdminUseCase {

    private final AdminPlataformaRepositoryPort repositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void executar(Comando comando) {
        AdminPlataforma admin = repositoryPort.buscarPorUsername(comando.username())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!admin.isAtivo()) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        if (!admin.isCodigoEmailValido()) {
            throw new IllegalArgumentException("Código expirado ou não solicitado");
        }
        if (!passwordEncoder.matches(comando.codigo(), admin.getRecuperacaoEmailHash())) {
            admin.registrarTentativaCodigoEmail();
            repositoryPort.salvar(admin);
            throw new IllegalArgumentException("Código inválido");
        }

        PasswordPolicy.validar(comando.novaSenha(), Role.ADMIN_PLATAFORMA)
                .ifPresent(mensagem -> {
                    throw new IllegalArgumentException(mensagem);
                });
        if (passwordEncoder.matches(comando.novaSenha(), admin.getSenhaHash())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da atual");
        }

        admin.limparCodigoEmail();
        admin.setSenhaHash(passwordEncoder.encode(comando.novaSenha()));
        admin.setAtualizadoEm(Instant.now());
        repositoryPort.salvar(admin);
    }
}
