package br.com.jess.chronos.pulse.modules.protocolo.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import br.com.jess.chronos.pulse.modules.protocolo.service.ProtocoloService;
import br.com.jess.chronos.pulse.modules.protocolo.web.dto.AtualizarStatusProtocoloDTO;
import br.com.jess.chronos.pulse.modules.protocolo.web.dto.CadastrarProtocoloDTO;
import br.com.jess.chronos.pulse.modules.protocolo.web.dto.ProtocoloResponseDTO;
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
@RequestMapping("/api/v1/protocolo")
@RequiredArgsConstructor
@RequiresModulo(codigo = "PROTOCOLO")
public class ProtocoloController {

    private final ProtocoloService protocoloService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<ProtocoloResponseDTO> cadastrar(
            @Valid @RequestBody CadastrarProtocoloDTO dto,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        ProtocoloResponseDTO response = protocoloService.cadastrar(dto, usuario.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<ProtocoloResponseDTO>> listar(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(protocoloService.listar(usuario.getTenantId(), pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<ProtocoloResponseDTO> buscarPorId(
            @PathVariable UUID id,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(protocoloService.buscarPorId(id, usuario.getTenantId()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<ProtocoloResponseDTO> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusProtocoloDTO dto,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        ProtocoloResponseDTO response = protocoloService.atualizarStatus(id, dto, usuario.getTenantId());
        return ResponseEntity.ok(response);
    }
}
