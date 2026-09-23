package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailComprovantePontoService;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.AjustarPontoManualUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.AprovarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarRelatorioEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ListarAjustesPendentesUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.RejeitarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.SolicitarAjustePontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.AjustePontoManualDTO;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.EspelhoPontoItemDTO;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.RelatorioEspelhoPontoDTO;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.SolicitarAjusteDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pontos")
@RequiresModulo("PONTO")
public class EspelhoPontoController {

    private final ConsultarEspelhoPontoUseCase consultarEspelhoPontoUseCase;
    private final ConsultarRelatorioEspelhoPontoUseCase consultarRelatorioEspelhoPontoUseCase;
    private final AjustarPontoManualUseCase ajustarPontoManualUseCase;
    private final SolicitarAjustePontoUseCase solicitarAjustePontoUseCase;
    private final AprovarAjustePontoUseCase aprovarAjustePontoUseCase;
    private final RejeitarAjustePontoUseCase rejeitarAjustePontoUseCase;
    private final ListarAjustesPendentesUseCase listarAjustesPendentesUseCase;
    private final EmailComprovantePontoService emailComprovantePontoService;
    private final AuditoriaService auditoriaService;

    public EspelhoPontoController(ConsultarEspelhoPontoUseCase consultarEspelhoPontoUseCase,
                                  ConsultarRelatorioEspelhoPontoUseCase consultarRelatorioEspelhoPontoUseCase,
                                  AjustarPontoManualUseCase ajustarPontoManualUseCase,
                                  SolicitarAjustePontoUseCase solicitarAjustePontoUseCase,
                                  AprovarAjustePontoUseCase aprovarAjustePontoUseCase,
                                  RejeitarAjustePontoUseCase rejeitarAjustePontoUseCase,
                                  ListarAjustesPendentesUseCase listarAjustesPendentesUseCase,
                                  EmailComprovantePontoService emailComprovantePontoService,
                                  AuditoriaService auditoriaService) {
        this.consultarEspelhoPontoUseCase = consultarEspelhoPontoUseCase;
        this.consultarRelatorioEspelhoPontoUseCase = consultarRelatorioEspelhoPontoUseCase;
        this.ajustarPontoManualUseCase = ajustarPontoManualUseCase;
        this.solicitarAjustePontoUseCase = solicitarAjustePontoUseCase;
        this.aprovarAjustePontoUseCase = aprovarAjustePontoUseCase;
        this.rejeitarAjustePontoUseCase = rejeitarAjustePontoUseCase;
        this.listarAjustesPendentesUseCase = listarAjustesPendentesUseCase;
        this.emailComprovantePontoService = emailComprovantePontoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/espelho")
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<List<EspelhoPontoItemDTO>> consultarEspelho(
            @RequestParam(required = false) UUID colaboradorId,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        UUID targetColaboradorId = (usuarioLogado.getRole() == Role.COLABORADOR || colaboradorId == null)
                ? usuarioLogado.getCpcId()
                : colaboradorId;

        UUID tenantId = usuarioLogado.getTenantId();

        var registros = consultarEspelhoPontoUseCase.consultar(targetColaboradorId, tenantId, mes, ano);
        return ResponseEntity.ok(registros.stream().map(EspelhoPontoItemDTO::fromDomain).toList());
    }

    @GetMapping("/espelho/relatorio")
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<RelatorioEspelhoPontoDTO> consultarRelatorioEspelho(
            @RequestParam(required = false) UUID colaboradorId,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        UUID targetColaboradorId = (usuarioLogado.getRole() == Role.COLABORADOR || colaboradorId == null)
                ? usuarioLogado.getCpcId()
                : colaboradorId;

        int mesCorrente = mes == null ? LocalDate.now().getMonthValue() : mes;
        int anoCorrente = ano == null ? LocalDate.now().getYear() : ano;

        var relatorio = consultarRelatorioEspelhoPontoUseCase.executar(
                targetColaboradorId, usuarioLogado.getTenantId(), mesCorrente, anoCorrente);
        return ResponseEntity.ok(RelatorioEspelhoPontoDTO.fromDomain(relatorio));
    }

    @PostMapping("/ajustar")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA', 'GESTOR_RH', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<EspelhoPontoItemDTO> ajustarPonto(
            @RequestBody @Valid AjustePontoManualDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        UUID targetColaboradorId = (usuarioLogado.getRole() == Role.COLABORADOR || request.colaboradorId() == null)
                ? usuarioLogado.getCpcId()
                : request.colaboradorId();

        var registro = ajustarPontoManualUseCase.executar(new AjustarPontoManualUseCase.Comando(
                targetColaboradorId,
                usuarioLogado.getTenantId(),
                usuarioLogado.getCpf(),
                request.dataHora(),
                request.tipoRegistro(),
                request.justificativa(),
                request.observacao()
        ));

        auditoriaService.registrar("AJUSTE_PONTO", "REGISTRO_PONTO", registro.getId(),
                "Ajuste manual de ponto (" + request.tipoRegistro() + "): " + request.justificativa(),
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, null, null);

        String email = usuarioLogado.getEmailCorporativo() != null ? usuarioLogado.getEmailCorporativo() : usuarioLogado.getEmailPessoal();
        if (emailComprovantePontoService != null && email != null && !email.isBlank()) {
            emailComprovantePontoService.enviarComprovantePontoAsync(
                    email, usuarioLogado.getNome(), registro, usuarioLogado.getCpf(), "Chronos Pulse"
            );
        }

        return ResponseEntity.ok(EspelhoPontoItemDTO.fromDomain(registro));
    }

