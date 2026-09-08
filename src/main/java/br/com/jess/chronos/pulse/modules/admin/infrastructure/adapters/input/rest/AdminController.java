package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AdicionarEventoContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.CadastrarContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.DashboardMetricsUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.ListarContratosUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.ListarEventosContratoUseCase;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.*;
import br.com.jess.chronos.pulse.modules.auditoria.domain.entity.Auditoria;
import br.com.jess.chronos.pulse.modules.auditoria.repository.AuditoriaRepository;
import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ListarColaboradoresUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
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
    private final AuditoriaRepository auditoriaRepository;
    private final AuditoriaService auditoriaService;

    public AdminController(
            CadastrarContratoUseCase cadastrarContratoUseCase,
            ListarContratosUseCase listarContratosUseCase,
            AdicionarEventoContratoUseCase adicionarEventoContratoUseCase,
            ListarEventosContratoUseCase listarEventosContratoUseCase,
            DashboardMetricsUseCase dashboardMetricsUseCase,
            ListarColaboradoresUseCase listarColaboradoresUseCase,
            EmpresaRepositoryPort empresaRepositoryPort,
            AuditoriaRepository auditoriaRepository,
            AuditoriaService auditoriaService) {
        this.cadastrarContratoUseCase = cadastrarContratoUseCase;
        this.listarContratosUseCase = listarContratosUseCase;
        this.adicionarEventoContratoUseCase = adicionarEventoContratoUseCase;
        this.listarEventosContratoUseCase = listarEventosContratoUseCase;
        this.dashboardMetricsUseCase = dashboardMetricsUseCase;
        this.listarColaboradoresUseCase = listarColaboradoresUseCase;
        this.empresaRepositoryPort = empresaRepositoryPort;
        this.auditoriaRepository = auditoriaRepository;
        this.auditoriaService = auditoriaService;
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
            @RequestBody @Valid CadastrarContratoRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
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
        auditoriaService.registrar("CADASTRO", "CONTRATO", contrato.getId(),
                "Cadastro de contrato " + request.numero(),
                request.tenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, null, null);
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

    @GetMapping("/auditoria")
    public ResponseEntity<Map<String, Object>> listarAuditoria(
            @RequestParam(required = false) UUID tenantId) {
        List<Auditoria> registros = tenantId != null
                ? auditoriaRepository.findByEntidade("TENANT", tenantId.toString())
                : auditoriaRepository.findRecentes(tenantId);
        List<Map<String, Object>> itens = registros.stream().limit(500).map(a -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("tenantId", a.getTenantId());
            item.put("usuarioCpf", a.getUsuarioCpf() != null && a.getUsuarioCpf().length() >= 6
                    ? a.getUsuarioCpf().replaceAll("(?<=^.{3}).(?=.{2}$)", "*") : a.getUsuarioCpf());
            item.put("papel", a.getPapel());
            item.put("acao", a.getAcao());
            item.put("entidade", a.getEntidade());
            item.put("entidadeId", a.getEntidadeId());
            item.put("descricao", a.getDescricao());
            item.put("dataHora", a.getDataHora());
            item.put("hashRegistro", a.getHashRegistro());
            item.put("hashAnterior", a.getHashAnterior());
            return item;
        }).toList();
        return ResponseEntity.ok(Map.of(
                "total", itens.size(),
                "itens", itens
        ));
    }
}
