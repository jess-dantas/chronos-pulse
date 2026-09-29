package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AlterarSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AutenticarAdminPlataformaUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.BootstrapAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.GerenciarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RecuperarAcessoAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarTwoFactorAdminUseCase;
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
    private JwtService jwtService;

    @InjectMocks
    private AdminAuthController controller;

    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        Claims claims = mock(Claims.class);
        when(claims.get("adminId", String.class)).thenReturn(adminId.toString());
        when(jwtService.extrairClaims("token")).thenReturn(claims);
    }

    @Test
    void deveDesabilitarDoisFatoresIndependenteDaFlagDeObrigatoriedade() {
        ResponseEntity<Void> response =
                controller.twoFactorDisable("Bearer token", new AdminTwoFactorCodigoRequestDTO("123456"));

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(gerenciarTwoFactorAdminUseCase).desabilitar(adminId.toString(), "123456");
    }
}
