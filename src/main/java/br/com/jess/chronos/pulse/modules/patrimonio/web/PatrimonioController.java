package br.com.jess.chronos.pulse.modules.patrimonio.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import br.com.jess.chronos.pulse.modules.patrimonio.service.PatrimonioService;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CadastrarPatrimonioDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.PatrimonioResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patrimonio")
@RequiredArgsConstructor
@RequiresModulo(codigo = "PATRIMONIO")
public class PatrimonioController {

    private final PatrimonioService patrimonioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<PatrimonioResponseDTO> cadastrar(
            @Valid @RequestBody CadastrarPatrimonioDTO dto,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PatrimonioResponseDTO response = patrimonioService.cadastrar(dto, usuario.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<PatrimonioResponseDTO>> listar(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(patrimonioService.listar(usuario.getTenantId(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<PatrimonioResponseDTO> buscarPorId(
            @PathVariable UUID id,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(patrimonioService.buscarPorId(id, usuario.getTenantId()));
    }

    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<List<PatrimonioResponseDTO>> listarAtivos(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(patrimonioService.listarAtivos(usuario.getTenantId()));
    }
}
