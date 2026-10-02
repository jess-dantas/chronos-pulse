package br.com.jess.chronos.pulse.modules.auth.application.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.telemetria.application.LoginMetricsRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/// Monta o resultado completo do login do colaborador (tokens, módulos e
/// métrica de sucesso) — compartilhado pelo login com senha e pela
/// verificação do 2FA (TOTP ou OTP por e-mail).
@Service
@RequiredArgsConstructor
public class LoginSessionFactory {

    private final JwtService jwtService;
    private final ModulosPort modulosPort;
    private final EmpresaRepositoryPort empresaRepository;
    private final LoginMetricsRecorder loginMetricsRecorder;

    /// tempToken de 5 minutos entre a etapa de senha e o código 2FA.
    public String tempTokenDe(CpcUsuario usuario) {
        return jwtService.gerarTempTokenTwoFactorUsuario(usuario.getId().toString());
    }

    public AutenticarUsuarioUseCase.Resultado montar(CpcUsuario usuario) {
        String tenantId = usuario.getTenantId() != null ? usuario.getTenantId().toString() : null;
        String tenantSlug = usuario.getTenantId() != null
                ? empresaRepository.buscarPorId(usuario.getTenantId()).map(Empresa::getSlug).orElse(null)
                : null;
        String accessToken = jwtService.gerarAccessToken(
                usuario.getCpf(), usuario.getRole().name(),
                usuario.getCpcId().toString(), tenantId,
                usuario.isAcessoEstoque(), usuario.isAcessoPatrimonio(),
                usuario.isAcessoFrota(), usuario.isAcessoProtocolo());
        String refreshToken = jwtService.gerarRefreshToken(usuario.getCpf());
        List<String> modulos;
        if (usuario.getTenantId() == null) {
            modulos = Collections.emptyList();
        } else if (usuario.getRole() == Role.ADMIN_EMPRESA) {
            modulos = modulosPort.listarCodigosAtivos(usuario.getTenantId());
        } else if (usuario.getRole() == Role.GESTOR_RH) {
            // Gestor RH: escopo fixo do papel — só PONTO + RECURSOS_HUMANOS
            // (filtra pelos módulos ativos do tenant, ignorando vínculos).
            modulos = modulosPort.listarCodigosAtivos(usuario.getTenantId()).stream()
                    .filter(codigo -> "PONTO".equals(codigo) || "RECURSOS_HUMANOS".equals(codigo))
                    .toList();
        } else {
            modulos = modulosPort.listarCodigosDoUsuario(usuario.getId(), usuario.getTenantId());
        }

        loginMetricsRecorder.registrarSucesso(usuario.getTenantId(), usuario.getCpcId(),
                usuario.getRole().name());

        return new AutenticarUsuarioUseCase.Resultado(
                accessToken,
                refreshToken,
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
                modulos,
                false,
                null
        );
    }

    /// Resultado de login pendente de 2FA: dados básicos do usuário para a
    /// auditoria/UX e o tempToken que autentica a segunda etapa (5 min).
    public AutenticarUsuarioUseCase.Resultado pendente(CpcUsuario usuario, String tempToken) {
        String tenantId = usuario.getTenantId() != null ? usuario.getTenantId().toString() : null;
        return new AutenticarUsuarioUseCase.Resultado(
                null,
                null,
                usuario.getRole().name(),
                usuario.getCpf(),
                usuario.getCpcId().toString(),
                usuario.getNome(),
                usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
                tenantId,
                null,
                usuario.isAcessoEstoque(),
                usuario.isAcessoPatrimonio(),
                usuario.isAcessoFrota(),
                usuario.isAcessoProtocolo(),
                usuario.getFoto(),
                Collections.emptyList(),
                true,
                tempToken
        );
    }
}
