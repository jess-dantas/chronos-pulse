package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.titularidade.domain.ports.input.TransferirTitularidadeUseCase;
import br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest.dto.BiometriaTitularidadeRequestDTO;
import br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest.dto.CodigoTitularidadeRequestDTO;
import br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest.dto.IniciarTitularidadeRequestDTO;
import br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.input.rest.dto.VerificarCelularTitularidadeRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Transferência de titularidade da empresa. Todas as rotas exigem
 * ADMIN_EMPRESA (URL rule + @PreAuthorize); tenant e solicitante vêm da
 * sessão — nunca do corpo da requisição.
 */
@RestController
@RequestMapping("/api/v1/titularidade")
public class TitularidadeController {

    private final TransferirTitularidadeUseCase transferirTitularidadeUseCase;
    private final AuditoriaService auditoriaService;

    public TitularidadeController(TransferirTitularidadeUseCase transferirTitularidadeUseCase,
                                  AuditoriaService auditoriaService) {
        this.transferirTitularidadeUseCase = transferirTitularidadeUseCase;
        this.auditoriaService = auditoriaService;
    }

    @PostMapping("/iniciar")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA')")
    public ResponseEntity<Map<String, Object>> iniciar(
            @RequestBody @Valid IniciarTitularidadeRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = exigirTenant(usuarioLogado);

        var iniciado = transferirTitularidadeUseCase.iniciar(
                new TransferirTitularidadeUseCase.IniciarComando(
                        usuarioLogado.getId(), tenantId, request.novoTitularId()));

        auditoriaService.registrar("TRANSFERENCIA", "TITULARIDADE",
                iniciado.transferenciaId(),
                "Início de transferência de titularidade para "
                        + iniciado.novoTitularNome(),
                tenantId, usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null,
                "novoTitularId=" + request.novoTitularId(), null);

        return ResponseEntity.ok(Map.of(
                "transferenciaId", iniciado.transferenciaId(),
                "novoTitularNome", iniciado.novoTitularNome() != null
                        ? iniciado.novoTitularNome() : "",
                "novoTitularCelular", iniciado.novoTitularCelular() != null
                        ? iniciado.novoTitularCelular() : ""));
    }

    @PostMapping("/{id}/etapa/biometria")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA')")
    public ResponseEntity<Map<String, String>> confirmarBiometria(
            @PathVariable UUID id,
            @RequestBody @Valid BiometriaTitularidadeRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = exigirTenant(usuarioLogado);

        transferirTitularidadeUseCase.confirmarBiometria(
                new TransferirTitularidadeUseCase.BiometriaComando(
                        id, usuarioLogado.getId(), tenantId,
                        Boolean.TRUE.equals(request.confirmado())));

        return ResponseEntity.ok(Map.of("mensagem", "Biometria confirmada."));
    }

    @PostMapping("/{id}/etapa/celular/enviar")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA')")
    public ResponseEntity<Map<String, String>> enviarCodigoCelular(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = exigirTenant(usuarioLogado);

        var enviado = transferirTitularidadeUseCase.enviarCodigo(
                new TransferirTitularidadeUseCase.EnviarCodigoComando(
                        id, usuarioLogado.getId(), tenantId,
                        TransferirTitularidadeUseCase.ETAPA_CELULAR));

        return ResponseEntity.ok(Map.of(
                "mensagem", "Código enviado para o e-mail do titular atual.",
                "destino", enviado.destino()));
    }

    @PostMapping("/{id}/etapa/celular/verificar")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA')")
    public ResponseEntity<Map<String, String>> verificarCelular(
            @PathVariable UUID id,
            @RequestBody @Valid VerificarCelularTitularidadeRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = exigirTenant(usuarioLogado);

        transferirTitularidadeUseCase.verificarCodigo(
                new TransferirTitularidadeUseCase.VerificarCodigoComando(
                        id, usuarioLogado.getId(), tenantId,
                        TransferirTitularidadeUseCase.ETAPA_CELULAR,
                        request.codigo(), request.celularConfirmado()));

        return ResponseEntity.ok(Map.of("mensagem", "Celular confirmado e código validado."));
    }

    @PostMapping("/{id}/etapa/email/enviar")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA')")
    public ResponseEntity<Map<String, String>> enviarCodigoEmail(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = exigirTenant(usuarioLogado);

        var enviado = transferirTitularidadeUseCase.enviarCodigo(
                new TransferirTitularidadeUseCase.EnviarCodigoComando(
                        id, usuarioLogado.getId(), tenantId,
                        TransferirTitularidadeUseCase.ETAPA_EMAIL));

        return ResponseEntity.ok(Map.of(
                "mensagem", "Código enviado para o e-mail corporativo do novo titular.",
                "destino", enviado.destino()));
    }

    @PostMapping("/{id}/etapa/email/verificar")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA')")
    public ResponseEntity<Map<String, String>> verificarEmail(
            @PathVariable UUID id,
            @RequestBody @Valid CodigoTitularidadeRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = exigirTenant(usuarioLogado);

        transferirTitularidadeUseCase.verificarCodigo(
                new TransferirTitularidadeUseCase.VerificarCodigoComando(
                        id, usuarioLogado.getId(), tenantId,
                        TransferirTitularidadeUseCase.ETAPA_EMAIL,
                        request.codigo(), null));

        return ResponseEntity.ok(Map.of("mensagem", "E-mail do novo titular confirmado."));
    }

    @PostMapping("/{id}/concluir")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA')")
    public ResponseEntity<Map<String, String>> concluir(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = exigirTenant(usuarioLogado);

        transferirTitularidadeUseCase.concluir(
                new TransferirTitularidadeUseCase.Comando(
                        id, usuarioLogado.getId(), tenantId));

        auditoriaService.registrar("TRANSFERENCIA", "TITULARIDADE", id,
                "Transferência de titularidade concluída",
                tenantId, usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, "status=CONCLUIDA", null);

        return ResponseEntity.ok(Map.of(
                "mensagem",
                "Titularidade transferida. Sessão encerrada — o novo titular "
                        + "deve entrar novamente para refletir o novo papel."));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN_EMPRESA')")
    public ResponseEntity<Map<String, String>> cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = exigirTenant(usuarioLogado);

        transferirTitularidadeUseCase.cancelar(
                new TransferirTitularidadeUseCase.Comando(
                        id, usuarioLogado.getId(), tenantId));

        auditoriaService.registrar("TRANSFERENCIA", "TITULARIDADE", id,
                "Transferência de titularidade cancelada",
                tenantId, usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, "status=CANCELADA", null);

        return ResponseEntity.ok(Map.of("mensagem", "Transferência cancelada."));
    }

    private UUID exigirTenant(CpcUsuario usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getTenantId() == null) {
            throw new IllegalArgumentException("Sessão inválida para transferência de titularidade.");
        }
        return usuarioLogado.getTenantId();
    }
}
