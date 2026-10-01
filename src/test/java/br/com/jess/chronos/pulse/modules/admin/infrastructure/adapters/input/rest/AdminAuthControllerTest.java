package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AlterarSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AutenticarAdminPlataformaUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.BootstrapAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.EnviarCodigoEmailAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.GerenciarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RecuperarAcessoAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RefreshAdminTokenUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarCodigoEmailAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminRefreshRequestDTO;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    private RefreshAdminTokenUseCase refreshAdminTokenUseCase;

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
}
