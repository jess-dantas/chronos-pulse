package br.com.jess.chronos.pulse.modules.estoque.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.estoque.service.EstoqueConsultaService;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.EstoqueSaldoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/estoque/saldos")
@RequiredArgsConstructor
public class EstoqueConsultaController {

    private final EstoqueConsultaService consultaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<EstoqueSaldoResponseDTO>> listarSaldos(
            @RequestParam(required = false) UUID almoxarifadoId,
            @RequestParam(required = false) UUID grupoId,
            @RequestParam(required = false) Boolean abaixoMinimo,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        UUID tenantId = usuario.getTenantId();

        Page<EstoqueSaldoResponseDTO> saldos = consultaService.listarSaldos(
                tenantId, almoxarifadoId, grupoId, abaixoMinimo, pageable);

        return ResponseEntity.ok(saldos);
    }
}
