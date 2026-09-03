package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailComprovantePontoService;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RegistrarPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.RegistroPontoDTO;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.ResultadoSincronizacaoDTO;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.SincronizacaoLoteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SincronizacaoPontoControllerTest {

    @Mock private RegistrarPontoUseCase registrarPontoUseCase;
    @Mock private EmailComprovantePontoService emailComprovantePontoService;
    @Mock private Authentication authentication;
    @InjectMocks private SincronizacaoPontoController controller;

    private CpcUsuario usuario;
    private UUID tenantId;
    private UUID colaboradorId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        colaboradorId = UUID.randomUUID();
        usuario = new CpcUsuario(UUID.randomUUID(), colaboradorId, "12345678901", "Colaborador",
                "colab@empresa.com", "hash", Role.COLABORADOR, tenantId);
        lenient().when(authentication.getPrincipal()).thenReturn(usuario);
    }

    private RegistroPontoDTO dto(UUID id) {
        return new RegistroPontoDTO(id, Instant.now(), new BigDecimal("-23.5"), new BigDecimal("-46.6"),
                new BigDecimal("5.0"), null, "hashLocal");
    }

    @Test
    void deveProcessarLoteComSucesso() {
        UUID id = UUID.randomUUID();
        SincronizacaoLoteDTO lote = new SincronizacaoLoteDTO(List.of(dto(id)));
        when(registrarPontoUseCase.executar(any(), eq("12345678901"), eq(tenantId))).thenReturn(mock(RegistroPonto.class));

        ResponseEntity<ResultadoSincronizacaoDTO> response = controller.sincronizarLote(lote, authentication);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().idsSucesso()).containsExactly(id);
        assertThat(response.getBody().idsFalha()).isEmpty();
    }

    @Test
    void deveRegistrarFalhaQuandoUseCaseLancaExcecao() {
        UUID id = UUID.randomUUID();
        SincronizacaoLoteDTO lote = new SincronizacaoLoteDTO(List.of(dto(id)));
        when(registrarPontoUseCase.executar(any(), any(), any())).thenThrow(new RuntimeException("erro"));

        ResponseEntity<ResultadoSincronizacaoDTO> response = controller.sincronizarLote(lote, authentication);

        assertThat(response.getBody().idsFalha()).containsExactly(id);
        assertThat(response.getBody().idsSucesso()).isEmpty();
    }

    @Test
    void deveProcessarLoteMistoComSucessoEFalha() {
        UUID idSucesso = UUID.randomUUID();
        UUID idFalha = UUID.randomUUID();
        SincronizacaoLoteDTO lote = new SincronizacaoLoteDTO(List.of(
                dto(idSucesso),
                dto(idFalha)
        ));

        when(registrarPontoUseCase.executar(any(), any(), any()))
                .thenReturn(mock(RegistroPonto.class))
                .thenThrow(new RuntimeException("erro"));

        ResponseEntity<ResultadoSincronizacaoDTO> response = controller.sincronizarLote(lote, authentication);

        assertThat(response.getBody().idsSucesso()).containsExactly(idSucesso);
        assertThat(response.getBody().idsFalha()).containsExactly(idFalha);
    }
}
