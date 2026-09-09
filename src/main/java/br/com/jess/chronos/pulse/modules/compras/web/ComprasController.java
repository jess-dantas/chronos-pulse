package br.com.jess.chronos.pulse.modules.compras.web;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.compras.service.ComprasService;
import br.com.jess.chronos.pulse.modules.compras.service.NfeImportacaoService;
import br.com.jess.chronos.pulse.modules.compras.service.NfeSefazConsultaService;
import br.com.jess.chronos.pulse.modules.compras.web.dto.AtualizarFornecedorDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarFornecedorDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarPedidoCompraDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.ChaveNfeDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.EntradaNfeResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.FornecedorResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PrecoConsultaResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.ReceberNfeDTO;
import br.com.jess.chronos.pulse.modules.modulo.infrastructure.security.RequiresModulo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compras")
@RequiredArgsConstructor
@RequiresModulo(codigo = "COMPRAS")
public class ComprasController {

    private static final String ROLES_COMPRAS =
            "hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH', 'ESTOQUE')";

    private final ComprasService comprasService;
    private final NfeImportacaoService nfeImportacaoService;
    private final NfeSefazConsultaService nfeSefazConsultaService;
    private final AuditoriaService auditoriaService;

    // ============================ FORNECEDORES ============================

    @GetMapping("/fornecedores")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<List<FornecedorResponseDTO>> listarFornecedores(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(comprasService.listarFornecedores(usuario.getTenantId()));
    }

    @GetMapping("/fornecedores/{id}")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<FornecedorResponseDTO> buscarFornecedor(
            @PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(comprasService.buscarFornecedor(id, usuario.getTenantId()));
    }

    @PostMapping("/fornecedores")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<FornecedorResponseDTO> cadastrarFornecedor(
            @Valid @RequestBody CadastrarFornecedorDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        FornecedorResponseDTO resposta = comprasService.criarFornecedor(dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_FORNECEDOR", "FORNECEDOR", resposta.id(),
                "Cadastro do fornecedor " + resposta.razaoSocial() + " (CNPJ " + resposta.cnpj() + ")",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/fornecedores/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<FornecedorResponseDTO> atualizarFornecedor(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarFornecedorDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        FornecedorResponseDTO resposta = comprasService.atualizarFornecedor(id, dto, usuario.getTenantId());
        auditoriaService.registrar("ATUALIZACAO_FORNECEDOR", "FORNECEDOR", id,
                "Atualização dos dados do fornecedor " + resposta.razaoSocial(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/fornecedores/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA')")
    public ResponseEntity<Void> inativarFornecedor(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        comprasService.inativarFornecedor(id, usuario.getTenantId());
        auditoriaService.registrar("INATIVACAO_FORNECEDOR", "FORNECEDOR", id,
                "Inativação do fornecedor " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok().build();
    }

    // ============================ PEDIDOS DE COMPRA ============================

    @GetMapping("/pedidos")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<List<PedidoCompraResponseDTO>> listarPedidos(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(comprasService.listarPedidos(usuario.getTenantId()));
    }

    @GetMapping("/pedidos/{id}")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<PedidoCompraResponseDTO> buscarPedido(
            @PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(comprasService.buscarPedido(id, usuario.getTenantId()));
    }

    @PostMapping("/pedidos")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<PedidoCompraResponseDTO> criarPedido(
            @Valid @RequestBody CadastrarPedidoCompraDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        PedidoCompraResponseDTO resposta = comprasService.criarPedido(dto, usuario.getTenantId());
        auditoriaService.registrar("CADASTRO_PEDIDO_COMPRA", "PEDIDO_COMPRA", resposta.id(),
                "Pedido " + resposta.numero() + " no valor de R$ " + resposta.valorTotal(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/pedidos/{id}/cancelar")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<Void> cancelarPedido(@PathVariable UUID id, Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        comprasService.cancelarPedido(id, usuario.getTenantId());
        auditoriaService.registrar("CANCELAMENTO_PEDIDO_COMPRA", "PEDIDO_COMPRA", id,
                "Cancelamento do pedido de compra " + id,
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok().build();
    }

    // ============================ RECEBIMENTO POR NFE ============================

    @PostMapping("/nfe/receber")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'ESTOQUE')")
    public ResponseEntity<EntradaNfeResponseDTO> receberNfe(
            @Valid @RequestBody ReceberNfeDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        EntradaNfeResponseDTO resposta = comprasService.receberNfe(dto, usuario.getTenantId(), usuario.getCpcId());
        auditoriaService.registrar("RECEBIMENTO_NFE", "ENTRADA_NFE", resposta.id(),
                "Recebimento da NFe " + resposta.numeroNfe() + " (chave " + resposta.chaveNfe() + ") vinculada ao pedido "
                        + resposta.pedidoNumero(),
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/nfe")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<List<EntradaNfeResponseDTO>> listarEntradasNfe(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(comprasService.listarEntradasNfe(usuario.getTenantId()));
    }

    @PostMapping("/nfe/importar-xml")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'ESTOQUE')")
    public ResponseEntity<NfeImportadoDTO> importarXmlNfe(
            @RequestParam("arquivo") MultipartFile arquivo,
            Authentication authentication) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Envie o arquivo XML da NFe");
        }
        if (arquivo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Arquivo XML da NFe maior que 5 MB");
        }

        String conteudo;
        try {
            conteudo = new String(arquivo.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo XML enviado");
        }

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        NfeImportadoDTO resposta = nfeImportacaoService.importarXml(conteudo);
        auditoriaService.registrar("IMPORTACAO_XML_NFE", "ENTRADA_NFE", null,
                "Importação do XML da NFe " + resposta.numero() + " (chave " + resposta.chaveNfe()
                        + ", emitente CNPJ " + resposta.cnpjEmitente() + ")",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/nfe/consultar-sefaz")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'ESTOQUE')")
    public ResponseEntity<NfeImportadoDTO> consultarNfeSefaz(
            @Valid @RequestBody ChaveNfeDTO dto,
            Authentication authentication) {

        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        NfeImportadoDTO resposta = nfeSefazConsultaService.consultarPorChave(dto.chaveNfe())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Consulta à SEFAZ não habilitada neste ambiente (app.compras.sefaz.consulta-enabled). "
                                + "Use a importação manual do XML da NFe."));
        auditoriaService.registrar("CONSULTA_SEFAZ_NFE", "ENTRADA_NFE", null,
                "Consulta SEFAZ da chave " + dto.chaveNfe() + " (NFe " + resposta.numero() + ")",
                usuario.getTenantId(), usuario.getCpcId(), usuario.getCpf(), usuario.getRole().name(),
                null, null, null);
        return ResponseEntity.ok(resposta);
    }

    // ============================ BANCO DE PREÇOS ============================

    @GetMapping("/precos")
    @PreAuthorize(ROLES_COMPRAS)
    public ResponseEntity<List<PrecoConsultaResponseDTO>> consultarPrecos(Authentication authentication) {
        CpcUsuario usuario = (CpcUsuario) authentication.getPrincipal();
        return ResponseEntity.ok(comprasService.consultarPrecos(usuario.getTenantId()));
    }
}