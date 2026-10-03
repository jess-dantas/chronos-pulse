package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.application.service.AdminDeviceTokenService;
import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AutenticarAdminPlataformaUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticarAdminPlataformaUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AdminDeviceTokenService adminDeviceTokenService;

    private AdminPlataforma admin;

    @BeforeEach
    void setUp() {
        admin = AdminPlataforma.builder()
                .id(UUID.randomUUID())
                .username("Administrator")
                .senhaHash("hash")
                .email("admin@example.com")
                .twoFactorEnabled(false)
                .ativo(true)
                .build();
    }

    private AutenticarAdminPlataformaUseCaseImpl useCase(boolean twoFactorRequired) {
        return new AutenticarAdminPlataformaUseCaseImpl(
                repositoryPort, passwordEncoder, jwtService, adminDeviceTokenService, twoFactorRequired);
    }

    private void prepararLogin() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin1234", "hash")).thenReturn(true);
    }

    @Test
    void deveForçarSetupQuando2FAObrigatorioEDesabilitado() {
        prepararLogin();
        when(jwtService.gerarTempTokenTwoFactor(admin.getId().toString())).thenReturn("temp-token");

        var resultado = useCase(true).executar(new Comando("Administrator", "admin1234"));

        assertThat(resultado.requiresTwoFactor()).isTrue();
        assertThat(resultado.setupRequired()).isTrue();
        assertThat(resultado.tempToken()).isEqualTo("temp-token");
        assertThat(resultado.accessToken()).isNull();
    }

    @Test
    void deveEmitirTokensDiretoQuando2FANaoObrigatorio() {
        prepararLogin();
        when(jwtService.gerarAccessTokenAdmin("Administrator", admin.getId().toString())).thenReturn("access");
        when(jwtService.gerarRefreshTokenAdmin("Administrator", admin.getId().toString())).thenReturn("refresh");

        var resultado = useCase(false).executar(new Comando("Administrator", "admin1234"));

        assertThat(resultado.requiresTwoFactor()).isFalse();
        assertThat(resultado.setupRequired()).isFalse();
        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(resultado.refreshToken()).isEqualTo("refresh");
        verify(repositoryPort).salvar(any(AdminPlataforma.class));
    }

    @Test
    void deveExigirVerificacaoQuando2FAJaHabilitado() {
        admin.setTwoFactorEnabled(true);
        prepararLogin();
        when(jwtService.gerarTempTokenTwoFactor(admin.getId().toString())).thenReturn("temp-token");

        var resultado = useCase(true).executar(new Comando("Administrator", "admin1234"));

        assertThat(resultado.requiresTwoFactor()).isTrue();
        assertThat(resultado.setupRequired()).isFalse();
        assertThat(resultado.tempToken()).isEqualTo("temp-token");
    }

    @Test
    void deveEmitirTempTokenSemSenhaQuando2FAHabilitado() {
        admin.setTwoFactorEnabled(true);
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(jwtService.gerarTempTokenTwoFactor(admin.getId().toString())).thenReturn("temp-token");

        var resultado = useCase(true).executar(new Comando("Administrator", null));

        assertThat(resultado.requiresTwoFactor()).isTrue();
        assertThat(resultado.setupRequired()).isFalse();
        assertThat(resultado.tempToken()).isEqualTo("temp-token");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveRecusarLoginSemSenhaQuando2FADesabilitado() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> useCase(true).executar(new Comando("Administrator", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Senha é obrigatória");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveBloquearLoginSemSenhaQuandoContaBloqueada() {
        admin.setBloqueioLoginAte(java.time.Instant.now().plusSeconds(600));
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> useCase(true).executar(new Comando("Administrator", null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloqueada");
        verifyNoInteractions(passwordEncoder);
    }

    // --- biometria-first: dispositivo confiável (deviceToken) ---

    @Test
    void devePularSenhaE2FAQuandoDispositivoConfiavel() {
        admin.setTwoFactorEnabled(true);
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(adminDeviceTokenService.validar(admin.getId(), "device-token")).thenReturn(true);
        prepararTokensFinais();

        var resultado = useCase(true)
                .executar(new Comando("Administrator", "admin1234", "device-token"));

        assertThat(resultado.requiresTwoFactor()).isFalse();
        assertThat(resultado.setupRequired()).isFalse();
        assertThat(resultado.tempToken()).isNull();
        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(resultado.refreshToken()).isEqualTo("refresh");
        verifyNoInteractions(passwordEncoder);
        verify(repositoryPort).salvar(any(AdminPlataforma.class));
    }

    @Test
    void devePularSenhaE2FAQuandoDispositivoConfiavelSemSenha() {
        admin.setTwoFactorEnabled(true);
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(adminDeviceTokenService.validar(admin.getId(), "device-token")).thenReturn(true);
        prepararTokensFinais();

        var resultado = useCase(true)
                .executar(new Comando("Administrator", null, "device-token"));

        assertThat(resultado.requiresTwoFactor()).isFalse();
        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(resultado.refreshToken()).isEqualTo("refresh");
        verifyNoInteractions(passwordEncoder);
        verify(jwtService, never()).gerarTempTokenTwoFactor(any());
    }

    @Test
    void deveIgnorarDispositivoInvalidoEExigir2FA() {
        admin.setTwoFactorEnabled(true);
        prepararLogin();
        when(adminDeviceTokenService.validar(admin.getId(), "device-vencido")).thenReturn(false);
        when(jwtService.gerarTempTokenTwoFactor(admin.getId().toString())).thenReturn("temp-token");

        var resultado = useCase(true)
                .executar(new Comando("Administrator", "admin1234", "device-vencido"));

        assertThat(resultado.requiresTwoFactor()).isTrue();
        assertThat(resultado.tempToken()).isEqualTo("temp-token");
        verify(adminDeviceTokenService).validar(admin.getId(), "device-vencido");
        verify(repositoryPort, never()).salvar(any(AdminPlataforma.class));
    }

    @Test
    void deveBloquearLoginComDispositivoQuandoContaBloqueada() {
        admin.setBloqueioLoginAte(java.time.Instant.now().plusSeconds(600));
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> useCase(true)
                .executar(new Comando("Administrator", "admin1234", "device-token")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("bloqueada");
        verifyNoInteractions(adminDeviceTokenService);
    }

    private void prepararTokensFinais() {
        when(jwtService.gerarAccessTokenAdmin("Administrator", admin.getId().toString())).thenReturn("access");
        when(jwtService.gerarRefreshTokenAdmin("Administrator", admin.getId().toString())).thenReturn("refresh");
    }
}
