package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AutenticarUsuarioUseCaseImpl implements AutenticarUsuarioUseCase {

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AutenticarUsuarioUseCaseImpl(CpcUsuarioRepositoryPort repositoryPort,
                                        JwtService jwtService,
                                        PasswordEncoder passwordEncoder) {
        this.repositoryPort = repositoryPort;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Resultado executar(Comando comando) {
        CpcUsuario usuario = repositoryPort.buscarPorCpf(comando.cpf())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuário inativo");
        }

        if (!passwordEncoder.matches(comando.senha(), usuario.getSenhaHash())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        String tenantId = usuario.getTenantId() != null ? usuario.getTenantId().toString() : null;
        String accessToken = jwtService.gerarAccessToken(
                usuario.getCpf(), usuario.getRole().name(),
                usuario.getCpcId().toString(), tenantId, usuario.isAcessoEstoque());
        String refreshToken = jwtService.gerarRefreshToken(usuario.getCpf());

        return new Resultado(
                accessToken,
                refreshToken,
                usuario.getRole().name(),
                usuario.getCpcId().toString(),
                usuario.getNome(),
                usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
                tenantId,
                usuario.isAcessoEstoque()
        );
    }
}
