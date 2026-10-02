package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.application.service.LoginSessionFactory;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.VerificarTwoFactorUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificarTwoFactorUsuarioUseCaseImpl implements VerificarTwoFactorUsuarioUseCase {

    private final CpcUsuarioRepositoryPort repositoryPort;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final LoginSessionFactory loginSessionFactory;

    @Override
    public AutenticarUsuarioUseCase.Resultado executar(Comando comando) {
        CpcUsuario usuario = extrairUsuario(comando.tempToken());

        if (!usuario.isTwoFactorEnabled()) {
            throw new IllegalArgumentException("2FA não está habilitado");
        }
        if (usuario.isLoginBloqueado()) {
            throw new IllegalStateException("Conta temporariamente bloqueada por excesso de tentativas");
        }
        if (!totpService.validar(comando.codigo(), usuario.getTwoFactorSecret())) {
            usuario.registrarFalhaLogin();
            repositoryPort.atualizar(usuario);
            throw new IllegalArgumentException("Código inválido");
        }

        usuario.registrarLoginSucesso();
        repositoryPort.atualizar(usuario);
        return loginSessionFactory.montar(usuario);
    }

    private CpcUsuario extrairUsuario(String tempToken) {
        Claims claims;
        try {
            claims = jwtService.extrairClaims(tempToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Sessão expirada. Refaça o login.");
        }
        if (!jwtService.isTwoFactorToken(claims)) {
            throw new IllegalArgumentException("Token inválido");
        }
        String usuarioId = claims.get("usuarioId", String.class);
        if (usuarioId == null) {
            throw new IllegalArgumentException("Token inválido");
        }
        CpcUsuario usuario = repositoryPort.buscarPorId(UUID.fromString(usuarioId))
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));
        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }
        return usuario;
    }
}