    @PostMapping("/ajustar/solicitar")
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ADMIN_PLATAFORMA')")
    public ResponseEntity<EspelhoPontoItemDTO> solicitarAjuste(
            @RequestBody @Valid SolicitarAjusteDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        UUID targetColaboradorId = (usuarioLogado.getRole() == Role.COLABORADOR || request.colaboradorId() == null)
                ? usuarioLogado.getCpcId()
                : request.colaboradorId();

        var registro = solicitarAjustePontoUseCase.executar(new SolicitarAjustePontoUseCase.Comando(
                targetColaboradorId,
                usuarioLogado.getTenantId(),
                usuarioLogado.getCpf(),
                request.dataHora(),
                request.tipoRegistro(),
                request.justificativa(),
                request.observacao()
        ));

        auditoriaService.registrar("SOLICITACAO_AJUSTE_PONTO", "REGISTRO_PONTO", registro.getId(),
                "Solicitação de ajuste de ponto (" + request.tipoRegistro() + "): " + request.justificativa(),
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, null, null);

        // Envia e-mail de confirmação para o colaborador
        String emailColaborador = "jess.dantas.it@outlook.com"; // Conforme solicitado
        if (emailComprovantePontoService != null && emailColaborador != null && !emailColaborador.isBlank()) {
            emailComprovantePontoService.enviarComprovantePontoAsync(
                    emailColaborador, usuarioLogado.getNome(), registro, usuarioLogado.getCpf(), "Chronos Pulse - Solicitação de Ajuste"
            );
        }

        return ResponseEntity.ok(EspelhoPontoItemDTO.fromDomain(registro));
    }

    @GetMapping("/ajustes/pendentes")
    @PreAuthorize("hasAnyRole('GESTOR_RH', 'ADMIN_EMPRESA')")
    public ResponseEntity<List<EspelhoPontoItemDTO>> listarAjustesPendentes(
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        var ajustes = listarAjustesPendentesUseCase.executar(new ListarAjustesPendentesUseCase.Comando(
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId()
        ));

        return ResponseEntity.ok(ajustes.stream().map(EspelhoPontoItemDTO::fromDomain).toList());
    }

    @PutMapping("/ajustes/{id}/aprovar")
    @PreAuthorize("hasAnyRole('GESTOR_RH', 'ADMIN_EMPRESA')")
    public ResponseEntity<EspelhoPontoItemDTO> aprovarAjuste(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        var registro = aprovarAjustePontoUseCase.executar(new AprovarAjustePontoUseCase.Comando(
                id, usuarioLogado.getCpcId(), usuarioLogado.getTenantId()
        ));

        auditoriaService.registrar("APROVACAO_AJUSTE_PONTO", "REGISTRO_PONTO", registro.getId(),
                "Aprovação de ajuste de ponto",
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, null, null);

        // Envia e-mail de confirmação para o colaborador
        String emailColaborador = "jess.dantas.it@outlook.com";
        if (emailComprovantePontoService != null && emailColaborador != null && !emailColaborador.isBlank()) {
            emailComprovantePontoService.enviarComprovantePontoAsync(
                    emailColaborador, usuarioLogado.getNome(), registro, usuarioLogado.getCpf(), "Chronos Pulse - Ajuste Aprovado"
            );
        }

        return ResponseEntity.ok(EspelhoPontoItemDTO.fromDomain(registro));
    }

    @PutMapping("/ajustes/{id}/rejeitar")
    @PreAuthorize("hasAnyRole('GESTOR_RH', 'ADMIN_EMPRESA')")
    public ResponseEntity<EspelhoPontoItemDTO> rejeitarAjuste(
            @PathVariable UUID id,
            @RequestParam String motivo,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        var registro = rejeitarAjustePontoUseCase.executar(new RejeitarAjustePontoUseCase.Comando(
                id, usuarioLogado.getCpcId(), usuarioLogado.getTenantId(), motivo
        ));

        auditoriaService.registrar("REJEICAO_AJUSTE_PONTO", "REGISTRO_PONTO", registro.getId(),
                "Rejeição de ajuste de ponto: " + motivo,
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, null, null);

        // Envia e-mail de notificação para o colaborador
        String emailColaborador = "jess.dantas.it@outlook.com";
        if (emailComprovantePontoService != null && emailColaborador != null && !emailColaborador.isBlank()) {
            emailComprovantePontoService.enviarComprovantePontoAsync(
                    emailColaborador, usuarioLogado.getNome(), registro, usuarioLogado.getCpf(), "Chronos Pulse - Ajuste Rejeitado"
            );
        }

        return ResponseEntity.ok(EspelhoPontoItemDTO.fromDomain(registro));
    }
}
