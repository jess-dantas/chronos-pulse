package br.com.jess.chronos.pulse.modules.estoque.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.estoque.service.MaterialService;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.*;
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
@RequestMapping("/api/v1/estoque")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    // --- Materiais ---

    @PostMapping("/materiais")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<MaterialResponseDTO> cadastrarMaterial(
            @Valid @RequestBody CadastrarMaterialDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        MaterialResponseDTO response = materialService.cadastrarMaterial(dto, usuario.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/materiais")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<MaterialResponseDTO>> listarMateriais(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        Page<MaterialResponseDTO> materiais = materialService.listarMateriais(usuario.getTenantId(), pageable);
        return ResponseEntity.ok(materiais);
    }

    @GetMapping("/materiais/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<MaterialResponseDTO> buscarMaterialPorId(
            @PathVariable UUID id,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        MaterialResponseDTO material = materialService.buscarMaterialPorId(id, usuario.getTenantId());
        return ResponseEntity.ok(material);
    }

    // --- Grupos de Materiais ---

    @PostMapping("/grupos")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<MaterialGrupoResponseDTO> cadastrarGrupo(
            @Valid @RequestBody CadastrarMaterialGrupoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        MaterialGrupoResponseDTO response = materialService.cadastrarGrupo(dto, usuario.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/grupos")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<List<MaterialGrupoResponseDTO>> listarGrupos(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        List<MaterialGrupoResponseDTO> grupos = materialService.listarGrupos(usuario.getTenantId());
        return ResponseEntity.ok(grupos);
    }

    // --- Almoxarifados ---

    @PostMapping("/almoxarifados")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<AlmoxarifadoResponseDTO> cadastrarAlmoxarifado(
            @Valid @RequestBody CadastrarAlmoxarifadoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        AlmoxarifadoResponseDTO response = materialService.cadastrarAlmoxarifado(dto, usuario.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/almoxarifados")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<List<AlmoxarifadoResponseDTO>> listarAlmoxarifados(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        List<AlmoxarifadoResponseDTO> almoxarifados = materialService.listarAlmoxarifados(usuario.getTenantId());
        return ResponseEntity.ok(almoxarifados);
    }
}
