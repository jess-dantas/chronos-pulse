package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import io.jsonwebtoken.Claims;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RefreshTokenUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final JwtService jwtService;
    private final ModulosPort modulosPort;
    private final EmpresaRepositoryPort empresaRepository;

    public RefreshTokenUseCaseImpl(CpcUsuarioRepositoryPort repositoryPort,
                                   JwtService jwtService,
                                   ModulosPort modulosPort,
                                   EmpresaRepositoryPort empresaRepository) {
        this.repositoryPort = repositoryPort;
        this.jwtService = jwtService;
        this.modulosPort = modulosPort;
        this.empresaRepository = empresaRepository;
    }

    @Override
    public Resultado executar(Comando comando) {
        if (!jwtService.isTokenValido(comando.refreshToken())) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        var claims = jwtService.extrairClaims(comando.refreshToken());
        // Access tokens não valem como refresh (LOW - security review).
        if (!jwtService.isRefreshToken(claims)) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        String cpf = claims.getSubject();

        CpcUsuario usuario = repositoryPort.buscarPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido ou expirado"));

        // Revogação (H2): usuário inativo ou token emitido antes da última troca
        // de senha não renovam a sessão.
        Instant iat = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null;
        if (!usuario.isAtivo()
                || (usuario.getSenhaAlteradaEm() != null && iat != null
                    && iat.isBefore(usuario.getSenhaAlteradaEm()))) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        String tenantId = usuario.getTenantId() != null ? usuario.getTenantId().toString() : null;
        String tenantSlug = usuario.getTenantId() != null
                ? empresaRepository.buscarPorId(usuario.getTenantId()).map(Empresa::getSlug).orElse(null)
                : null;
        String accessToken = jwtService.gerarAccessToken(
                usuario.getCpf(), usuario.getRole().name(),
                usuario.getCpcId().toString(), tenantId,
                usuario.isAcessoEstoque(), usuario.isAcessoPatrimonio(),
                usuario.isAcessoFrota(), usuario.isAcessoProtocolo());
        List<String> modulos = usuario.getTenantId() != null
                ? modulosPort.listarCodigosAtivos(usuario.getTenantId())
                : Collections.emptyList();

        return new Resultado(
                accessToken,
                usuario.getRole().name(),
                usuario.getCpf(),
                usuario.getCpcId().toString(),
                usuario.getNome(),
                usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
tenantId,
                tenantSlug,
                usuario.isAcessoEstoque(),
                usuario.isAcessoPatrimonio(),
                usuario.isAcessoFrota(),
                usuario.isAcessoProtocolo(),
                usuario.getFoto(),
                modulos);
    }
}
