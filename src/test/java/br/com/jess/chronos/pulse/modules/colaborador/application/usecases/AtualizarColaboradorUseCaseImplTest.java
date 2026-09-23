package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.AtualizarColaboradorUseCase.Comando;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarColaboradorUseCaseImplTest {

    @Mock
    private ColaboradorRepositoryPort colaboradorRepository;

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    @Mock
    private ModulosPort modulosPort;

    private AtualizarColaboradorUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AtualizarColaboradorUseCaseImpl(colaboradorRepository, usuarioRepository, modulosPort);
    }

    @Test
    void deveBloquearAtualizacaoSemTenant() {
        Comando comando = comandoBase(UUID.randomUUID(), null);

        assertThatThrownBy(() -> useCase.executar(comando))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tenant ID obrigatório");

        verify(colaboradorRepository, never()).atualizar(any());
        verify(usuarioRepository, never()).atualizar(any());
    }

    @Test
    void deveBloquearAtualizacaoQuandoColaboradorNaoPertenceAoTenant() {
        UUID tenantLogado = UUID.randomUUID();
        UUID tenantOutro = UUID.randomUUID();
        UUID colaboradorId = UUID.randomUUID();

        Comando comando = comandoBase(colaboradorId, tenantLogado);

        when(colaboradorRepository.buscarPorIdETenant(colaboradorId, tenantLogado)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(comando))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não encontrado no seu tenant");

        verify(colaboradorRepository, never()).atualizar(any());
        verify(usuarioRepository, never()).atualizar(any());
    }

    @Test
    void deveRemoverModulosToggleAovDesativados() {
        UUID tenantLogado = UUID.randomUUID();
        UUID colaboradorId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Colaborador colaborador = new Colaborador(
                colaboradorId, usuarioId, tenantLogado, "MAT001", "Cargo", "Depto",
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1), null, null);
        when(colaboradorRepository.buscarPorIdETenant(colaboradorId, tenantLogado))
                .thenReturn(Optional.of(colaborador));

        CpcUsuario usuario = new CpcUsuario(
                usuarioId, UUID.randomUUID(), "12345678901", "Fulano",
                "fulano@empresa.com", "hash", Role.COLABORADOR, tenantLogado,
                true, true, true, true, null);
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

        when(modulosPort.listarCodigosDoUsuario(usuarioId, tenantLogado))
                .thenReturn(java.util.List.of("PONTO", "ESTOQUE", "PATRIMONIO", "FROTA", "PROTOCOLO"));

        Comando comando = new Comando(
                colaboradorId, tenantLogado,
                "Fulano", "fulano@empresa.com", "MAT001", "Cargo", "Depto",
                LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1), null,
                false, false, false, false, "(11) 97777-6666"
        );

        useCase.executar(comando);

        var codigosCaptor = org.mockito.ArgumentCaptor.forClass(java.util.List.class);
        verify(modulosPort).definirModulosDoUsuario(
                org.mockito.ArgumentMatchers.eq(usuarioId),
                org.mockito.ArgumentMatchers.eq(tenantLogado),
                codigosCaptor.capture());
        assertThat(codigosCaptor.getValue()).containsExactly("PONTO");
    }

    private Comando comandoBase(UUID colaboradorId, UUID tenantId) {
        return new Comando(
                colaboradorId,
                tenantId,
                "Fulano",
                "fulano@empresa.com",
                "MAT001",
                "Cargo",
                "Departamento",
                LocalDate.of(1990, 1, 1),
                LocalDate.of(2020, 1, 1),
                null,
                true, true, true, true,
                null
        );
    }
}