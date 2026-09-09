package br.com.jess.chronos.pulse.modules.licitacoes.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.licitacoes.service.LicitacaoService;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.CadastrarLicitacaoDTO;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.LicitacaoResponseDTO;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.PublicarLicitacaoDTO;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.RegistrarPropostasLicitacaoDTO;
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
@RequestMapping("/api/v1/licitacoes")
@RequiredArgsConstructor
@RequiresModulo(codigo = "LICITACOES")
public class LicitacaoController {

    private static final String ROLES_LICITACOES =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ESTOQUE')";

    private static final String ROLES_GERENCIA =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')";

    private final LicitacaoService licitacaoService;
    private final AuditoriaService auditoriaService;

    @GetMapping
    @PreAuthorize(ROLES_LICITACOES)
    public ResponseEntity<List<LicitacaoResponseDTO>> listarLicitacoes(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(licitacaoService.listarLicitacoes(usuario.getTenantId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize(ROLES_LICITACOES)
    public ResponseEntity<LicitacaoResponseDTO> buscarLicitacao(
            @PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(licitacaoService.buscarLicitacao(id, usuario.getTenantId()));
    }

    @PostMapping
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<LicitacaoResponseDTO> criarLicitacao(
            @Valid @RequestBody CadastrarLicitacaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        LicitacaoResponseDTO resposta = licitacaoService.criarLicitacao(dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_LICITACAO", "LICITACAO", resposta.id(),
                "Licitação " + resposta.numero() + " (" + resposta.modalidade() + "/"
                        + resposta.tipoJulgamento() + ") com " + resposta.itens().size() + " item(ns)",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/publicar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<LicitacaoResponseDTO> publicarLicitacao(
            @PathVariable UUID id,
            @Valid @RequestBody PublicarLicitacaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        LicitacaoResponseDTO resposta = licitacaoService.publicarLicitacao(id, dto, usuario.getTenantId());
        auditoriaService.registrar("PUBLICACAO_LICITACAO", "LICITACAO", id,
                "Licitação " + resposta.numero() + " publicada com "
                        + resposta.participantes().size() + " fornecedor(es) habilitado(s)",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/{id}/propostas")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<LicitacaoResponseDTO> registrarPropostas(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarPropostasLicitacaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        LicitacaoResponseDTO resposta = licitacaoService.registrarPropostas(id, dto, usuario.getTenantId());
        auditoriaService.registrar("REGISTRO_PROPOSTA_LICITACAO", "LICITACAO", id,
                "Propostas do fornecedor " + dto.fornecedorId() + " na licitação " + resposta.numero()
                        + " (" + dto.itens().size() + " item(ns))",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/adjudicar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<LicitacaoResponseDTO> adjudicarLicitacao(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        LicitacaoResponseDTO resposta = licitacaoService.adjudicarLicitacao(id, usuario.getTenantId());
        auditoriaService.registrar("ADJUDICACAO_LICITACAO", "LICITACAO", id,
                "Licitação " + resposta.numero() + " adjudicada — vencedor por menor preço em cada item",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/homologar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<LicitacaoResponseDTO> homologarLicitacao(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        LicitacaoResponseDTO resposta = licitacaoService.homologarLicitacao(id, usuario.getTenantId());
        auditoriaService.registrar("HOMOLOGACAO_LICITACAO", "LICITACAO", id,
                "Licitação " + resposta.numero() + " homologada",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<Void> cancelarLicitacao(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        licitacaoService.cancelarLicitacao(id, usuario.getTenantId());
        auditoriaService.registrar("CANCELAMENTO_LICITACAO", "LICITACAO", id,
                "Cancelamento da licitação " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/gerar-pedidos")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<List<PedidoCompraResponseDTO>> gerarPedidos(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        List<PedidoCompraResponseDTO> resposta = licitacaoService.gerarPedidos(id, usuario.getTenantId());
        String descricao = resposta.stream().map(PedidoCompraResponseDTO::numero)
                .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
        auditoriaService.registrar("GERACAO_PEDIDO_LICITACAO", "LICITACAO", id,
                "Pedido(s) " + descricao + " gerado(s) a partir da licitação " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }
}