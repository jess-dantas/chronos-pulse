package br.com.jess.chronos.pulse.modules.patrimonio.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import br.com.jess.chronos.pulse.modules.patrimonio.service.TransferenciaService;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.SolicitarTransferenciaDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.TransferenciaResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patrimonio/transferencias")
@RequiredArgsConstructor
@RequiresModulo(codigo = "PATRIMONIO")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;
    private final AuditoriaService auditoriaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<TransferenciaResponseDTO> solicitar(
            @Valid @RequestBody SolicitarTransferenciaDTO dto,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        TransferenciaResponseDTO response = transferenciaService.solicitar(dto, usuario.getTenantId(), usuario.getNome());
        auditoriaService.registrar("SOLICITACAO_TRANSFERENCIA", "TRANSFERENCIA_PATRIMONIO", response.id(),
                "Solicitação de transferência de bem patrimonial",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<TransferenciaResponseDTO> confirmar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        TransferenciaResponseDTO response = transferenciaService.confirmar(id, usuario.getTenantId(), usuario.getNome());
        auditoriaService.registrar("CONFIRMACAO_TRANSFERENCIA", "TRANSFERENCIA_PATRIMONIO", response.id(),
                "Confirmação de transferência com atualização da localização do bem",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), "SOLICITADA", "CONFIRMADA", request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<Void> cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        TransferenciaResponseDTO response = transferenciaService.cancelar(id, usuario.getTenantId());
        auditoriaService.registrar("CANCELAMENTO_TRANSFERENCIA", "TRANSFERENCIA_PATRIMONIO", response.id(),
                "Cancelamento de transferência de bem patrimonial",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, "CANCELADA", request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<List<TransferenciaResponseDTO>> listar(@AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(transferenciaService.listar(usuario.getTenantId()));
    }
}