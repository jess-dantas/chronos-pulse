package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AtualizarSaldoContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtualizarSaldoContratoUseCaseImplTest {

    @Mock
    private ContratoRepositoryPort contratoRepositoryPort;

    @InjectMocks
    private AtualizarSaldoContratoUseCaseImpl useCase;

    private Contrato contratoBase() {
        return new Contrato(UUID.randomUUID(), UUID.randomUUID(), "CT-2026-001", "Dedetização",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                new BigDecimal("500.00"), new BigDecimal("6000.00"), "ATIVO");
    }

    @Test
    void atualizarSaldoDevePersistirNovosValores() {
        Contrato contrato = contratoBase();
        when(contratoRepositoryPort.buscarPorId(contrato.getId())).thenReturn(Optional.of(contrato));
        when(contratoRepositoryPort.salvar(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Contrato> resultado = useCase.executar(new AtualizarSaldoContratoUseCase.Comando(
                contrato.getId(), new BigDecimal("5000.00"), new BigDecimal("2000.00"),
                "EMP-2026/001", 30));

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getValorEmpenhado()).isEqualByComparingTo("5000.00");
        assertThat(resultado.get().getValorLiquidado()).isEqualByComparingTo("2000.00");
        assertThat(resultado.get().getEmpenhoNumero()).isEqualTo("EMP-2026/001");
        assertThat(resultado.get().getSaldo()).isEqualByComparingTo("3000.00");
        verify(contratoRepositoryPort).salvar(any(Contrato.class));
    }

    @Test
    void contratoInexistenteDeveRetornarVazio() {
        when(contratoRepositoryPort.buscarPorId(any())).thenReturn(Optional.empty());

        Optional<Contrato> resultado = useCase.executar(new AtualizarSaldoContratoUseCase.Comando(
                UUID.randomUUID(), BigDecimal.TEN, BigDecimal.ZERO, null, null));

        assertThat(resultado).isEmpty();
        verify(contratoRepositoryPort, never()).salvar(any());
    }

    @Test
    void liquidacaoSuperiorAoEmpenhoDeveLancarErro() {
        Contrato contrato = contratoBase();
        when(contratoRepositoryPort.buscarPorId(contrato.getId())).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> useCase.executar(new AtualizarSaldoContratoUseCase.Comando(
                contrato.getId(), new BigDecimal("1000.00"), new BigDecimal("1500.00"), null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor liquidado não pode superar o valor empenhado.");
    }

    @Test
    void dominioDeveCalcularSaldoEVigencia() {
        Contrato contrato = new Contrato(UUID.randomUUID(), UUID.randomUUID(), "CT-2026-002", "Limpeza",
                LocalDate.now().minusMonths(1), LocalDate.now().plusDays(10), null,
                new BigDecimal("12000.00"),
                "ATIVO", null,
                new BigDecimal("12000.00"), new BigDecimal("4000.00"), "EMP-2026/002", 10);

        assertThat(contrato.getSaldo()).isEqualByComparingTo("8000.00");
        assertThat(contrato.getStatusVigencia()).isEqualTo("VENCENDO");
        assertThat(contrato.getDiasParaVencimento()).isEqualTo(10);
    }
}