package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerenciarTwoFactorUsuarioUseCaseImplTest {

    @Mock
    private CpcUsuarioRepositoryPort repositoryPort;

    @Mock
    private TotpService totpService;

    private GerenciarTwoFactorUsuarioUseCaseImpl useCase;

    private CpcUsuario usuario;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarTwoFactorUsuarioUseCaseImpl(repositoryPort, totpService);
        usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", "colab@empresa.com", "hash", Role.COLABORADOR, null);
    }

    private void usar() {
        when(repositoryPort.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
    }

    @Test
    void statusDeveRefletirHabilitacao() {
        usar();

        assertThat(useCase.status(usuario.getId().toString()).enabled()).isFalse();
        usuario.setTwoFactorEnabled(true);
        assertThat(useCase.status(usuario.getId().toString()).enabled()).isTrue();
    }

    @Test
    void setupDeveGerarSegredoEUri() {
        usar();
        when(totpService.gerarSegredo()).thenReturn("SEGREDO");
        when(totpService.gerarOtpauthUri("SEGREDO", "12345678901", "Chronos Pulse"))
                .thenReturn("otpauth://totp/Chronos Pulse:12345678901");

        var resultado = useCase.setup(usuario.getId().toString());

        assertThat(resultado.secret()).isEqualTo("SEGREDO");
        assertThat(resultado.otpauthUri()).startsWith("otpauth://");
        var captor = ArgumentCaptor.forClass(CpcUsuario.class);
        verify(repositoryPort).atualizar(captor.capture());
        assertThat(captor.getValue().getTwoFactorSecret()).isEqualTo("SEGREDO");
        assertThat(captor.getValue().isTwoFactorEnabled()).isFalse();
    }

    @Test
    void setupFalhaSeJaHabilitado() {
        usuario.setTwoFactorEnabled(true);
        usar();

        assertThatThrownBy(() -> useCase.setup(usuario.getId().toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está habilitado");

        verifyNoInteractions(totpService);
    }

    @Test
    void confirmarDeveHabilitarComCodigoValido() {
        usuario.setTwoFactorSecret("SEGREDO");
        usar();
        when(totpService.validar("123456", "SEGREDO")).thenReturn(true);

        useCase.confirmar(usuario.getId().toString(), "123456");

        assertThat(usuario.isTwoFactorEnabled()).isTrue();
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void confirmarFalhaSemSetupPrevio() {
        usar();

        assertThatThrownBy(() -> useCase.confirmar(usuario.getId().toString(), "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Execute a configuração primeiro");

        verifyNoInteractions(totpService);
        verify(repositoryPort, never()).atualizar(any());
    }

    @Test
    void confirmarFalhaComCodigoInvalido() {
        usuario.setTwoFactorSecret("SEGREDO");
        usar();
        when(totpService.validar("000000", "SEGREDO")).thenReturn(false);

        assertThatThrownBy(() -> useCase.confirmar(usuario.getId().toString(), "000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código inválido");

        assertThat(usuario.isTwoFactorEnabled()).isFalse();
    }

    @Test
    void desabilitarDeveLimparSegredoECodigoEmail() {
        usuario.setTwoFactorEnabled(true);
        usuario.setTwoFactorSecret("SEGREDO");
        usuario.definirCodigoEmail2FA("hash", Instant.now().plus(Duration.ofMinutes(10)));
        usar();
        when(totpService.validar("123456", "SEGREDO")).thenReturn(true);

        useCase.desabilitar(usuario.getId().toString(), "123456");

        assertThat(usuario.isTwoFactorEnabled()).isFalse();
        assertThat(usuario.getTwoFactorSecret()).isNull();
        assertThat(usuario.isCodigoEmail2FAValido()).isFalse();
        verify(repositoryPort).atualizar(usuario);
    }

    @Test
    void desabilitarFalhaSeNaoHabilitado() {
        usar();

        assertThatThrownBy(() -> useCase.desabilitar(usuario.getId().toString(), "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não está habilitado");
    }

    @Test
    void falhaQuandoUsuarioNaoEncontrado() {
        when(repositoryPort.buscarPorId(usuario.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.status(usuario.getId().toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuário não encontrado");
    }
}
