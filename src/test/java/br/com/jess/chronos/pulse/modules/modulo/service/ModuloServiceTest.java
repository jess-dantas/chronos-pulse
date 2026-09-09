package br.com.jess.chronos.pulse.modules.modulo.service;

import br.com.jess.chronos.pulse.modules.modulo.domain.entity.EmpresaModulo;
import br.com.jess.chronos.pulse.modules.modulo.domain.entity.ModuloPlataforma;
import br.com.jess.chronos.pulse.modules.modulo.repository.EmpresaModuloRepository;
import br.com.jess.chronos.pulse.modules.modulo.repository.ModuloPlataformaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuloServiceTest {

    @Mock
    private ModuloPlataformaRepository moduloRepository;

    @Mock
    private EmpresaModuloRepository empresaModuloRepository;

    @InjectMocks
    private ModuloService moduloService;

    private final UUID tenantId = UUID.randomUUID();

    private ModuloPlataforma modulo(String codigo) {
        return ModuloPlataforma.builder()
                .id(UUID.randomUUID())
                .codigo(codigo)
                .nome(codigo)
                .ativo(true)
                .build();
    }

    @BeforeEach
    void setUp() {
        lenient().when(moduloRepository.findByCodigo("PONTO"))
                .thenReturn(Optional.of(modulo("PONTO")));
        lenient().when(moduloRepository.findByCodigo("ESTOQUE"))
                .thenReturn(Optional.of(modulo("ESTOQUE")));
    }

    @Test
    @DisplayName("atualizarModulos remove os vínculos antigos antes de gravar os novos")
    void deveRemoverVinculosAntigosAntesDeGravar() {
        List<String> resultado = moduloService.atualizarModulos(
                tenantId, List.of("PONTO", "ESTOQUE"));

        assertEquals(List.of("PONTO", "ESTOQUE"), resultado);
        verify(empresaModuloRepository).deleteAllByTenantId(tenantId);
        verify(empresaModuloRepository, times(2)).save(any(EmpresaModulo.class));
    }

    @Test
    @DisplayName("atualizarModulos é idempotente ao reagendar o mesmo tenant")
    void devePermitirReagendarMesmoTenant() {
        moduloService.atualizarModulos(tenantId, List.of("PONTO", "ESTOQUE"));
        moduloService.atualizarModulos(tenantId, List.of("PONTO", "ESTOQUE"));

        verify(empresaModuloRepository, times(2)).deleteAllByTenantId(tenantId);
        verify(empresaModuloRepository, times(4)).save(any(EmpresaModulo.class));
    }

    @Test
    @DisplayName("atualizarModulos normaliza códigos em maiúsculas e remove duplicados")
    void deveNormalizarCodigosEIgnorarDuplicados() {
        List<String> resultado = moduloService.atualizarModulos(
                tenantId, List.of(" ponto ", "PONTO", "ESTOQUE"));

        assertEquals(List.of("PONTO", "ESTOQUE"), resultado);
        verify(empresaModuloRepository, times(2)).save(any(EmpresaModulo.class));
    }

    @Test
    @DisplayName("atualizarModulos grava o vínculo apontando para o módulo correto")
    void deveGravarVinculoComModuloCorreto() {
        moduloService.atualizarModulos(tenantId, List.of("PONTO"));

        ArgumentCaptor<EmpresaModulo> captor = ArgumentCaptor.forClass(EmpresaModulo.class);
        verify(empresaModuloRepository).save(captor.capture());
        EmpresaModulo gravado = captor.getValue();
        assertEquals(tenantId, gravado.getTenantId());
        assertEquals("PONTO", gravado.getModulo().getCodigo());
        assertNotNull(gravado.getAtivadoEm());
    }

    @Test
    @DisplayName("atualizarModulos rejeita módulo inexistente sem tocar nos vínculos")
    void deveRejeitarModuloInexistente() {
        when(moduloRepository.findByCodigo("FANTASMA"))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> moduloService.atualizarModulos(tenantId, List.of("FANTASMA")));
        verify(empresaModuloRepository, never()).deleteAllByTenantId(tenantId);
        verify(empresaModuloRepository, never()).save(any());
    }

    @Test
    @DisplayName("atualizarModulos rejeita módulo desativado sem tocar nos vínculos")
    void deveRejeitarModuloDesativado() {
        ModuloPlataforma inativo = modulo("PONTO");
        inativo.setAtivo(false);
        when(moduloRepository.findByCodigo("PONTO")).thenReturn(Optional.of(inativo));

        assertThrows(IllegalArgumentException.class,
                () -> moduloService.atualizarModulos(tenantId, List.of("PONTO")));
        verify(empresaModuloRepository, never()).deleteAllByTenantId(tenantId);
        verify(empresaModuloRepository, never()).save(any());
    }

    @Test
    @DisplayName("listarCodigosAtivos devolve lista vazia para tenant nulo")
    void deveDevolverVazioParaTenantNulo() {
        assertTrue(moduloService.listarCodigosAtivos(null).isEmpty());
    }
}