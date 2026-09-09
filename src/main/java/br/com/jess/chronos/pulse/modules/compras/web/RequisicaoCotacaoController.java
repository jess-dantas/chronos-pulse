package br.com.jess.chronos.pulse.modules.compras.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.compras.service.ComprasService;
import br.com.jess.chronos.pulse.modules.compras.service.RequisicaoCotacaoService;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarCotacaoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarRequisicaoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CotacaoResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RegistrarPropostasDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RequisicaoResponseDTO;
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
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
@RequiresModulo(codigo = "COMPRAS")
public class RequisicaoCotacaoController {

    private static final String ROLES_COMPRAS =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ESTOQUE')";

    private static final String ROLES_GERENCIA =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')";

    private final RequisicaoCotacaoService requisicaoCotacaoService;
    private final ComprasService comprasService;
    private final AuditoriaService auditoriaService;

    // ============================ REQUISIÇÕES ============================

    @GetMapping("/requisicoes")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<List<RequisicaoResponseDTO>> listarRequisicoes(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(requisicaoCotacaoService.listarRequisicoes(usuario.getTenantId()));
    }

    @GetMapping("/requisicoes/{id}")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<RequisicaoResponseDTO> buscarRequisicao(
            @PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(requisicaoCotacaoService.buscarRequisicao(id, usuario.getTenantId()));
    }

    @PostMapping("/requisicoes")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<RequisicaoResponseDTO> criarRequisicao(
            @Valid @RequestBody CadastrarRequisicaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        RequisicaoResponseDTO resposta = requisicaoCotacaoService.criarRequisicao(dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_REQUISICAO", "REQUISICAO_COMPRA", resposta.id(),
                "Requisição " + resposta.numero() + " com " + resposta.itens().size() + " item(ns) — " + resposta.justificativa(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/requisicoes/{id}/cancelar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<Void> cancelarRequisicao(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        requisicaoCotacaoService.cancelarRequisicao(id, usuario.getTenantId());
        auditoriaService.registrar("CANCELAMENTO_REQUISICAO", "REQUISICAO_COMPRA", id,
                "Cancelamento da requisição de compra " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok().build();
    }

    // ============================ COTAÇÕES ============================

    @GetMapping("/cotacoes")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<List<CotacaoResponseDTO>> listarCotacoes(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(requisicaoCotacaoService.listarCotacoes(usuario.getTenantId()));
    }

    @GetMapping("/cotacoes/{id}")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<CotacaoResponseDTO> buscarCotacao(
            @PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(requisicaoCotacaoService.buscarCotacao(id, usuario.getTenantId()));
    }

    @PostMapping("/cotacoes")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<CotacaoResponseDTO> criarCotacao(
            @Valid @RequestBody CadastrarCotacaoDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        CotacaoResponseDTO resposta = requisicaoCotacaoService.criarCotacao(dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_COTACAO", "COTACAO_COMPRA", resposta.id(),
                "Cotação " + resposta.numero() + " para a requisição " + resposta.requisicaoNumero()
                        + " com " + resposta.fornecedores().size() + " fornecedor(es)",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/cotacoes/{id}/propostas")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<CotacaoResponseDTO> registrarPropostas(
            @PathVariable UUID id,
            @Valid @RequestBody RegistrarPropostasDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        CotacaoResponseDTO resposta = requisicaoCotacaoService.registrarPropostas(id, dto, usuario.getTenantId());
        auditoriaService.registrar("REGISTRO_PROPOSTA", "COTACAO_COMPRA", id,
                "Propostas do fornecedor " + dto.fornecedorId() + " na cotação " + resposta.numero(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/cotacoes/{id}/concluir")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<CotacaoResponseDTO> concluirCotacao(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        CotacaoResponseDTO resposta = requisicaoCotacaoService.concluirCotacao(id, usuario.getTenantId());
        auditoriaService.registrar("CONCLUSAO_COTACAO", "COTACAO_COMPRA", id,
                "Cotação " + resposta.numero() + " concluída — vencedores definidos por item",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/cotacoes/{id}/cancelar")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<Void> cancelarCotacao(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        requisicaoCotacaoService.cancelarCotacao(id, usuario.getTenantId());
        auditoriaService.registrar("CANCELAMENTO_COTACAO", "COTACAO_COMPRA", id,
                "Cancelamento da cotação " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cotacoes/{id}/gerar-pedidos")
    @PreAuthorize(ROLES_GERENCIA)
    public ResponseEntity<List<PedidoCompraResponseDTO>> gerarPedidos(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        List<PedidoCompraResponseDTO> resposta = requisicaoCotacaoService.gerarPedidos(id, usuario.getTenantId());
        String descricao = resposta.stream().map(PedidoCompraResponseDTO::numero).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
        auditoriaService.registrar("GERACAO_PEDIDO_COTACAO", "COTACAO_COMPRA", id,
                "Pedido(s) " + descricao + " gerado(s) a partir da cotação " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }
}