package br.com.jess.chronos.pulse.modules.patrimonio.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import br.com.jess.chronos.pulse.modules.patrimonio.service.PatrimonioService;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CadastrarPatrimonioDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.PatrimonioResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patrimonio")
@RequiredArgsConstructor
@RequiresModulo(codigo = "PATRIMONIO")
public class PatrimonioController {

    private final PatrimonioService patrimonioService;
    private final AuditoriaService auditoriaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<PatrimonioResponseDTO> cadastrar(
            @Valid @RequestBody CadastrarPatrimonioDTO dto,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        PatrimonioResponseDTO response = patrimonioService.cadastrar(dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO", "PATRIMONIO", response.id(),
                "Cadastro de bem patrimonial",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<PatrimonioResponseDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CadastrarPatrimonioDTO dto,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        PatrimonioResponseDTO response = patrimonioService.atualizar(id, dto, usuario.getTenantId());
        auditoriaService.registrar("ATUALIZACAO", "PATRIMONIO", response.id(),
                "Atualização de bem patrimonial",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<Void> desativar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        PatrimonioResponseDTO response = patrimonioService.desativar(id, usuario.getTenantId());
        auditoriaService.registrar("INATIVACAO", "PATRIMONIO", response.id(),
                "Inativação de bem patrimonial",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), "true", "false", request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<PatrimonioResponseDTO>> listar(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(patrimonioService.listar(usuario.getTenantId(), pageable));
    }

    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<List<PatrimonioResponseDTO>> listarAtivos(@AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(patrimonioService.listarAtivos(usuario.getTenantId()));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<List<PatrimonioResponseDTO>> buscar(
            @RequestParam("q") String q,
            @AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(patrimonioService.buscar(q, usuario.getTenantId()));
    }

    @GetMapping("/qrcode/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<PatrimonioResponseDTO> buscarPorQrCode(
            @PathVariable String codigo,
            @AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(patrimonioService.buscarPorQrCode(codigo, usuario.getTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<PatrimonioResponseDTO> buscarPorId(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(patrimonioService.buscarPorId(id, usuario.getTenantId()));
    }
}