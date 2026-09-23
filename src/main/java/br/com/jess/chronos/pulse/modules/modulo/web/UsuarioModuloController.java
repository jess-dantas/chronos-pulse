package br.com.jess.chronos.pulse.modules.modulo.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.modulo.service.UsuarioModuloService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios/{usuarioId}/modulos")
@PreAuthorize("hasAnyRole('ADMIN_EMPRESA', 'GESTOR_RH', 'ADMIN_PLATAFORMA')")
public class UsuarioModuloController {

    private final UsuarioModuloService usuarioModuloService;

    public UsuarioModuloController(UsuarioModuloService usuarioModuloService) {
        this.usuarioModuloService = usuarioModuloService;
    }

    public record AtualizarModulosUsuarioRequest(@NotNull UUID tenantId, @NotEmpty List<String> codigos) {}

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(
            @PathVariable UUID usuarioId,
            @RequestParam UUID tenantId,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        return ResponseEntity.ok(usuarioModuloService.listar(usuarioId, tenantId, usuarioLogado));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> atualizar(
            @PathVariable UUID usuarioId,
            @RequestBody @Valid AtualizarModulosUsuarioRequest request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        return ResponseEntity.ok(usuarioModuloService.atualizar(
                usuarioId, request.tenantId(), request.codigos(), usuarioLogado));
    }
}
