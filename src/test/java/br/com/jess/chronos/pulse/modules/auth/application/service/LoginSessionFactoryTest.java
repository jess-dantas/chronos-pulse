package br.com.jess.chronos.pulse.modules.auth.application.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.telemetria.application.LoginMetricsRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// Módulos devolvidos no login: ADMIN_EMPRESA recebe todos os ativos do
/// tenant; GESTOR_RH tem escopo fixo PONTO + RECURSOS_HUMANOS (ignora os
/// vínculos de usuario_modulo); os demais usam o vínculo por usuário.
@ExtendWith(MockitoExtension.class)
class LoginSessionFactoryTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private ModulosPort modulosPort;
    @Mock
    private EmpresaRepositoryPort empresaRepository;
    @Mock
    private LoginMetricsRecorder loginMetricsRecorder;

    private LoginSessionFactory factory;

    @BeforeEach
    void setUp() {
        factory = new LoginSessionFactory(jwtService, modulosPort, empresaRepository,
                loginMetricsRecorder);
    }

    private CpcUsuario usuario(Role role, UUID tenantId) {
        return new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "22222222222",
                "Usuario", "usuario@empresa.com", "hash", role, tenantId);
    }

    @Test
    void gestorRhRecebeApenasPontoERhIndependentementeDosVinculos() {
        UUID tenantId = UUID.randomUUID();
        var gestor = usuario(Role.GESTOR_RH, tenantId);
        when(empresaRepository.buscarPorId(tenantId)).thenReturn(Optional.empty());
        when(modulosPort.listarCodigosAtivos(tenantId))
                .thenReturn(List.of("PONTO", "RECURSOS_HUMANOS", "ESTOQUE", "FROTA"));

        var resultado = factory.montar(gestor);

        assertThat(resultado.modulos()).containsExactly("PONTO", "RECURSOS_HUMANOS");
        assertThat(resultado.acessoEstoque()).isFalse();
        // O papel decide: vínculos de usuario_modulo não são consultados.
        verify(modulosPort, never()).listarCodigosDoUsuario(gestor.getId(), tenantId);
    }

    @Test
    void gestorRhFiltraPelosModulosAtivosDoTenant() {
        UUID tenantId = UUID.randomUUID();
        var gestor = usuario(Role.GESTOR_RH, tenantId);
        when(empresaRepository.buscarPorId(tenantId)).thenReturn(Optional.empty());
        when(modulosPort.listarCodigosAtivos(tenantId))
                .thenReturn(List.of("RECURSOS_HUMANOS", "ESTOQUE"));

        var resultado = factory.montar(gestor);

        assertThat(resultado.modulos()).containsExactly("RECURSOS_HUMANOS");
    }

    @Test
    void colaboradorUsaOVinculoPorUsuario() {
        UUID tenantId = UUID.randomUUID();
        var colaborador = usuario(Role.COLABORADOR, tenantId);
        when(empresaRepository.buscarPorId(tenantId)).thenReturn(Optional.empty());
        when(modulosPort.listarCodigosDoUsuario(colaborador.getId(), tenantId))
                .thenReturn(List.of("PONTO"));

        var resultado = factory.montar(colaborador);

        assertThat(resultado.modulos()).containsExactly("PONTO");
    }
}
