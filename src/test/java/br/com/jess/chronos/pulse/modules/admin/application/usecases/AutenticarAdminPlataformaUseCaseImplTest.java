package br.com.jess.chronos.pulse.modules.admin.application.usecases;

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
                repositoryPort, passwordEncoder, jwtService, twoFactorRequired);
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
}
