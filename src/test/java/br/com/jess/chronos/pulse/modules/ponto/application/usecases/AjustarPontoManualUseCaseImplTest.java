package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.AjustarPontoManualUseCase;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AjustarPontoManualUseCaseImplTest {

    @Mock
    private RegistroPontoRepositoryPort repositoryPort;

    private AjustarPontoManualUseCaseImpl useCase;
    private UUID colaboradorId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        useCase = new AjustarPontoManualUseCaseImpl(repositoryPort);
        colaboradorId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    @Test
    void deveRegistrarAjusteManualComSucessoQuandoJustificativaFornecida() {
        when(repositoryPort.obterProximoNsr()).thenReturn(10L);
        when(repositoryPort.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var comando = new AjustarPontoManualUseCase.Comando(
                colaboradorId,
                tenantId,
                "12345678901",
                Instant.now(),
                TipoRegistro.ENTRADA,
                "Esquecimento de marcação",
                "Cheguei às 08:00 normalmente"
        );

        RegistroPonto resultado = useCase.executar(comando);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getAjusteManual()).isTrue();
        assertThat(resultado.getJustificativa()).isEqualTo("Esquecimento de marcação");
        assertThat(resultado.getObservacao()).isEqualTo("Cheguei às 08:00 normalmente");
        assertThat(resultado.getNsr()).isEqualTo(10L);
        assertThat(resultado.getHashIntegridade()).isNotNull().hasSize(64);
        verify(repositoryPort).salvar(any());
    }

    @Test
    void deveLancarExcecaoQuandoJustificativaEstiverVaziaOuNula() {
        var comando = new AjustarPontoManualUseCase.Comando(
                colaboradorId,
                tenantId,
                "12345678901",
                Instant.now(),
                TipoRegistro.ENTRADA,
                "   ",
                null
        );

        assertThatThrownBy(() -> useCase.executar(comando))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("justificativa é obrigatória");
    }
}
