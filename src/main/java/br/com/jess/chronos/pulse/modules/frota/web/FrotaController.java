package br.com.jess.chronos.pulse.modules.frota.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.frota.service.FrotaService;
import br.com.jess.chronos.pulse.modules.frota.web.dto.AbastecimentoResponseDTO;
import br.com.jess.chronos.pulse.modules.frota.web.dto.CadastrarAbastecimentoDTO;
import br.com.jess.chronos.pulse.modules.frota.web.dto.CadastrarFrotaVeiculoDTO;
import br.com.jess.chronos.pulse.modules.frota.web.dto.FrotaVeiculoResponseDTO;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
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
@RequestMapping("/api/v1/frota")
@RequiredArgsConstructor
@RequiresModulo(codigo = "FROTA")
public class FrotaController {

    private final FrotaService frotaService;

    @PostMapping("/veiculos")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<FrotaVeiculoResponseDTO> cadastrarVeiculo(
            @Valid @RequestBody CadastrarFrotaVeiculoDTO dto,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        FrotaVeiculoResponseDTO response = frotaService.cadastrarVeiculo(dto, usuario.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/veiculos")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<FrotaVeiculoResponseDTO>> listarVeiculos(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(frotaService.listarVeiculos(usuario.getTenantId(), pageable));
    }

    @GetMapping("/veiculos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<FrotaVeiculoResponseDTO> buscarVeiculo(
            @PathVariable UUID id,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(frotaService.buscarVeiculoPorId(id, usuario.getTenantId()));
    }

    @PostMapping("/abastecimentos")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<AbastecimentoResponseDTO> registrarAbastecimento(
            @Valid @RequestBody CadastrarAbastecimentoDTO dto,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        AbastecimentoResponseDTO response = frotaService.registrarAbastecimento(dto, usuario.getTenantId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/abastecimentos")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'COLABORADOR')")
    public ResponseEntity<Page<AbastecimentoResponseDTO>> listarAbastecimentos(
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(frotaService.listarAbastecimentos(usuario.getTenantId(), pageable));
    }
}
