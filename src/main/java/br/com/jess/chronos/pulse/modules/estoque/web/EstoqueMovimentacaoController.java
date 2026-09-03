package br.com.jess.chronos.pulse.modules.estoque.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.estoque.service.EstoqueMovimentacaoService;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.EntradaMaterialDTO;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.SaidaMaterialDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/estoque/movimentacoes")
@RequiredArgsConstructor
public class EstoqueMovimentacaoController {

    private final EstoqueMovimentacaoService movimentacaoService;

    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<Void> registrarEntrada(
            @Valid @RequestBody EntradaMaterialDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        UUID tenantId = usuario.getTenantId();
        UUID usuarioCpcId = usuario.getCpcId();

        movimentacaoService.registrarEntrada(dto, tenantId, usuarioCpcId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/saida")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<Void> registrarSaida(
            @Valid @RequestBody SaidaMaterialDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        UUID tenantId = usuario.getTenantId();
        UUID usuarioCpcId = usuario.getCpcId();

        movimentacaoService.registrarSaida(dto, tenantId, usuarioCpcId);

        return ResponseEntity.ok().build();
    }
}
