package br.com.jess.chronos.pulse.modules.modulo.web;

import br.com.jess.chronos.pulse.modules.modulo.service.ModuloService;
import br.com.jess.chronos.pulse.modules.modulo.web.dto.AtualizarModulosEmpresaRequestDTO;
import br.com.jess.chronos.pulse.modules.modulo.web.dto.ModuloResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'SUPORTE_N1', 'SUPORTE_N2')")
@RequiredArgsConstructor
public class ModuloAdminController {

    private final ModuloService moduloService;

    @GetMapping("/modulos")
    public List<ModuloResponseDTO> listarModulos() {
        return moduloService.listarCatalogo();
    }

    @GetMapping("/empresas/{tenantId}/modulos")
    public List<String> listarModulosEmpresa(@PathVariable UUID tenantId) {
        return moduloService.listarCodigosAtivos(tenantId);
    }

    @PutMapping("/empresas/{tenantId}/modulos")
    public List<String> atualizarModulosEmpresa(
            @PathVariable UUID tenantId,
            @Valid @RequestBody AtualizarModulosEmpresaRequestDTO request) {
        return moduloService.atualizarModulos(tenantId, request.modulos());
    }
}