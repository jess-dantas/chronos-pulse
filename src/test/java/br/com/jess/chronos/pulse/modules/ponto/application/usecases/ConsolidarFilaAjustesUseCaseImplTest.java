package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.FilaAjusteItem;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsolidarFilaAjustesUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsolidarFilaAjustesUseCaseImplTest {

    private static final ZoneId FUSO_PONTO = ZoneId.of("America/Sao_Paulo");

    @Mock
    private RegistroPontoRepositoryPort repositoryPort;

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    private ConsolidarFilaAjustesUseCaseImpl useCase;
    private UUID colaboradorId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        useCase = new ConsolidarFilaAjustesUseCaseImpl(repositoryPort, usuarioRepository);
        colaboradorId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    private RegistroPonto registro(Instant dataHora, TipoRegistro tipo, boolean ajusteManual,
                                   AjusteStatus status) {
        return new RegistroPonto(null, colaboradorId, tenantId, dataHora, dataHora, tipo,
                null, null, null, null, true, 10L, 3L, ajusteManual,
                ajusteManual ? "Esqueci o ponto" : null, null, status, null, null, null);
    }

    private Instant inicioDoDia(LocalDate dia) {
        return dia.atStartOfDay(FUSO_PONTO).toInstant();
    }

    @Test
    void deveRetornarVazioQuandoNaoHaPendentes() {
        when(repositoryPort.listarAjustesPendentesPorTenant(tenantId)).thenReturn(List.of());

        List<FilaAjusteItem> fila = useCase.executar(
                new ConsolidarFilaAjustesUseCase.Comando(tenantId, UUID.randomUUID()));

        assertThat(fila).isEmpty();
    }

    @Test
    void deveConsolidarNomeEMarcacoesDoDiaOrdenadoPorDataHora() {
        Instant pendenteA = Instant.parse("2026-09-10T13:00:00Z"); // 10:00 em SP
        Instant pendenteB = Instant.parse("2026-09-12T13:00:00Z");
        Instant inicioDia10 = inicioDoDia(LocalDate.of(2026, 9, 10));
        Instant inicioDia12 = inicioDoDia(LocalDate.of(2026, 9, 12));

        when(repositoryPort.listarAjustesPendentesPorTenant(tenantId))
                .thenReturn(List.of(pendenteB, pendenteA)
                        .stream()
                        .map(t -> registro(t, TipoRegistro.ENTRADA, true, AjusteStatus.PENDENTE))
                        .toList());

        when(repositoryPort.listarPorColaboradorEPeriodo(any(), any(), any(), any()))
                .thenAnswer(inv -> {
                    Instant inicio = inv.getArgument(2);
                    if (inicio.equals(inicioDia10)) {
                        // Lista propositalmente fora de ordem: entrada, pendente e saída.
                        return List.of(
                                registro(Instant.parse("2026-09-10T15:00:00Z"), TipoRegistro.SAIDA, false, null),
                                registro(pendenteA, TipoRegistro.ENTRADA, true, AjusteStatus.PENDENTE),
                                registro(Instant.parse("2026-09-10T11:00:00Z"), TipoRegistro.ENTRADA, false, null));
                    }
                    return List.of(
                            registro(Instant.parse("2026-09-12T11:00:00Z"), TipoRegistro.ENTRADA, false, null));
                });

        CpcUsuario usuario = org.mockito.Mockito.mock(CpcUsuario.class);
        when(usuario.getNome()).thenReturn("Maria Silva");
        when(usuarioRepository.buscarPorId(colaboradorId)).thenReturn(Optional.of(usuario));

        List<FilaAjusteItem> fila = useCase.executar(
                new ConsolidarFilaAjustesUseCase.Comando(tenantId, UUID.randomUUID()));

        assertThat(fila).hasSize(2);
        assertThat(fila.get(0).ajuste().getDataHoraDispositivo()).isEqualTo(pendenteA);
        assertThat(fila.get(1).ajuste().getDataHoraDispositivo()).isEqualTo(pendenteB);

        FilaAjusteItem item = fila.get(0);
        assertThat(item.colaboradorNome()).isEqualTo("Maria Silva");

        assertThat(item.marcacoesDoDia())
                .extracting(FilaAjusteItem.MarcacaoDoDia::dataHora)
                .containsExactly(
                        Instant.parse("2026-09-10T11:00:00Z"),
                        pendenteA,
                        Instant.parse("2026-09-10T15:00:00Z"));
        assertThat(item.marcacoesDoDia().get(1).ajuste()).isTrue();
        assertThat(item.marcacoesDoDia().get(0).ajuste()).isFalse();
    }

    @Test
    void deveDeixarNomeNuloQuandoColaboradorNaoEncontrado() {
        Instant pendente = Instant.parse("2026-09-10T13:00:00Z");

        when(repositoryPort.listarAjustesPendentesPorTenant(tenantId))
                .thenReturn(List.of(registro(pendente, TipoRegistro.SAIDA, true, AjusteStatus.PENDENTE)));
        when(repositoryPort.listarPorColaboradorEPeriodo(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(usuarioRepository.buscarPorId(colaboradorId)).thenReturn(Optional.empty());

        List<FilaAjusteItem> fila = useCase.executar(
                new ConsolidarFilaAjustesUseCase.Comando(tenantId, UUID.randomUUID()));

        assertThat(fila).hasSize(1);
        assertThat(fila.get(0).colaboradorNome()).isNull();
        assertThat(fila.get(0).marcacoesDoDia()).isEmpty();
    }
}
