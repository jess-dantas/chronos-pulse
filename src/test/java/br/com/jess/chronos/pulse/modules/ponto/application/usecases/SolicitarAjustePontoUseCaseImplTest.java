package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.SolicitarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitarAjustePontoUseCaseImplTest {

    @Mock
    private RegistroPontoRepositoryPort repositoryPort;

    private SolicitarAjustePontoUseCaseImpl useCase;
    private UUID colaboradorId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        useCase = new SolicitarAjustePontoUseCaseImpl(repositoryPort);
        colaboradorId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    private SolicitarAjustePontoUseCase.Comando comando() {
        return new SolicitarAjustePontoUseCase.Comando(
                colaboradorId, tenantId, "12345678901", Instant.now(),
                TipoRegistro.ENTRADA, "Bateu e esqueceu o ponto", null);
    }

    @Test
    void deveAtribuirNsrENsrLogicoAntesDePersistir() {
        when(repositoryPort.obterProximoNsrLogico(colaboradorId, tenantId)).thenReturn(5L);
        when(repositoryPort.obterProximoNsr()).thenReturn(42L);
        when(repositoryPort.salvar(any())).thenAnswer(inv -> {
            RegistroPonto salvo = inv.getArgument(0);
            assertThat(salvo.getNsr()).as("nsr deve estar atribuído antes do save").isNotNull();
            assertThat(salvo.getNsrLogico()).isEqualTo(5L);
            assertThat(salvo.getAjusteStatus()).isEqualTo(AjusteStatus.PENDENTE);
            return salvo;
        });

        RegistroPonto resultado = useCase.executar(comando());

        assertThat(resultado.getNsr()).isEqualTo(42L);
        assertThat(resultado.getNsrLogico()).isEqualTo(5L);
        assertThat(resultado.getHashIntegridade()).isNotNull().hasSize(64);
        verify(repositoryPort).obterProximoNsr();
        verify(repositoryPort).obterProximoNsrLogico(colaboradorId, tenantId);
        verify(repositoryPort).salvar(any());
    }

    @Test
    void deveRejeitarJustificativaVazia() {
        SolicitarAjustePontoUseCase.Comando invalido = new SolicitarAjustePontoUseCase.Comando(
                colaboradorId, tenantId, "12345678901", Instant.now(),
                TipoRegistro.ENTRADA, "   ", null);

        assertThatThrownBy(() -> useCase.executar(invalido))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("justificativa");

        verifyNoInteractions(repositoryPort);
    }
}
