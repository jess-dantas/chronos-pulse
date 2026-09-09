package br.com.jess.chronos.pulse.modules.transparencia.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import br.com.jess.chronos.pulse.modules.transparencia.service.TransparenciaService;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.CriarPublicacaoDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.DespesasMensaisDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.PublicacaoResponseDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.TransparenciaResumoDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transparencia")
@RequiredArgsConstructor
@RequiresModulo(codigo = "TRANSPARENCIA")
public class TransparenciaController {

    private static final String ROLES_LEITURA =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ESTOQUE', 'COLABORADOR')";

    private static final String ROLES_GERENCIA =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')";

    private final TransparenciaService transparenciaService;
    private final AuditoriaService auditoriaService;

    @GetMapping("/resumo")
    @PreAuthorize(ROLES_LEITURA)
    public ResponseEntity<TransparenciaResumoDTO> obterResumo(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(transparenciaService.obterResumo(usuario.getTenantId()));
    }

    @GetMapping("/despesas-mensais")
    @PreAuthorize(ROLES_LEITURA)
    public ResponseEntity<DespesasMensaisDTO> obterDespesasMensais(
            @RequestParam(defaultValue = "2026") int ano,
            Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(transparenciaService.obterDespesasMensais(usuario.getTenantId(), ano));
    }

    @GetMapping("/publicacoes")
    @PreAuthorize(ROLES_LEITURA)
    public ResponseEntity<List<PublicacaoResponseDTO>> listarPublicacoes(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(transparenciaService.listarPublicacoes(usuario.getTenantId()));
    }

    @PostMapping("/publicacoes")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<PublicacaoResponseDTO> criarPublicacao(
            @Valid @RequestBody CriarPublicacaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PublicacaoResponseDTO resposta =
                transparenciaService.criarPublicacao(dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_PUBLICACAO_TRANSPARENCIA", "TRANSPARENCIA", resposta.id(),
                "Publicação " + resposta.competencia() + "/" + resposta.tipoPublicacao()
                        + " criada com valor " + resposta.valorTotal(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/publicacoes/{id}/publicar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<PublicacaoResponseDTO> publicar(
            @PathVariable UUID id,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PublicacaoResponseDTO resposta = transparenciaService.publicar(id, usuario.getTenantId());
        auditoriaService.registrar("DIVULGACAO_PUBLICACAO_TRANSPARENCIA", "TRANSPARENCIA", id,
                "Publicação " + resposta.competencia() + "/" + resposta.tipoPublicacao()
                        + " divulgada no portal",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/publicacoes/{id}")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<Void> remover(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        transparenciaService.remover(id, usuario.getTenantId());
        auditoriaService.registrar("REMOCAO_PUBLICACAO_TRANSPARENCIA", "TRANSPARENCIA", id,
                "Remoção de publicação em elaboração " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok().build();
    }
}