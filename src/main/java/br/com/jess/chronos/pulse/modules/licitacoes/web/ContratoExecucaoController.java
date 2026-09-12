package br.com.jess.chronos.pulse.modules.licitacoes.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.licitacoes.service.ContratoExecucaoService;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.*;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contratos")
@RequiredArgsConstructor
@RequiresModulo(codigo = "LICITACOES")
public class ContratoExecucaoController {

    private static final String ROLES_LICITACOES =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ESTOQUE')";

    private static final String ROLES_GERENCIA =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')";

    private final ContratoExecucaoService execucaoService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize(ROLES_LICITACOES)
    public ResponseEntity<List<ContratoExecucaoResponseDTO>> listarExecucoes(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(execucaoService.listarExecucoes(usuario.getTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize(ROLES_LICITACOES)
    public ResponseEntity<ContratoExecucaoResponseDTO> buscarExecucao(
            @PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(execucaoService.buscarExecucao(id, usuario.getTenantId()));
    }

    @PostMapping("/{id}/aditivos")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<ContratoAditivoResponseDTO> registrarAditivo(
            @PathVariable UUID id,
            @Valid @RequestBody AdicionarAditivoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        ContratoAditivoResponseDTO resposta =
                execucaoService.registrarAditivo(id, dto, usuario.getTenantId(), usuario.getCpcId());
        auditoriaService.registrar("REGISTRO_ADITIVO_CONTRATO", "CONTRATO", id,
                "Aditivo de " + dto.tipo() + " no contrato " + id + " — " + dto.descricao(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/apontamentos")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<ContratoApontamentoResponseDTO> registrarApontamento(
            @PathVariable UUID id,
            @Valid @RequestBody AdicionarApontamentoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        ContratoApontamentoResponseDTO resposta =
                execucaoService.registrarApontamento(id, dto, usuario.getTenantId(), usuario.getCpcId());
        auditoriaService.registrar("REGISTRO_APONTAMENTO_CONTRATO", "CONTRATO", id,
                "Apontamento (" + dto.gravidade() + ") de " + dto.fiscal() + " no contrato " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/apontamentos/{apontamentoId}/resolver")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<ContratoApontamentoResponseDTO> resolverApontamento(
            @PathVariable UUID id,
            @PathVariable UUID apontamentoId,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        ContratoApontamentoResponseDTO resposta =
                execucaoService.resolverApontamento(id, apontamentoId, usuario.getTenantId());
        auditoriaService.registrar("RESOLUCAO_APONTAMENTO_CONTRATO", "CONTRATO", id,
                "Apontamento " + apontamentoId + " resolvido no contrato " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/medicoes")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<ContratoMedicaoResponseDTO> registrarMedicao(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarMedicaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        ContratoMedicaoResponseDTO resposta =
                execucaoService.registrarMedicao(id, dto, usuario.getTenantId(), usuario.getCpcId());
        auditoriaService.registrar("REGISTRO_MEDICAO_CONTRATO", "CONTRATO", id,
                "Medição " + dto.periodo() + " de R$ " + dto.valorMedido() + " (pago: R$ "
                        + dto.valorPago() + ") no contrato " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/sancoes")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<ContratoSancaoResponseDTO> registrarSancao(
            @PathVariable UUID id,
            @Valid @RequestBody AdicionarSancaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        ContratoSancaoResponseDTO resposta =
                execucaoService.registrarSancao(id, dto, usuario.getTenantId(), usuario.getCpcId());
        auditoriaService.registrar("REGISTRO_SANCAO_CONTRATO", "CONTRATO", id,
                "Sanção " + dto.tipo() + " aplicada em " + dto.aplicadaEm() + " no contrato " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/rescindir")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<ContratoRescisaoResponseDTO> rescindirContrato(
            @PathVariable UUID id,
            @Valid @RequestBody RescindirContratoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        ContratoRescisaoResponseDTO resposta =
                execucaoService.rescindirContrato(id, dto, usuario.getTenantId(), usuario.getCpcId());
        auditoriaService.registrar("RESCISAO_CONTRATO", "CONTRATO", id,
                "Rescisão " + dto.tipo() + " em " + dto.dataRescisao() + " — " + dto.motivo(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }
}