package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import br.com.jess.chronos.pulse.modules.auth.application.service.LoginSessionFactory;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.VerificarTwoFactorUsuarioUseCase;
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

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarTwoFactorUsuarioUseCaseImplTest {

    @Mock
    private CpcUsuarioRepositoryPort repositoryPort;

    @Mock
    private JwtService jwtService;

    @Mock
    private TotpService totpService;

    @Mock
    private ModulosPort modulosPort;

    @Mock
    private EmpresaRepositoryPort empresaRepository;

    @Mock
    private LoginMetricsRecorder loginMetricsRecorder;

    private VerificarTwoFactorUsuarioUseCaseImpl useCase;

    private CpcUsuario usuario;
    private Claims claims;

    @BeforeEach
    void setUp() {
        LoginSessionFactory loginSessionFactory = new LoginSessionFactory(
                jwtService, modulosPort, empresaRepository, loginMetricsRecorder);
        useCase = new VerificarTwoFactorUsuarioUseCaseImpl(repositoryPort, jwtService,
                totpService, loginSessionFactory);
        usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", "colab@empresa.com", "hash", Role.COLABORADOR, null);
        usuario.setTwoFactorEnabled(true);
        usuario.setTwoFactorSecret("SECRET");
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
    void deveEmitirTokensComCodigoValido() {
        prepararToken();
        when(totpService.validar("123456", "SECRET")).thenReturn(true);
        prepararTokensFinais();

        var resultado = useCase.executar(new VerificarTwoFactorUsuarioUseCase.Comando("temp", "123456"));

        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(resultado.refreshToken()).isEqualTo("refresh");
        assertThat(resultado.requiresTwoFactor()).isFalse();
        assertThat(usuario.getTentativasLoginFalhas()).isZero();
        verify(repositoryPort).atualizar(usuario);
        verify(loginMetricsRecorder).registrarSucesso(null, usuario.getCpcId(), "COLABORADOR");
    }

    @Test
    void deveContarFalhaDeCodigoInvalido() {
        prepararToken();
        when(totpService.validar("000000", "SECRET")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(
                new VerificarTwoFactorUsuarioUseCase.Comando("temp", "000000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        assertThat(usuario.getTentativasLoginFalhas()).isEqualTo(1);
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void deveRecusarQuandoDoisFatorDesabilitado() {
        usuario.setTwoFactorEnabled(false);
        prepararToken();

        assertThatThrownBy(() -> useCase.executar(
                new VerificarTwoFactorUsuarioUseCase.Comando("temp", "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não está habilitado");

        verifyNoInteractions(totpService);
    }

    @Test
    void deveBloquearContaBloqueada() {
        usuario.atualizarControleAcesso(null, 5, Instant.now().plusSeconds(600));
        prepararToken();

        assertThatThrownBy(() -> useCase.executar(
                new VerificarTwoFactorUsuarioUseCase.Comando("temp", "123456")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloqueada");

        verifyNoInteractions(totpService);
    }

    @Test
    void deveRecusarTempTokenExpirado() {
        when(jwtService.extrairClaims("temp")).thenThrow(new RuntimeException("expired"));

        assertThatThrownBy(() -> useCase.executar(
                new VerificarTwoFactorUsuarioUseCase.Comando("temp", "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sessão expirada");
    }
}
