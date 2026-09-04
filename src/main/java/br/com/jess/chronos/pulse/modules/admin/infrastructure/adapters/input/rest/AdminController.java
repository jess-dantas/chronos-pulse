package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AdicionarEventoContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.CadastrarContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.DashboardMetricsUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.ListarContratosUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.ListarEventosContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.*;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ListarColaboradoresUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'SUPORTE_N1', 'SUPORTE_N2')")
public class AdminController {

    private final CadastrarContratoUseCase cadastrarContratoUseCase;
    private final ListarContratosUseCase listarContratosUseCase;
    private final AdicionarEventoContratoUseCase adicionarEventoContratoUseCase;
    private final ListarEventosContratoUseCase listarEventosContratoUseCase;
    private final DashboardMetricsUseCase dashboardMetricsUseCase;
    private final ListarColaboradoresUseCase listarColaboradoresUseCase;
    private final EmpresaRepositoryPort empresaRepositoryPort;

    public AdminController(
            CadastrarContratoUseCase cadastrarContratoUseCase,
            ListarContratosUseCase listarContratosUseCase,
            AdicionarEventoContratoUseCase adicionarEventoContratoUseCase,
            ListarEventosContratoUseCase listarEventosContratoUseCase,
            DashboardMetricsUseCase dashboardMetricsUseCase,
            ListarColaboradoresUseCase listarColaboradoresUseCase,
            EmpresaRepositoryPort empresaRepositoryPort) {
        this.cadastrarContratoUseCase = cadastrarContratoUseCase;
        this.listarContratosUseCase = listarContratosUseCase;
        this.adicionarEventoContratoUseCase = adicionarEventoContratoUseCase;
        this.listarEventosContratoUseCase = listarEventosContratoUseCase;
        this.dashboardMetricsUseCase = dashboardMetricsUseCase;
        this.listarColaboradoresUseCase = listarColaboradoresUseCase;
        this.empresaRepositoryPort = empresaRepositoryPort;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(dashboardMetricsUseCase.executar());
    }

    @GetMapping("/colaboradores")
    public ResponseEntity<List<AdminColaboradorResponseDTO>> listarColaboradores() {
        var itens = listarColaboradoresUseCase.executar(null);
        List<AdminColaboradorResponseDTO> resultado = itens.stream().map(item -> {
            String tenantNome = item.tenantId() != null
                    ? empresaRepositoryPort.buscarPorId(item.tenantId())
                            .map(e -> e.getNome())
                            .orElse("—")
                    : "—";
            return AdminColaboradorResponseDTO.fromItem(item, tenantNome);
        }).toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/contratos")
    public ResponseEntity<List<ContratoResponseDTO>> listarContratos(
            @RequestParam(required = false) UUID tenantId) {
        var contratos = listarContratosUseCase.executar(tenantId);
        return ResponseEntity.ok(contratos.stream().map(ContratoResponseDTO::fromDomain).toList());
    }

    @PostMapping("/contratos")
    public ResponseEntity<ContratoResponseDTO> cadastrarContrato(
            @RequestBody @Valid CadastrarContratoRequestDTO request) {
        var contrato = cadastrarContratoUseCase.executar(new CadastrarContratoUseCase.Comando(
                request.tenantId(),
                request.numero(),
                request.objeto(),
                request.dataInicio(),
                request.dataFim(),
                request.valorMensal(),
                request.valorTotal(),
                request.observacoes()
        ));
        return ResponseEntity.ok(ContratoResponseDTO.fromDomain(contrato));
    }

    @PostMapping("/contratos/eventos")
    public ResponseEntity<ContratoEventoResponseDTO> adicionarEvento(
            @RequestBody @Valid AdicionarEventoContratoRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        var evento = adicionarEventoContratoUseCase.executar(new AdicionarEventoContratoUseCase.Comando(
                request.contratoId(),
                request.tipo(),
                request.descricao(),
                usuarioLogado.getCpcId()
        ));
        return ResponseEntity.ok(ContratoEventoResponseDTO.fromDomain(evento));
    }

    @GetMapping("/contratos/{contratoId}/eventos")
    public ResponseEntity<List<ContratoEventoResponseDTO>> listarEventos(
            @PathVariable UUID contratoId) {
        var eventos = listarEventosContratoUseCase.executar(contratoId);
        return ResponseEntity.ok(eventos.stream().map(ContratoEventoResponseDTO::fromDomain).toList());
    }
}
