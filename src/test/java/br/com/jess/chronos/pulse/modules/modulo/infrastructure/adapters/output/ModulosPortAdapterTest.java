package br.com.jess.chronos.pulse.modules.modulo.infrastructure.adapters.output;

import br.com.jess.chronos.pulse.modules.modulo.repository.UsuarioModuloJpaRepository;
import br.com.jess.chronos.pulse.modules.modulo.service.ModuloService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModulosPortAdapterTest {

    @Mock
    private ModuloService moduloService;

    @Mock
    private UsuarioModuloJpaRepository usuarioModuloRepository;

    @InjectMocks
    private ModulosPortAdapter adapter;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID tenantId = UUID.randomUUID();

    @Test
    void deveApagarLinhasDoUsuarioEmQualquerTenantAntesDeReinserir() {
        when(moduloService.listarCodigosAtivos(tenantId))
                .thenReturn(List.of("PONTO", "COMPRAS"));

        adapter.definirModulosDoUsuario(usuarioId, tenantId, List.of("PONTO"));

        // O delete NÃO pode filtrar por tenant: linhas com tenant_id divergente
        // ou NULL sobravam e o insert seguinte batia em uk_usuario_modulo (23505).
        verify(usuarioModuloRepository).deleteByUsuarioId(usuarioId);
        verify(usuarioModuloRepository, never())
                .deleteByUsuarioIdAndTenantId(any(), any());
        verify(usuarioModuloRepository).upsertModulo(
                any(UUID.class), eq(tenantId), eq(usuarioId), eq("PONTO"));
        verify(usuarioModuloRepository, never())
                .upsertModulo(any(UUID.class), any(), any(), eq("COMPRAS"));
    }

    @Test
    void deveSerIdempotenteAoRepetirMesmaDefinicao() {
        when(moduloService.listarCodigosAtivos(tenantId))
                .thenReturn(List.of("PONTO", "ESTOQUE"));

        adapter.definirModulosDoUsuario(usuarioId, tenantId, List.of("PONTO", "ESTOQUE"));
        adapter.definirModulosDoUsuario(usuarioId, tenantId, List.of("PONTO", "ESTOQUE"));

        // Reprovisionamento (retry, nova aceitação) usa upsert — nunca duplicate key.
        verify(usuarioModuloRepository, org.mockito.Mockito.times(2))
                .deleteByUsuarioId(usuarioId);
        verify(usuarioModuloRepository, org.mockito.Mockito.times(4))
                .upsertModulo(any(UUID.class), eq(tenantId), eq(usuarioId), anyString());
    }

    @Test
    void deveIgnorarCodigosForaDosContratados() {
        when(moduloService.listarCodigosAtivos(tenantId))
                .thenReturn(List.of("PONTO"));

        adapter.definirModulosDoUsuario(usuarioId, tenantId, List.of("PONTO", "HACK"));

        verify(usuarioModuloRepository, never())
                .upsertModulo(any(UUID.class), any(), any(), eq("HACK"));
    }

    @Test
    void deveRemoverTudoQuandoListaVazia() {
        when(moduloService.listarCodigosAtivos(tenantId))
                .thenReturn(List.of("PONTO"));

        adapter.definirModulosDoUsuario(usuarioId, tenantId, List.of());

        verify(usuarioModuloRepository).deleteByUsuarioId(usuarioId);
        verify(usuarioModuloRepository, never())
                .upsertModulo(any(UUID.class), any(), any(), anyString());
    }

    @Test
    void deveRejeitarTenantNulo() {
        assertThatThrownBy(() -> adapter.definirModulosDoUsuario(usuarioId, null, List.of("PONTO")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant");
    }

    @Test
    void listarCodigosDoUsuarioDeveSerIntersecaoComAtivos() {
        when(moduloService.listarCodigosAtivos(tenantId))
                .thenReturn(List.of("PONTO", "COMPRAS"));
        var linhaPonto = new br.com.jess.chronos.pulse.modules.modulo.repository.UsuarioModuloJpaEntity();
        linhaPonto.setUsuarioId(usuarioId);
        linhaPonto.setTenantId(tenantId);
        linhaPonto.setCodigo("PONTO");
        var linhaObsoleta = new br.com.jess.chronos.pulse.modules.modulo.repository.UsuarioModuloJpaEntity();
        linhaObsoleta.setUsuarioId(usuarioId);
        linhaObsoleta.setTenantId(tenantId);
        linhaObsoleta.setCodigo("FOLHA");
        when(usuarioModuloRepository.findByUsuarioIdAndTenantId(usuarioId, tenantId))
                .thenReturn(List.of(linhaPonto, linhaObsoleta));

        var codigos = adapter.listarCodigosDoUsuario(usuarioId, tenantId);

        assertThat(codigos).containsExactly("PONTO");
    }

    @Test
    void listarCodigosSemTenantDeveSerVazio() {
        assertThat(adapter.listarCodigosDoUsuario(usuarioId, null)).isEmpty();
        verify(moduloService, never()).listarCodigosAtivos(any());
    }
}
