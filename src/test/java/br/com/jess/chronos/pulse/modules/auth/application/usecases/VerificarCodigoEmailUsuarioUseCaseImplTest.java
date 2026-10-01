package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.application.service.LoginSessionFactory;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.VerificarCodigoEmailUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.telemetria.application.LoginMetricsRecorder;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarCodigoEmailUsuarioUseCaseImplTest {

    @Mock
    private CpcUsuarioRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ModulosPort modulosPort;

    @Mock
    private EmpresaRepositoryPort empresaRepository;

    @Mock
    private LoginMetricsRecorder loginMetricsRecorder;

    private VerificarCodigoEmailUsuarioUseCaseImpl useCase;

    private CpcUsuario usuario;
    private Claims claims;

    @BeforeEach
    void setUp() {
        LoginSessionFactory loginSessionFactory = new LoginSessionFactory(
                jwtService, modulosPort, empresaRepository, loginMetricsRecorder);
        useCase = new VerificarCodigoEmailUsuarioUseCaseImpl(repositoryPort, passwordEncoder,
                jwtService, loginSessionFactory);
        usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", "colab@empresa.com", "hash", Role.COLABORADOR, null);
        claims = mock(Claims.class);
    }

    private void prepararToken() {
        when(jwtService.extrairClaims("temp")).thenReturn(claims);
        when(jwtService.isTwoFactorToken(claims)).thenReturn(true);
        when(claims.get("usuarioId", String.class)).thenReturn(usuario.getId().toString());
        when(repositoryPort.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
    }

    private void prepararTokensFinais() {
        when(jwtService.gerarAccessToken(eq("12345678901"), eq("COLABORADOR"), eq(usuario.getCpcId().toString()),
                isNull(), eq(false), eq(false), eq(false), eq(false))).thenReturn("access");
        when(jwtService.gerarRefreshToken("12345678901")).thenReturn("refresh");
    }

    @Test
    void deveAutenticarComCodigoValido() {
        usuario.definirCodigoEmail2FA("hash-codigo", Instant.now().plus(Duration.ofMinutes(10)));
        prepararToken();
        when(passwordEncoder.matches("123456", "hash-codigo")).thenReturn(true);
        prepararTokensFinais();

        var resultado = useCase.executar(
                new VerificarCodigoEmailUsuarioUseCase.Comando("temp", "123456"));

        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(usuario.isCodigoEmail2FAValido()).isFalse();
        assertThat(usuario.getTwoFactorEmailTentativas()).isZero();
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void deveContarTentativaEmCodigoInvalido() {
        usuario.definirCodigoEmail2FA("hash-codigo", Instant.now().plus(Duration.ofMinutes(10)));
        prepararToken();
        when(passwordEncoder.matches("000000", "hash-codigo")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(
                new VerificarCodigoEmailUsuarioUseCase.Comando("temp", "000000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        assertThat(usuario.getTwoFactorEmailTentativas()).isEqualTo(1);
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void deveRecusarCodigoExpiradoOuNaoSolicitado() {
        prepararToken();

        assertThatThrownBy(() -> useCase.executar(
                new VerificarCodigoEmailUsuarioUseCase.Comando("temp", "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirado ou não solicitado");

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void deveBloquearContaBloqueada() {
        usuario.atualizarControleAcesso(Instant.now(), 5, Instant.now().plusSeconds(600));
        prepararToken();

        assertThatThrownBy(() -> useCase.executar(
                new VerificarCodigoEmailUsuarioUseCase.Comando("temp", "123456")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloqueada");
    }

    @Test
    void deveRecusarTempTokenExpirado() {
        when(jwtService.extrairClaims("temp")).thenThrow(new RuntimeException("expired"));

        assertThatThrownBy(() -> useCase.executar(
                new VerificarCodigoEmailUsuarioUseCase.Comando("temp", "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sessão expirada");
    }
}
