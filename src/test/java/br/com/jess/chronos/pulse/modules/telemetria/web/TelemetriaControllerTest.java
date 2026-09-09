package br.com.jess.chronos.pulse.modules.telemetria.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TelemetriaEvento;
import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TipoEventoTelemetria;
import br.com.jess.chronos.pulse.modules.telemetria.service.TelemetriaService;
import br.com.jess.chronos.pulse.modules.telemetria.web.dto.EventoIngestDTO;
import br.com.jess.chronos.pulse.modules.telemetria.web.dto.RegistrarEventosDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetriaControllerTest {

    @Mock
    private TelemetriaService telemetriaService;

    @Mock
    private DataSource dataSource;

    @Mock
    private Authentication authentication;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @Captor
    private ArgumentCaptor<String> moduloCaptor;

    private TelemetriaController controller;

    private CpcUsuario usuario;
    private UUID tenantId;
    private UUID cpcId;

    @BeforeEach
    void setUp() throws Exception {
        controller = new TelemetriaController(telemetriaService, dataSource);
        tenantId = UUID.randomUUID();
        cpcId = UUID.randomUUID();
        usuario = new CpcUsuario(UUID.randomUUID(), cpcId, "12345678901", "Admin",
                "admin@empresa.com", "hash", Role.ADMIN_EMPRESA, tenantId);
        lenient().when(authentication.getPrincipal()).thenReturn(usuario);
    }

    @Test
    void deveRetornarSaudeUp() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT version()")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("PostgreSQL 16.0");

        ResponseEntity<br.com.jess.chronos.pulse.modules.telemetria.web.dto.SaudeDTO> resposta = controller.saude();

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().status()).isEqualTo("UP");
        assertThat(resposta.getBody().versaoPostgres()).contains("PostgreSQL");
        verify(telemetriaService).registrar(eq(TipoEventoTelemetria.CONEXAO_BD), eq("PLATAFORMA"),
                any(), any(), eq("/api/v1/telemetria/saude"), anyInt(), anyLong(), anyString(), any());
    }

    @Test
    void deveRetornarSaudeDownQuandoBancoIndisponivel() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("connection refused", "08001"));

        ResponseEntity<br.com.jess.chronos.pulse.modules.telemetria.web.dto.SaudeDTO> resposta = controller.saude();

        assertThat(resposta.getBody().status()).isEqualTo("DOWN");
        verify(telemetriaService).registrar(eq(TipoEventoTelemetria.CONEXAO_BD), eq("PLATAFORMA"),
                any(), any(), eq("/api/v1/telemetria/saude"), eq(503), anyLong(), anyString(), any());
    }

    @Test
    void deveRegistrarLoteEnriquecendoTenantEUsuario() {
        var lote = new RegistrarEventosDTO(List.of(
                new EventoIngestDTO("COMPRAS", TipoEventoTelemetria.API_REQUEST, "/api/v1/compras/pedidos",
                        200, 5L, "OK", null, "MOBILE", "trace-1")));

        ResponseEntity<Object> resposta = controller.registrarEventos(lote, authentication);

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        verify(telemetriaService).registrar(TipoEventoTelemetria.API_REQUEST, "COMPRAS", tenantId, cpcId,
                "/api/v1/compras/pedidos", 200, 5L, "OK", null);
    }

    @Test
    void deveConsultarEventosComFiltros() {
        TelemetriaEvento evento = TelemetriaEvento.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .modulo("AUTH")
                .tipo(TipoEventoTelemetria.LOGIN_SUCESSO)
                .endpoint("/api/v1/auth/login")
                .statusHttp(200)
                .appVersao("1.0.0")
                .criadoEm(Instant.now())
                .build();

        when(telemetriaService.consultar(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(evento), PageRequest.of(0, 20), 1));

        var resposta = controller.consultarEventos(TipoEventoTelemetria.LOGIN_SUCESSO, "AUTH",
                tenantId.toString(), null, null, PageRequest.of(0, 20));

        assertThat(resposta.getBody().conteudo()).hasSize(1);
        assertThat(resposta.getBody().conteudo().get(0).tipo()).isEqualTo(TipoEventoTelemetria.LOGIN_SUCESSO);
        assertThat(resposta.getBody().totalElementos()).isEqualTo(1);
    }
}