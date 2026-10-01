package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RefreshAdminTokenUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshAdminTokenUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private JwtService jwtService;

    private RefreshAdminTokenUseCaseImpl useCase;

    private AdminPlataforma admin;
    private Claims claims;

    @BeforeEach
    void setUp() {
        useCase = new RefreshAdminTokenUseCaseImpl(repositoryPort, jwtService);
        admin = AdminPlataforma.builder()
                .id(UUID.randomUUID())
                .username("Administrator")
                .senhaHash("hash")
                .ativo(true)
                .build();
        claims = mock(Claims.class);
    }

    private void prepararRefreshValido() {
        when(jwtService.isTokenValido("rt")).thenReturn(true);
        when(jwtService.extrairClaims("rt")).thenReturn(claims);
        when(jwtService.isRefreshToken(claims)).thenReturn(true);
        when(claims.get("adminId", String.class)).thenReturn(admin.getId().toString());
        when(repositoryPort.buscarPorId(admin.getId())).thenReturn(Optional.of(admin));
    }

    @Test
    void deveRotacionarTokens() {
        prepararRefreshValido();
        when(jwtService.gerarAccessTokenAdmin("Administrator", admin.getId().toString())).thenReturn("access");
        when(jwtService.gerarRefreshTokenAdmin("Administrator", admin.getId().toString())).thenReturn("refresh");

        var resultado = useCase.executar(new Comando("rt"));

        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(resultado.refreshToken()).isEqualTo("refresh");
        assertThat(resultado.admin()).isEqualTo(admin);
    }

    @Test
    void deveRecusarTokenInvalido() {
        when(jwtService.isTokenValido("rt")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new Comando("rt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token inválido");
    }

    @Test
    void deveRecusarAccessToken() {
        when(jwtService.isTokenValido("rt")).thenReturn(true);
        when(jwtService.extrairClaims("rt")).thenReturn(claims);
        when(jwtService.isRefreshToken(claims)).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new Comando("rt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token inválido");
        verifyNoInteractions(repositoryPort);
    }

    @Test
    void deveRecusarTokenSemAdminId() {
        when(jwtService.isTokenValido("rt")).thenReturn(true);
        when(jwtService.extrairClaims("rt")).thenReturn(claims);
        when(jwtService.isRefreshToken(claims)).thenReturn(true);
        when(claims.get("adminId", String.class)).thenReturn(null);

        assertThatThrownBy(() -> useCase.executar(new Comando("rt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token inválido");
        verifyNoInteractions(repositoryPort);
    }

    @Test
    void deveRecusarAdminInativo() {
        prepararRefreshValido();
        admin.setAtivo(false);

        assertThatThrownBy(() -> useCase.executar(new Comando("rt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token inválido");
    }

    @Test
    void deveRecusarAdminDesconhecido() {
        when(jwtService.isTokenValido("rt")).thenReturn(true);
        when(jwtService.extrairClaims("rt")).thenReturn(claims);
        when(jwtService.isRefreshToken(claims)).thenReturn(true);
        when(claims.get("adminId", String.class)).thenReturn(admin.getId().toString());
        when(repositoryPort.buscarPorId(admin.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new Comando("rt")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token inválido");
    }
}
