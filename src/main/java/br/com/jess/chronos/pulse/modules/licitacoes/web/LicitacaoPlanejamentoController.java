package br.com.jess.chronos.pulse.modules.licitacoes.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.licitacoes.service.PlanejamentoLicitacaoService;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.*;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/licitacoes/{licitacaoId}/planejamento")
@RequiredArgsConstructor
@RequiresModulo(codigo = "LICITACOES")
public class LicitacaoPlanejamentoController {

    private static final String ROLES_LICITACOES =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ESTOQUE')";

    private static final String ROLES_GERENCIA =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')";

    private final PlanejamentoLicitacaoService planejamentoService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize(ROLES_LICITACOES)
    public ResponseEntity<PlanejamentoLicitacaoResponseDTO> buscarPlanejamento(
            @PathVariable UUID licitacaoId, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(
                planejamentoService.buscarPlanejamento(licitacaoId, usuario.getTenantId()));
    }

    @PutMapping("/etp")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<PlanejamentoLicitacaoResponseDTO> salvarEtp(
            @PathVariable UUID licitacaoId,
            @Valid @RequestBody EtpDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PlanejamentoLicitacaoResponseDTO resposta =
                planejamentoService.salvarEtp(licitacaoId, dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_ETP", "LICITACAO", licitacaoId,
                "ETP " + (resposta.etp().status().equals("APROVADO") ? "atualizado" : "salvo")
                        + " na licitação " + resposta.numero(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/etp/aprovar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<PlanejamentoLicitacaoResponseDTO> aprovarEtp(
            @PathVariable UUID licitacaoId,
            @Valid @RequestBody AprovacaoDocumentoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PlanejamentoLicitacaoResponseDTO resposta =
                planejamentoService.aprovarEtp(licitacaoId, dto, usuario.getTenantId());
        auditoriaService.registrar("APROVACAO_ETP", "LICITACAO", licitacaoId,
                "ETP aprovado na licitação " + resposta.numero() + " por " + dto.responsavel(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/tr")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<PlanejamentoLicitacaoResponseDTO> salvarTr(
            @PathVariable UUID licitacaoId,
            @Valid @RequestBody TrDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PlanejamentoLicitacaoResponseDTO resposta =
                planejamentoService.salvarTr(licitacaoId, dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_TR", "LICITACAO", licitacaoId,
                "Termo de Referência salvo na licitação " + resposta.numero(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/tr/aprovar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<PlanejamentoLicitacaoResponseDTO> aprovarTr(
            @PathVariable UUID licitacaoId,
            @Valid @RequestBody AprovacaoDocumentoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PlanejamentoLicitacaoResponseDTO resposta =
                planejamentoService.aprovarTr(licitacaoId, dto, usuario.getTenantId());
        auditoriaService.registrar("APROVACAO_TR", "LICITACAO", licitacaoId,
                "Termo de Referência aprovado na licitação " + resposta.numero()
                        + " por " + dto.responsavel(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/edital")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<PlanejamentoLicitacaoResponseDTO> salvarEdital(
            @PathVariable UUID licitacaoId,
            @Valid @RequestBody EditalDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PlanejamentoLicitacaoResponseDTO resposta =
                planejamentoService.salvarEdital(licitacaoId, dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_EDITAL", "LICITACAO", licitacaoId,
                "Edital salvo na licitação " + resposta.numero(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/edital/publicar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<PlanejamentoLicitacaoResponseDTO> publicarEdital(
            @PathVariable UUID licitacaoId, Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PlanejamentoLicitacaoResponseDTO resposta =
                planejamentoService.publicarEdital(licitacaoId, usuario.getTenantId());
        auditoriaService.registrar("PUBLICACAO_EDITAL", "LICITACAO", licitacaoId,
                "Edital publicado na licitação " + resposta.numero(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }
}