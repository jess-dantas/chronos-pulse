package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailComprovantePontoService;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.AjustarPontoManualUseCase;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarEspelhoPontoUseCase;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.AjustePontoManualDTO;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto.EspelhoPontoItemDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pontos")
public class EspelhoPontoController {

    private final ConsultarEspelhoPontoUseCase consultarEspelhoPontoUseCase;
    private final AjustarPontoManualUseCase ajustarPontoManualUseCase;
    private final EmailComprovantePontoService emailComprovantePontoService;

    public EspelhoPontoController(ConsultarEspelhoPontoUseCase consultarEspelhoPontoUseCase,
                                  AjustarPontoManualUseCase ajustarPontoManualUseCase,
                                  EmailComprovantePontoService emailComprovantePontoService) {
        this.consultarEspelhoPontoUseCase = consultarEspelhoPontoUseCase;
        this.ajustarPontoManualUseCase = ajustarPontoManualUseCase;
        this.emailComprovantePontoService = emailComprovantePontoService;
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

    @PostMapping("/ajustar")
    @PreAuthorize("hasAnyRole('COLABORADOR', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ADMIN_PLATAFORMA')")
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

        String email = usuarioLogado.getEmailCorporativo() != null ? usuarioLogado.getEmailCorporativo() : usuarioLogado.getEmailPessoal();
        if (emailComprovantePontoService != null && email != null && !email.isBlank()) {
            emailComprovantePontoService.enviarComprovantePontoAsync(
                    email, usuarioLogado.getNome(), registro, usuarioLogado.getCpf(), "Chronos Pulse"
            );
        }

        return ResponseEntity.ok(EspelhoPontoItemDTO.fromDomain(registro));
    }
}
