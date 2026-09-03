package br.com.jess.chronos.pulse.modules.estoque.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.RequisicaoStatus;
import br.com.jess.chronos.pulse.modules.estoque.service.RequisicaoService;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.CriarRequisicaoDTO;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.RequisicaoResponseDTO;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/estoque/requisicoes")
@RequiredArgsConstructor
public class RequisicaoController {

    private final RequisicaoService requisicaoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<RequisicaoResponseDTO> criarRequisicao(
            @Valid @RequestBody CriarRequisicaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        RequisicaoResponseDTO response = requisicaoService.criarRequisicao(dto, usuario.getTenantId(), usuario.getCpcId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<RequisicaoResponseDTO>> listarRequisicoes(
            @RequestParam(required = false) RequisicaoStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        Page<RequisicaoResponseDTO> page = requisicaoService.listarRequisicoes(usuario.getTenantId(), status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<RequisicaoResponseDTO> buscarPorId(
            @PathVariable UUID id,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        RequisicaoResponseDTO response = requisicaoService.buscarPorId(id, usuario.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<RequisicaoResponseDTO> aprovarRequisicao(
            @PathVariable UUID id,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        RequisicaoResponseDTO response = requisicaoService.aprovarRequisicao(id, usuario.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/atender")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<RequisicaoResponseDTO> atenderRequisicao(
            @PathVariable UUID id,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        RequisicaoResponseDTO response = requisicaoService.atenderRequisicao(id, usuario.getTenantId(), usuario.getCpcId());
        return ResponseEntity.ok(response);
    }
}
