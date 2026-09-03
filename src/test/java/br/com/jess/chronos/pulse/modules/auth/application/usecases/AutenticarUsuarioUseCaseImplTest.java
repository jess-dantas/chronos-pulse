package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase.Comando;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase.Resultado;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioUseCaseImplTest {

    @Mock
    private CpcUsuarioRepositoryPort repositoryPort;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AutenticarUsuarioUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AutenticarUsuarioUseCaseImpl(repositoryPort, jwtService, passwordEncoder);
    }

    @Test
    void deveAutenticarUsuarioComSucesso() {
        UUID cpcId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        CpcUsuario usuario = new CpcUsuario(UUID.randomUUID(), cpcId, "12345678901", "Usuario Teste",
                "teste@empresa.com", "hashSenha", Role.COLABORADOR, tenantId);

        when(repositoryPort.buscarPorCpf("12345678901")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha123", "hashSenha")).thenReturn(true);
        when(jwtService.gerarAccessToken("12345678901", "COLABORADOR", cpcId.toString(), tenantId.toString()))
                .thenReturn("access-token");
        when(jwtService.gerarRefreshToken("12345678901")).thenReturn("refresh-token");

        Resultado resultado = useCase.executar(new Comando("12345678901", "senha123"));

        assertThat(resultado.accessToken()).isEqualTo("access-token");
        assertThat(resultado.refreshToken()).isEqualTo("refresh-token");
        assertThat(resultado.role()).isEqualTo("COLABORADOR");
        assertThat(resultado.cpcId()).isEqualTo(cpcId.toString());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(repositoryPort.buscarPorCpf("12345678901")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(new Comando("12345678901", "senha123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Credenciais inválidas");
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
    }
}
