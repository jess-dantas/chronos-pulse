package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import br.com.jess.chronos.pulse.modules.auth.application.service.DeviceTokenService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.VerificarDeviceTwoFactorUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificarDeviceTwoFactorUseCaseImplTest {

    @Mock
    private DeviceTokenService deviceTokenService;

    @Mock
    private CpcUsuarioRepositoryPort repositoryPort;

    @Mock
    private TotpService totpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    private VerificarDeviceTwoFactorUseCaseImpl useCase;

    private CpcUsuario usuario;

    @BeforeEach
    void setUp() {
        useCase = new VerificarDeviceTwoFactorUseCaseImpl(deviceTokenService, repositoryPort,
                totpService, passwordEncoder, emailRecuperacaoSenhaService);
        usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", "colab@empresa.com", "hash", Role.COLABORADOR, null);
        usuario.setTwoFactorEnabled(true);
        usuario.setTwoFactorSecret("SECRET");
    }

    private void comVinculo() {
        when(deviceTokenService.autenticar("device-token")).thenReturn(Optional.of(usuario));
    }

    @Test
    void tokenInvalidoDeveRetornarVazio() {
        when(deviceTokenService.autenticar("device-token")).thenReturn(Optional.empty());

        var resultado = useCase.executar(new VerificarDeviceTwoFactorUseCase.Comando(
                "device-token", VerificarDeviceTwoFactorUseCase.Metodo.TOTP, "123456"));

        assertThat(resultado).isEmpty();
        verifyNoInteractions(totpService, repositoryPort);
    }

    @Test
    void totpValidoDeveVerificar() {
        comVinculo();
        when(totpService.validar("123456", "SECRET")).thenReturn(true);

        var resultado = useCase.executar(new VerificarDeviceTwoFactorUseCase.Comando(
                "device-token", VerificarDeviceTwoFactorUseCase.Metodo.TOTP, "123456")).orElseThrow();

        assertThat(resultado.verificado()).isTrue();
        assertThat(resultado.enviado()).isFalse();
        assertThat(resultado.cpcId()).isEqualTo(usuario.getCpcId());
        assertThat(resultado.cpf()).isEqualTo("12345678901");
        assertThat(resultado.role()).isEqualTo("COLABORADOR");
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void totpInvalidoDeveContarFalha() {
        comVinculo();
        when(totpService.validar("000000", "SECRET")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new VerificarDeviceTwoFactorUseCase.Comando(
                "device-token", VerificarDeviceTwoFactorUseCase.Metodo.TOTP, "000000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        assertThat(usuario.getTentativasLoginFalhas()).isEqualTo(1);
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void emailSemCodigoDeveGerarEEnviarOtp() {
        comVinculo();
        when(passwordEncoder.encode(any())).thenReturn("bcrypt-codigo");

        var resultado = useCase.executar(new VerificarDeviceTwoFactorUseCase.Comando(
                "device-token", VerificarDeviceTwoFactorUseCase.Metodo.EMAIL, null)).orElseThrow();

        assertThat(resultado.verificado()).isFalse();
        assertThat(resultado.enviado()).isTrue();
        assertThat(resultado.expiraEm()).isAfter(Instant.now());

        var codigoCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailRecuperacaoSenhaService).enviarCodigoRecuperacaoAsync(
                eq("colab@empresa.com"), codigoCaptor.capture());
        assertThat(codigoCaptor.getValue()).matches("\\d{8}");
        assertThat(usuario.getTwoFactorEmailHash()).isEqualTo("bcrypt-codigo");
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void emailComCodigoValidoDeveVerificar() {
        usuario.definirCodigoEmail2FA("bcrypt-codigo", Instant.now().plus(Duration.ofMinutes(10)));
        comVinculo();
        when(passwordEncoder.matches("123456", "bcrypt-codigo")).thenReturn(true);

        var resultado = useCase.executar(new VerificarDeviceTwoFactorUseCase.Comando(
                "device-token", VerificarDeviceTwoFactorUseCase.Metodo.EMAIL, "123456")).orElseThrow();

        assertThat(resultado.verificado()).isTrue();
        assertThat(resultado.enviado()).isFalse();
        assertThat(usuario.isCodigoEmail2FAValido()).isFalse();
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void emailComCodigoInvalidoDeveContarTentativa() {
        usuario.definirCodigoEmail2FA("bcrypt-codigo", Instant.now().plus(Duration.ofMinutes(10)));
        comVinculo();
        when(passwordEncoder.matches("000000", "bcrypt-codigo")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new VerificarDeviceTwoFactorUseCase.Comando(
                "device-token", VerificarDeviceTwoFactorUseCase.Metodo.EMAIL, "000000")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        assertThat(usuario.getTwoFactorEmailTentativas()).isEqualTo(1);
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void deveRecusarQuandoDoisFatorDesabilitado() {
        usuario.setTwoFactorEnabled(false);
        comVinculo();

        assertThatThrownBy(() -> useCase.executar(new VerificarDeviceTwoFactorUseCase.Comando(
                "device-token", VerificarDeviceTwoFactorUseCase.Metodo.TOTP, "123456")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não está habilitado");

        verifyNoInteractions(totpService);
    }
}
