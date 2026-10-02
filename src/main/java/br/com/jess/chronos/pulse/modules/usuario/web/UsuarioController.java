package br.com.jess.chronos.pulse.modules.usuario.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/// Gestão de contas administrativas da empresa (listar/criar/suspender).
/// Restrita ao ADMIN_EMPRESA (administração da conta titular da empresa);
/// os vínculos de módulo por usuário seguem em `/usuarios/{id}/modulos`
/// ([br.com.jess.chronos.pulse.modules.modulo.web.UsuarioModuloController]).
@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMIN_EMPRESA')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public record CriarUsuarioRequest(
            @NotBlank String cpf,
            @NotBlank String nome,
            String emailCorporativo,
            @NotBlank String senha,
            @NotBlank String papel) {}

    @GetMapping
    public ResponseEntity<List<UsuarioService.UsuarioItem>> listar(
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        return ResponseEntity.ok(usuarioService.listar(usuarioLogado.getTenantId()));
    }

    @PostMapping
    public ResponseEntity<UsuarioService.UsuarioItem> criar(
            @RequestBody @Valid CriarUsuarioRequest request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        var item = usuarioService.criar(
                new UsuarioService.CriarUsuario(
                        request.cpf(), request.nome(), request.emailCorporativo(),
                        request.senha(), request.papel()),
                usuarioLogado.getTenantId(), usuarioLogado);
        return ResponseEntity.ok(item);
    }

    @PatchMapping("/{id}/suspender")
    public ResponseEntity<UsuarioService.UsuarioItem> suspender(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        return ResponseEntity.ok(
                usuarioService.suspender(id, usuarioLogado.getTenantId(), usuarioLogado));
    }
}
