package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.admin.application.service.AdminDeviceTokenService;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AlterarSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AutenticarAdminPlataformaUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.BootstrapAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.EnviarCodigoEmailAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.GerenciarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RecuperarAcessoAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RefreshAdminTokenUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RedefinirSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.SolicitarResetSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarCodigoEmailAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminDispositivoRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminLoginRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminRefreshRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminResetSenhaEnviarRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminResetSenhaVerificarRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorCodigoRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthControllerTest {

    @Mock
    private AutenticarAdminPlataformaUseCase autenticarAdminPlataformaUseCase;

    @Mock
    private VerificarTwoFactorAdminUseCase verificarTwoFactorAdminUseCase;

    @Mock
    private GerenciarTwoFactorAdminUseCase gerenciarTwoFactorAdminUseCase;

    @Mock
    private AlterarSenhaAdminUseCase alterarSenhaAdminUseCase;

    @Mock
    private BootstrapAdminUseCase bootstrapAdminUseCase;

    @Mock
    private RecuperarAcessoAdminUseCase recuperarAcessoAdminUseCase;

    @Mock
    private EnviarCodigoEmailAdminUseCase enviarCodigoEmailAdminUseCase;

    @Mock
    private VerificarCodigoEmailAdminUseCase verificarCodigoEmailAdminUseCase;

    @Mock
    private SolicitarResetSenhaAdminUseCase solicitarResetSenhaAdminUseCase;

    @Mock
    private RedefinirSenhaAdminUseCase redefinirSenhaAdminUseCase;

    @Mock
    private RefreshAdminTokenUseCase refreshAdminTokenUseCase;

    @Mock
    private AdminDeviceTokenService adminDeviceTokenService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AdminAuthController controller;

    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        Claims claims = mock(Claims.class);
        lenient().when(claims.get("adminId", String.class)).thenReturn(adminId.toString());
        lenient().when(jwtService.extrairClaims("token")).thenReturn(claims);
    }

    @Test
    void deveDesabilitarDoisFatoresIndependenteDaFlagDeObrigatoriedade() {
        ResponseEntity<Void> response =
                controller.twoFactorDisable("Bearer token", new AdminTwoFactorCodigoRequestDTO("123456"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(gerenciarTwoFactorAdminUseCase).desabilitar(adminId.toString(), "123456");
    }

    @Test
    void deveDelegarRefreshDoAdmin() {
        AdminPlataforma admin = AdminPlataforma.builder()
                .id(adminId).username("Administrator").build();
        when(refreshAdminTokenUseCase.executar(any()))
                .thenReturn(new RefreshAdminTokenUseCase.Resultado(admin, "access", "refresh"));

        var response = controller.refresh(new AdminRefreshRequestDTO("rt"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getAccessToken()).isEqualTo("access");
        assertThat(response.getBody().getRefreshToken()).isEqualTo("refresh");
        verify(refreshAdminTokenUseCase)
                .executar(new RefreshAdminTokenUseCase.Comando("rt"));
    }

    @Test
    void deveDelegarEnvioDeCodigoPorEmail() {
        var response = controller.sendEmailCode(
                new br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest
                        .dto.AdminEmailCodigoRequestDTO("temp"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(enviarCodigoEmailAdminUseCase)
                .executar(new EnviarCodigoEmailAdminUseCase.Comando("temp"));
    }

    @Test
    void deveDelegarSolicitacaoDeResetDeSenha() {
        var response = controller.resetSenhaEnviar(
                new AdminResetSenhaEnviarRequestDTO("Administrator"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(solicitarResetSenhaAdminUseCase)
                .executar(new SolicitarResetSenhaAdminUseCase.Comando("Administrator"));
    }

    @Test
    void deveDelegarVerificacaoDeResetDeSenha() {
        var response = controller.resetSenhaVerificar(
                new AdminResetSenhaVerificarRequestDTO("Administrator", "12345678", "Nova@1234"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(redefinirSenhaAdminUseCase).executar(
                new RedefinirSenhaAdminUseCase.Comando("Administrator", "12345678", "Nova@1234"));
    }

    // --- biometria-first: dispositivo confiável (deviceToken) ---

    @Test
    void loginDevePassarDeviceTokenParaOCasoDeUso() {
        var admin = AdminPlataforma.builder().id(adminId).username("Administrator").build();
        when(autenticarAdminPlataformaUseCase.executar(any()))
                .thenReturn(new AutenticarAdminPlataformaUseCase.Resultado(
                        admin, "access", "refresh", false, null, false));

        var response = controller.login(
                new AdminLoginRequestDTO("Administrator", "admin1234", "device-token"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(autenticarAdminPlataformaUseCase).executar(
                new AutenticarAdminPlataformaUseCase.Comando(
                        "Administrator", "admin1234", "device-token"));
    }

    @Test
    void loginSemDeviceTokenDeveUsarComandoDeDoisArgs() {
        var admin = AdminPlataforma.builder().id(adminId).username("Administrator").build();
        when(autenticarAdminPlataformaUseCase.executar(any()))
                .thenReturn(new AutenticarAdminPlataformaUseCase.Resultado(
                        admin, null, null, true, "temp-token", false));

        var response = controller.login(
                new AdminLoginRequestDTO("Administrator", "admin1234", null));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().getTempToken()).isEqualTo("temp-token");
        verify(autenticarAdminPlataformaUseCase).executar(
                new AutenticarAdminPlataformaUseCase.Comando("Administrator", "admin1234"));
    }

    @Test
    void dispositivoVincularDeveDevolverTokenCruUmaUnicaVez() {
        when(adminDeviceTokenService.vincular(eq(adminId), eq("MacBook")))
                .thenReturn(new AdminDeviceTokenService.VinculoAdminDeviceToken(
                        "valor-cru", Instant.now().plusSeconds(2592000L)));

        var response = controller.dispositivoVincular(
                "Bearer token", new AdminDispositivoRequestDTO("MacBook"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody().deviceToken()).isEqualTo("valor-cru");
        assertThat(response.getBody().expiraEm()).isAfter(Instant.now());
        verify(adminDeviceTokenService).vincular(adminId, "MacBook");
    }

    @Test
    void dispositivoVincularDeveRecusarTempTokenDe2FA() {
        when(jwtService.isTwoFactorToken(any(Claims.class))).thenReturn(true);

        assertThatThrownBy(() -> controller.dispositivoVincular("Bearer token", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token inválido");
        verifyNoInteractions(adminDeviceTokenService);
    }

    @Test
    void dispositivoRevogarDeveRevogarTodosOsVinculos() {
        var response = controller.dispositivoRevogar("Bearer token");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(adminDeviceTokenService).revogarTodos(adminId);
    }
}
