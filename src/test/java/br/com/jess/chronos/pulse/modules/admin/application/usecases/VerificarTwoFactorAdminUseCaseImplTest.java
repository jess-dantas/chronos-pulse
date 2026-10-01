package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarTwoFactorAdminUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarTwoFactorAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private JwtService jwtService;

    @Mock
    private TotpService totpService;

    private VerificarTwoFactorAdminUseCaseImpl useCase;

    private AdminPlataforma admin;
    private Claims claims;

    @BeforeEach
    void setUp() {
        useCase = new VerificarTwoFactorAdminUseCaseImpl(repositoryPort, jwtService, totpService);
        admin = AdminPlataforma.builder()
                .id(UUID.randomUUID())
                .username("Administrator")
                .senhaHash("hash")
                .twoFactorEnabled(true)
                .twoFactorSecret("SECRET")
                .ativo(true)
                .build();
        claims = mock(Claims.class);
    }

    private void prepararToken() {
        when(jwtService.extrairClaims("temp")).thenReturn(claims);
        when(jwtService.isTwoFactorToken(claims)).thenReturn(true);
        when(claims.get("adminId", String.class)).thenReturn(admin.getId().toString());
        when(repositoryPort.buscarPorId(admin.getId())).thenReturn(Optional.of(admin));
    }

    @Test
    void deveEmitirTokensComCodigoValido() {
        prepararToken();
        when(totpService.validar("123456", "SECRET")).thenReturn(true);
        when(jwtService.gerarAccessTokenAdmin("Administrator", admin.getId().toString())).thenReturn("access");
        when(jwtService.gerarRefreshTokenAdmin("Administrator", admin.getId().toString())).thenReturn("refresh");

        var resultado = useCase.executar(new Comando("temp", "123456"));

        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(resultado.refreshToken()).isEqualTo("refresh");
        assertThat(admin.getTentativasLoginFalhas()).isZero();
    }

    @Test
    void deveContarFalhaDeTotp() {
        prepararToken();
        when(totpService.validar("000000", "SECRET")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new Comando("temp", "000000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        assertThat(admin.getTentativasLoginFalhas()).isEqualTo(1);
        verify(repositoryPort).salvar(any(AdminPlataforma.class));
    }

    @Test
    void deveBloquearContaBloqueada() {
        prepararToken();
        admin.setBloqueioLoginAte(java.time.Instant.now().plusSeconds(600));

        assertThatThrownBy(() -> useCase.executar(new Comando("temp", "123456")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloqueada");
        verifyNoInteractions(totpService);
    }

    @Test
    void deveRecusarTempTokenExpirado() {
        when(jwtService.extrairClaims("temp")).thenThrow(new RuntimeException("expired"));

        assertThatThrownBy(() -> useCase.executar(new Comando("temp", "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sessão expirada");
    }
}
