package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RefreshTokenUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;

import java.util.Collections;
import java.util.List;

public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final JwtService jwtService;
    private final ModulosPort modulosPort;

    public RefreshTokenUseCaseImpl(CpcUsuarioRepositoryPort repositoryPort,
                                   JwtService jwtService,
                                   ModulosPort modulosPort) {
        this.repositoryPort = repositoryPort;
        this.jwtService = jwtService;
        this.modulosPort = modulosPort;
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
        List<String> modulos = usuario.getTenantId() != null
                ? modulosPort.listarCodigosAtivos(usuario.getTenantId())
                : Collections.emptyList();

        return new Resultado(
                accessToken,
                usuario.getRole().name(),
                usuario.getCpcId().toString(),
                usuario.getNome(),
                usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
                tenantId,
                usuario.isAcessoEstoque(),
                usuario.getFoto(),
                modulos);
    }
}
