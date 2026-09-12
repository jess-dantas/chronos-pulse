package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RefreshTokenUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;

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

        String cpf = jwtService.extrairCpf(comando.refreshToken());

        CpcUsuario usuario = repositoryPort.buscarPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new IllegalStateException("Usuário inativo");
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
