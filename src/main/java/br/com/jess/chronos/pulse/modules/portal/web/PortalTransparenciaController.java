package br.com.jess.chronos.pulse.modules.portal.web;

import br.com.jess.chronos.pulse.modules.portal.service.PortalTransparenciaService;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalContratoDetalheDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalContratoListaDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalLicitacaoDetalheDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalLicitacaoListaDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalPublicacaoDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalResumoDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.DespesasMensaisDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/publico/transparencia")
public class PortalTransparenciaController {

    private final PortalTransparenciaService portalService;

    public PortalTransparenciaController(PortalTransparenciaService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PortalResumoDTO> obterResumo(@PathVariable String slug) {
        return ResponseEntity.ok(portalService.obterResumo(slug));
    }

    @GetMapping("/{slug}/licitacoes")
    public ResponseEntity<List<PortalLicitacaoListaDTO>> listarLicitacoes(@PathVariable String slug) {
        return ResponseEntity.ok(portalService.listarLicitacoes(slug));
    }

    @GetMapping("/{slug}/licitacoes/{id}")
    public ResponseEntity<PortalLicitacaoDetalheDTO> obterLicitacao(
            @PathVariable String slug, @PathVariable UUID id) {
        return ResponseEntity.ok(portalService.obterLicitacao(slug, id));
    }

    @GetMapping("/{slug}/contratos")
    public ResponseEntity<List<PortalContratoListaDTO>> listarContratos(@PathVariable String slug) {
        return ResponseEntity.ok(portalService.listarContratos(slug));
    }

    @GetMapping("/{slug}/contratos/{id}")
    public ResponseEntity<PortalContratoDetalheDTO> obterContrato(
            @PathVariable String slug, @PathVariable UUID id) {
        return ResponseEntity.ok(portalService.obterContrato(slug, id));
    }

    @GetMapping("/{slug}/despesas-mensais")
    public ResponseEntity<DespesasMensaisDTO> obterDespesasMensais(
            @PathVariable String slug,
            @RequestParam(defaultValue = "2026") int ano) {
        return ResponseEntity.ok(portalService.obterDespesasMensais(slug, ano));
    }

    @GetMapping("/{slug}/publicacoes")
    public ResponseEntity<List<PortalPublicacaoDTO>> listarPublicacoes(@PathVariable String slug) {
        return ResponseEntity.ok(portalService.listarPublicacoes(slug));
    }
}