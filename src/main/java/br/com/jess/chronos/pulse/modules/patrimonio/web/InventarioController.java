package br.com.jess.chronos.pulse.modules.patrimonio.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import br.com.jess.chronos.pulse.modules.patrimonio.service.InventarioService;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.ConferirItemDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CriarInventarioDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.InventarioResponseDTO;
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
@RequestMapping("/api/v1/patrimonio/inventarios")
@RequiredArgsConstructor
@RequiresModulo(codigo = "PATRIMONIO")
public class InventarioController {

    private final InventarioService inventarioService;
    private final AuditoriaService auditoriaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<InventarioResponseDTO> criar(
            @Valid @RequestBody CriarInventarioDTO dto,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        InventarioResponseDTO response = inventarioService.criar(dto, usuario.getTenantId(), usuario.getNome());
        auditoriaService.registrar("CADASTRO_INVENTARIO", "INVENTARIO", response.id(),
                "Abertura de sessão de inventário físico",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/conferir")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<InventarioResponseDTO> conferirItem(
            @PathVariable UUID id,
            @Valid @RequestBody ConferirItemDTO dto,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        InventarioResponseDTO response = inventarioService.conferirItem(id, dto, usuario.getTenantId(), usuario.getNome());
        auditoriaService.registrar("CONFERENCIA_INVENTARIO", "INVENTARIO", response.id(),
                "Conferência de item de inventário",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<InventarioResponseDTO> finalizar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        InventarioResponseDTO response = inventarioService.finalizar(id, usuario.getTenantId());
        auditoriaService.registrar("FINALIZACAO_INVENTARIO", "INVENTARIO", response.id(),
                "Conclusão de sessão de inventário físico",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), "EM_ANDAMENTO", "CONCLUIDO", request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<Void> cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        InventarioResponseDTO response = inventarioService.cancelar(id, usuario.getTenantId());
        auditoriaService.registrar("CANCELAMENTO_INVENTARIO", "INVENTARIO", response.id(),
                "Cancelamento de sessão de inventário físico",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), "EM_ANDAMENTO", "CANCELADO", request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<List<InventarioResponseDTO>> listar(@AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(inventarioService.listar(usuario.getTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<InventarioResponseDTO> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(inventarioService.buscarPorId(id, usuario.getTenantId()));
    }
}