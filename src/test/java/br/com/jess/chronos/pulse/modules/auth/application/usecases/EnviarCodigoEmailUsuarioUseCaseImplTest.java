package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.EnviarCodigoEmailUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailRecuperacaoSenhaService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnviarCodigoEmailUsuarioUseCaseImplTest {

    @Mock
    private CpcUsuarioRepositoryPort repositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailRecuperacaoSenhaService emailRecuperacaoSenhaService;

    private EnviarCodigoEmailUsuarioUseCaseImpl useCase;

    private CpcUsuario usuario;
    private Claims claims;

    @BeforeEach
    void setUp() {
        useCase = new EnviarCodigoEmailUsuarioUseCaseImpl(repositoryPort, passwordEncoder,
                jwtService, emailRecuperacaoSenhaService);
        usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", "colab@empresa.com", "hash", Role.COLABORADOR, null);
        claims = mock(Claims.class);
    }

    private void prepararToken() {
        when(jwtService.extrairClaims("temp")).thenReturn(claims);
        when(jwtService.isTwoFactorToken(claims)).thenReturn(true);
        when(claims.get("usuarioId", String.class)).thenReturn(usuario.getId().toString());
        when(repositoryPort.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
    }

    @Test
    void deveGerarCodigoDe8DigitosEEnviarAsync() {
        prepararToken();
        when(passwordEncoder.encode(any())).thenReturn("bcrypt-codigo");

        useCase.executar(new EnviarCodigoEmailUsuarioUseCase.Comando("temp"));

        var codigoCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailRecuperacaoSenhaService).enviarCodigoRecuperacaoAsync(
                eq("colab@empresa.com"), codigoCaptor.capture());
        assertThat(codigoCaptor.getValue()).matches("\\d{8}");

        var usuarioCaptor = ArgumentCaptor.forClass(CpcUsuario.class);
        verify(repositoryPort).atualizar(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getTwoFactorEmailHash()).isEqualTo("bcrypt-codigo");
        assertThat(usuarioCaptor.getValue().getTwoFactorEmailExpiraEm()).isAfter(Instant.now());
        assertThat(usuarioCaptor.getValue().getTwoFactorEmailTentativas()).isZero();
    }

    @Test
    void deveFalharSemEmailCadastrado() {
        CpcUsuario semEmail = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", null, "hash", Role.COLABORADOR, null);
        when(jwtService.extrairClaims("temp")).thenReturn(claims);
        when(jwtService.isTwoFactorToken(claims)).thenReturn(true);
        when(claims.get("usuarioId", String.class)).thenReturn(semEmail.getId().toString());
        when(repositoryPort.buscarPorId(semEmail.getId())).thenReturn(Optional.of(semEmail));

        assertThatThrownBy(() -> useCase.executar(
                new EnviarCodigoEmailUsuarioUseCase.Comando("temp")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("E-mail de recuperação não cadastrado");

        verify(repositoryPort, never()).atualizar(any());
    }

    @Test
    void deveRecusarTempTokenExpirado() {
        when(jwtService.extrairClaims("temp")).thenThrow(new RuntimeException("expired"));

        assertThatThrownBy(() -> useCase.executar(
                new EnviarCodigoEmailUsuarioUseCase.Comando("temp")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sessão expirada");
    }
}
