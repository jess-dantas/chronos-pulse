package br.com.jess.chronos.pulse.modules.modulo.infrastructure.security;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/// Enforço de módulo por rota (@RequiresModulo): colaborador depende do
/// vínculo usuario_modulo; o Gestor RH tem escopo fixo PONTO +
/// RECURSOS_HUMANOS decidido pelo papel (V005 normaliza os vínculos legados).
@ExtendWith(MockitoExtension.class)
class ModuloInterceptorTest {

    @Mock
    private ModulosPort modulosPort;

    private ModuloInterceptor interceptor;

    static class ControladorFixture {
        @RequiresModulo("PONTO")
        public void ponto() {
        }

        @RequiresModulo("RECURSOS_HUMANOS")
        public void rh() {
        }

        @RequiresModulo("ESTOQUE")
        public void estoque() {
        }

        public void semModulo() {
        }
    }

    @BeforeEach
    void setUp() {
        interceptor = new ModuloInterceptor(modulosPort);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(CpcUsuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
    }

    private HandlerMethod handler(String metodo) throws NoSuchMethodException {
        return new HandlerMethod(new ControladorFixture(),
                ControladorFixture.class.getMethod(metodo));
    }

    private CpcUsuario usuario(Role role) {
        return new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "22222222222",
                "Usuario", "usuario@empresa.com", "hash", role, UUID.randomUUID());
    }

    @Test
    void gestorRhLiberaPontoSemDependerDeVinculo() throws Exception {
        var gestor = usuario(Role.GESTOR_RH);
        autenticar(gestor);
        when(modulosPort.isAtivo(gestor.getTenantId(), "PONTO")).thenReturn(true);

        assertThat(interceptor.preHandle(null, null, handler("ponto"))).isTrue();

        // O papel decide: o vínculo usuario_modulo não é consultado.
        verify(modulosPort, never()).usuarioModuloAtivo(any(), any(), any());
    }

    @Test
    void gestorRhLiberaRecursosHumanosSemDependerDeVinculo() throws Exception {
        var gestor = usuario(Role.GESTOR_RH);
        autenticar(gestor);
        when(modulosPort.isAtivo(gestor.getTenantId(), "RECURSOS_HUMANOS")).thenReturn(true);

        assertThat(interceptor.preHandle(null, null, handler("rh"))).isTrue();
    }

    @Test
    void gestorRhBloqueiaEstoqueMesmoComVinculoAtivo() throws Exception {
        var gestor = usuario(Role.GESTOR_RH);
        autenticar(gestor);

        assertThatThrownBy(() -> interceptor.preHandle(null, null, handler("estoque")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.FORBIDDEN));
        verifyNoInteractions(modulosPort);
    }

    @Test
    void gestorRhBloqueiaPontoQuandoONaoContratouOTenant() throws Exception {
        var gestor = usuario(Role.GESTOR_RH);
        autenticar(gestor);
        when(modulosPort.isAtivo(gestor.getTenantId(), "PONTO")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preHandle(null, null, handler("ponto")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void colaboradorComVinculoLibera() throws Exception {
        var colaborador = usuario(Role.COLABORADOR);
        autenticar(colaborador);
        when(modulosPort.usuarioModuloAtivo(colaborador.getId(), colaborador.getTenantId(), "ESTOQUE"))
                .thenReturn(true);

        assertThat(interceptor.preHandle(null, null, handler("estoque"))).isTrue();
    }

    @Test
    void colaboradorSemVinculoBloqueia() throws Exception {
        var colaborador = usuario(Role.COLABORADOR);
        autenticar(colaborador);
        when(modulosPort.usuarioModuloAtivo(colaborador.getId(), colaborador.getTenantId(), "ESTOQUE"))
                .thenReturn(false);

        assertThatThrownBy(() -> interceptor.preHandle(null, null, handler("estoque")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void adminEmpresaBypassaSemConsultarModulos() throws Exception {
        autenticar(usuario(Role.ADMIN_EMPRESA));

        assertThat(interceptor.preHandle(null, null, handler("estoque"))).isTrue();

        verifyNoInteractions(modulosPort);
    }

    @Test
    void rotaSemAnotacaoLiberaQualquerFluxo() throws Exception {
        assertThat(interceptor.preHandle(null, null, handler("semModulo"))).isTrue();

        verifyNoInteractions(modulosPort);
    }
}
