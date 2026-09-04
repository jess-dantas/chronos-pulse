package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RefreshTokenUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;

public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final JwtService jwtService;

    public RefreshTokenUseCaseImpl(CpcUsuarioRepositoryPort repositoryPort, JwtService jwtService) {
        this.repositoryPort = repositoryPort;
        this.jwtService = jwtService;
    }

    @Override
    public Resultado executar(Comando comando) {
        if (!jwtService.isTokenValido(comando.refreshToken())) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        String cpf = jwtService.extrairCpf(comando.refreshToken());

        CpcUsuario usuario = repositoryPort.buscarPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuário inativo");
        }

        String tenantId = usuario.getTenantId() != null ? usuario.getTenantId().toString() : null;
        String accessToken = jwtService.gerarAccessToken(
                usuario.getCpf(), usuario.getRole().name(),
                usuario.getCpcId().toString(), tenantId, usuario.isAcessoEstoque());

        return new Resultado(
                accessToken,
                usuario.getRole().name(),
                usuario.getCpcId().toString(),
                usuario.getNome(),
                usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
                tenantId,
                usuario.isAcessoEstoque());
    }
}
