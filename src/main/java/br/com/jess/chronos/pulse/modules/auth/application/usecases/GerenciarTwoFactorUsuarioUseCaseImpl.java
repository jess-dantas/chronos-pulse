package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.GerenciarTwoFactorUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GerenciarTwoFactorUsuarioUseCaseImpl implements GerenciarTwoFactorUsuarioUseCase {

    private static final String EMISSOR = "Chronos Pulse";

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final TotpService totpService;

    @Override
    public StatusResultado status(String usuarioId) {
        return new StatusResultado(buscar(usuarioId).isTwoFactorEnabled());
    }

    @Override
    public SetupResultado setup(String usuarioId) {
        CpcUsuario usuario = buscar(usuarioId);
        if (usuario.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("2FA já está habilitado");
        }
        String segredo = totpService.gerarSegredo();
        usuario.setTwoFactorSecret(segredo);
        repositoryPort.atualizar(usuario);

        String uri = totpService.gerarOtpauthUri(segredo, usuario.getCpf(), EMISSOR);
        return new SetupResultado(segredo, uri);
    }

    @Override
    public void confirmar(String usuarioId, String codigo) {
        CpcUsuario usuario = buscar(usuarioId);
        if (usuario.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("2FA já está habilitado");
        }
        if (usuario.getTwoFactorSecret() == null || usuario.getTwoFactorSecret().isBlank()) {
            throw new IllegalArgumentException("Execute a configuração primeiro");
        }
        if (!totpService.validar(codigo, usuario.getTwoFactorSecret())) {
            throw new IllegalArgumentException("Código inválido");
        }
        usuario.setTwoFactorEnabled(true);
        repositoryPort.atualizar(usuario);
    }

    @Override
    public void desabilitar(String usuarioId, String codigo) {
        CpcUsuario usuario = buscar(usuarioId);
        if (!usuario.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("2FA não está habilitado");
        }
        if (!totpService.validar(codigo, usuario.getTwoFactorSecret())) {
            throw new IllegalArgumentException("Código inválido");
        }
        usuario.setTwoFactorEnabled(false);
        usuario.setTwoFactorSecret(null);
        usuario.limparCodigoEmail2FA();
        repositoryPort.atualizar(usuario);
    }

    private CpcUsuario buscar(String usuarioId) {
        try {
            return repositoryPort.buscarPorId(UUID.fromString(usuarioId))
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("Usuário não encontrado")) {
                throw e;
            }
            throw new IllegalArgumentException("Usuário não encontrado");
        }
    }
}
