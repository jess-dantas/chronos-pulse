package br.com.jess.chronos.pulse.modules.patrimonio.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import br.com.jess.chronos.pulse.modules.patrimonio.service.DesfazimentoService;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.AprovarDesfazimentoDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CadastrarDesfazimentoDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.DesfazimentoResponseDTO;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patrimonio/desfazimentos")
@RequiredArgsConstructor
@RequiresModulo(codigo = "PATRIMONIO")
public class DesfazimentoController {

    private final DesfazimentoService desfazimentoService;
    private final AuditoriaService auditoriaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<DesfazimentoResponseDTO> cadastrar(
            @Valid @RequestBody CadastrarDesfazimentoDTO dto,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        DesfazimentoResponseDTO response = desfazimentoService.cadastrar(dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO", "DESFAZIMENTO", response.id(),
                "Solicitação de desfazimento de bem patrimonial",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), null, null, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<DesfazimentoResponseDTO>> listar(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CpcUsuario usuario) {
        return ResponseEntity.ok(desfazimentoService.listar(usuario.getTenantId(), pageable));
    }

    @PatchMapping("/{id}/aprovar")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<DesfazimentoResponseDTO> aprovar(
            @PathVariable UUID id,
            @RequestBody(required = false) AprovarDesfazimentoDTO body,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        String parecer = body != null ? body.parecerComissao() : null;
        DesfazimentoResponseDTO response = desfazimentoService.aprovar(id, usuario.getTenantId(), parecer);
        auditoriaService.registrar("APROVACAO", "DESFAZIMENTO", response.id(),
                "Aprovação de desfazimento com baixa patrimonial",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), "EM_ANALISE", "APROVADO", request.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<Void> cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuario,
            HttpServletRequest request) {
        DesfazimentoResponseDTO response = desfazimentoService.cancelar(id, usuario.getTenantId());
        auditoriaService.registrar("CANCELAMENTO", "DESFAZIMENTO", response.id(),
                "Cancelamento de solicitação de desfazimento",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getRole().name(), "EM_ANALISE", "CANCELADO", request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}