package br.com.jess.chronos.pulse.modules.ponto.application.usecases;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarPontoUseCaseImplTest {

    @Mock
    private RegistroPontoRepositoryPort repositoryPort;

    private RegistrarPontoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarPontoUseCaseImpl(repositoryPort);
    }

    private RegistroPonto novoRegistro() {
        return new RegistroPonto(UUID.randomUUID(), UUID.randomUUID(), Instant.now(),
                null, TipoRegistro.ENTRADA, new BigDecimal("-23.5505"), new BigDecimal("-46.6333"),
                new BigDecimal("5.0"), null, false, null);
    }

    @Test
    void deveAtribuirHashEPersistirRegistro() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.obterProximoNsr()).thenReturn(1L);
        when(repositoryPort.salvar(any())).thenReturn(registro);

        RegistroPonto resultado = useCase.executar(registro, "12345678901");

        assertThat(registro.getHashIntegridade()).isNotNull().hasSize(64);
        assertThat(resultado).isNotNull();
        verify(repositoryPort).obterProximoNsr();
        verify(repositoryPort).salvar(registro);
    }

    @Test
    void devePropagaExcecaoQuandoRepositorioFalha() {
        RegistroPonto registro = novoRegistro();
        when(repositoryPort.obterProximoNsr()).thenReturn(1L);
        when(repositoryPort.salvar(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> useCase.executar(registro, "12345678901"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }
}
