package br.com.jess.chronos.pulse.modules.telemetria.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TelemetriaEvento;
import br.com.jess.chronos.pulse.modules.telemetria.domain.entity.TipoEventoTelemetria;
import br.com.jess.chronos.pulse.modules.telemetria.service.TelemetriaService;
import br.com.jess.chronos.pulse.modules.telemetria.web.dto.EventoIngestDTO;
import br.com.jess.chronos.pulse.modules.telemetria.web.dto.EventoTelemetriaResponseDTO;
import br.com.jess.chronos.pulse.modules.telemetria.web.dto.PaginaEventosDTO;
import br.com.jess.chronos.pulse.modules.telemetria.web.dto.RegistrarEventosDTO;
import br.com.jess.chronos.pulse.modules.telemetria.web.dto.SaudeDTO;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetria")
public class TelemetriaController {

    private static final Logger log = LoggerFactory.getLogger(TelemetriaController.class);

    private final TelemetriaService telemetriaService;
    private final DataSource dataSource;

    public TelemetriaController(TelemetriaService telemetriaService, DataSource dataSource) {
        this.telemetriaService = telemetriaService;
        this.dataSource = dataSource;
    }

    /**
     * Ingestão de eventos vindos dos apps (best-effort, sem dados pessoais).
     * Requer sessão autenticada; o contexto de tenant/usuario é enriquecido
     * no servidor a partir do principal (evita falsificação no payload).
     */
    @PostMapping("/eventos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> registrarEventos(@Valid @RequestBody RegistrarEventosDTO dto,
                                                   Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        List<EventoIngestDTO> eventos = dto.eventos().stream()
                .limit(100)
                .toList();
        for (EventoIngestDTO evento : eventos) {
            telemetriaService.registrar(
                    evento.tipo(),
                    evento.modulo(),
                    usuario.getTenantId(),
                    usuario.getCpcId(),
                    evento.endpoint(),
                    evento.statusHttp(),
                    evento.latencyMs(),
                    evento.mensagem(),
                    evento.detalhe());
        }
        return ResponseEntity.ok(java.util.Map.of("registrados", eventos.size()));
    }

    @GetMapping("/saude")
    @PreAuthorize("hasRole('ADMIN_PLATAFORMA')")
    public ResponseEntity<SaudeDTO> saude() {
        long inicio = System.nanoTime();
        SaudeDTO saude = coletarSaude();
        long latencyMs = (System.nanoTime() - inicio) / 1_000_000L;

        telemetriaService.registrar(TipoEventoTelemetria.CONEXAO_BD, "PLATAFORMA", null, null,
                "/api/v1/telemetria/saude", "UP".equals(saude.status()) ? 200 : 503,
                latencyMs, "Verificação de saúde do banco de dados", null);

        return ResponseEntity.ok(saude);
    }

    @GetMapping("/eventos")
    @PreAuthorize("hasRole('ADMIN_PLATAFORMA')")
    public ResponseEntity<PaginaEventosDTO<EventoTelemetriaResponseDTO>> consultarEventos(
            @RequestParam(required = false) TipoEventoTelemetria tipo,
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) Instant inicio,
            @RequestParam(required = false) Instant fim,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {

        java.util.UUID tenantUuid = (tenantId == null || tenantId.isBlank())
                ? null : java.util.UUID.fromString(tenantId);

        Page<TelemetriaEvento> pagina = telemetriaService.consultar(tipo, modulo, tenantUuid,
                inicio, fim, pageable);

        return ResponseEntity.ok(PaginaEventosDTO.of(pagina.map(TelemetriaController::toDTO)));
    }

    private SaudeDTO coletarSaude() {
        String versaoPostgres = "";
        OffsetDateTime agora = OffsetDateTime.now();
        int ativo = 0, idle = 0, pendentes = 0, max = 0;

        if (dataSource instanceof HikariDataSource hikari) {
            com.zaxxer.hikari.HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
            ativo = pool.getActiveConnections();
            idle = pool.getIdleConnections();
            pendentes = pool.getThreadsAwaitingConnection();
            max = hikari.getMaximumPoolSize();
        }

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT version()")) {
                if (resultSet.next()) {
                    versaoPostgres = resultSet.getString(1);
                }
            }
            return new SaudeDTO("UP", versaoPostgres, ativo, idle, pendentes, max, agora);
        } catch (SQLException ex) {
            log.error("Falha ao verificar saúde do banco. SQLState={}", ex.getSQLState(), ex);
            return new SaudeDTO("DOWN", "indisponível", ativo, idle, pendentes, max, agora);
        }
    }

    private static EventoTelemetriaResponseDTO toDTO(TelemetriaEvento evento) {
        return new EventoTelemetriaResponseDTO(
                evento.getId(),
                evento.getTenantId(),
                evento.getUsuarioId(),
                evento.getModulo(),
                evento.getTipo(),
                evento.getEndpoint(),
                evento.getStatusHttp(),
                evento.getLatencyMs(),
                evento.getMensagem(),
                evento.getDetalhe(),
                evento.getTraceId(),
                evento.getAppVersao(),
                evento.getPlataforma(),
                evento.getCriadoEm());
    }
}