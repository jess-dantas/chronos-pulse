package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase.Comando;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase.Resultado;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.telemetria.application.LoginMetricsRecorder;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioUseCaseImplTest {

    @Mock
    private CpcUsuarioRepositoryPort repositoryPort;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModulosPort modulosPort;

    @Mock
    private LoginMetricsRecorder loginMetricsRecorder;

    private AutenticarUsuarioUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AutenticarUsuarioUseCaseImpl(repositoryPort, jwtService, passwordEncoder, modulosPort, loginMetricsRecorder);
    }

    @Test
    void deveAutenticarUsuarioComSucesso() {
        UUID cpcId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        CpcUsuario usuario = new CpcUsuario(UUID.randomUUID(), cpcId, "12345678901", "Usuario Teste",
                "teste@empresa.com", "hashSenha", Role.COLABORADOR, tenantId);

        when(repositoryPort.buscarPorCpf("12345678901")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hashSenha")).thenReturn(true);
        when(jwtService.gerarAccessToken("12345678901", "COLABORADOR", cpcId.toString(), tenantId.toString(),
                false, false, false, false))
                .thenReturn("access-token");
        when(jwtService.gerarRefreshToken("12345678901")).thenReturn("refresh-token");
        when(modulosPort.listarCodigosAtivos(tenantId)).thenReturn(java.util.List.of("PONTO"));

        Resultado resultado = useCase.executar(new Comando("12345678901", "senha123"));

        assertThat(resultado.accessToken()).isEqualTo("access-token");
        assertThat(resultado.refreshToken()).isEqualTo("refresh-token");
        assertThat(resultado.role()).isEqualTo("COLABORADOR");
        assertThat(resultado.cpf()).isEqualTo("12345678901");
        assertThat(resultado.cpcId()).isEqualTo(cpcId.toString());
        assertThat(resultado.modulos()).containsExactly("PONTO");
        verify(loginMetricsRecorder).registrarSucesso(tenantId, cpcId, "COLABORADOR");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(repositoryPort.buscarPorCpf("12345678901")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new Comando("12345678901", "senha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciais inválidas");

        verify(loginMetricsRecorder).registrarFalha("12345678901", "USUARIO_NAO_ENCONTRADO", false, null, null);
    }

    @Test
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        CpcUsuario usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901", "Usuario Teste",
                "teste@empresa.com", "hashSenha", Role.COLABORADOR, UUID.randomUUID());

        when(repositoryPort.buscarPorCpf("12345678901")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "hashSenha")).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(new Comando("12345678901", "senhaErrada")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciais inválidas");

        verify(loginMetricsRecorder).registrarFalha(eq("12345678901"), eq("SENHA_INVALIDA"), eq(true), any(), any());
    }
}
