package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.EntradaNfe;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraItem;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraStatus;
import br.com.jess.chronos.pulse.modules.compras.repository.EntradaNfeRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.FornecedorRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.PedidoCompraItemRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.PedidoCompraRepository;
import br.com.jess.chronos.pulse.modules.compras.web.dto.AtualizarFornecedorDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarFornecedorDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarPedidoCompraDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.EntradaNfeResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.FornecedorResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.ItemNfeDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraItemResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PrecoConsultaResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PrecoMaterialProjecao;
import br.com.jess.chronos.pulse.modules.compras.web.dto.ReceberNfeDTO;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.estoque.service.EstoqueMovimentacaoService;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.EntradaMaterialDTO;
import br.com.jess.chronos.pulse.shared.util.CnpjValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComprasService {

    private static final String NFE_SEM_WEBSERVICE =
            "Validação estrutural da chave realizada localmente (consulta SEFAZ será habilitada em fase futura)";

    private final FornecedorRepository fornecedorRepository;
    private final PedidoCompraRepository pedidoRepository;
    private final PedidoCompraItemRepository pedidoItemRepository;
    private final EntradaNfeRepository entradaNfeRepository;
    private final MaterialRepository materialRepository;
    private final EstoqueMovimentacaoService estoqueMovimentacaoService;
    private final ContratoRepositoryPort contratoRepositoryPort;

    // ============================ FORNECEDORES ============================

    @Transactional(readOnly = true)
    public List<FornecedorResponseDTO> listarFornecedores(UUID tenantId) {
        return fornecedorRepository.findAllByTenantIdOrderByRazaoSocial(tenantId).stream()
                .map(FornecedorResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FornecedorResponseDTO buscarFornecedor(UUID id, UUID tenantId) {
        return FornecedorResponseDTO.from(buscarFornecedorDoTenant(id, tenantId));
    }

    @Transactional
    public FornecedorResponseDTO criarFornecedor(CadastrarFornecedorDTO dto, UUID tenantId) {
        String cnpj = CnpjValidator.normalizar(dto.cnpj());
        if (!CnpjValidator.validar(cnpj)) {
            throw new IllegalArgumentException("CNPJ inválido");
        }
        if (fornecedorRepository.findByTenantIdAndCnpj(tenantId, cnpj).isPresent()) {
            throw new IllegalArgumentException("Já existe fornecedor cadastrado com este CNPJ");
        }

        Fornecedor fornecedor = Fornecedor.builder()
                .tenantId(tenantId)
                .cnpj(cnpj)
                .razaoSocial(dto.razaoSocial().trim())
                .nomeFantasia(dto.nomeFantasia())
                .inscricaoEstadual(dto.inscricaoEstadual())
                .email(dto.email())
                .telefone(dto.telefone())
                .enderecoLogradouro(dto.enderecoLogradouro())
                .enderecoNumero(dto.enderecoNumero())
                .enderecoBairro(dto.enderecoBairro())
                .enderecoCidade(dto.enderecoCidade())
                .enderecoUf(dto.enderecoUf())
                .enderecoCep(dto.enderecoCep())
                .observacoes(dto.observacoes())
                .ativo(true)
                .build();

        return FornecedorResponseDTO.from(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponseDTO atualizarFornecedor(UUID id, AtualizarFornecedorDTO dto, UUID tenantId) {
        Fornecedor fornecedor = buscarFornecedorDoTenant(id, tenantId);

        if (dto.cnpj() != null && !dto.cnpj().isBlank()) {
            String cnpj = CnpjValidator.normalizar(dto.cnpj());
            if (!CnpjValidator.validar(cnpj)) {
                throw new IllegalArgumentException("CNPJ inválido");
            }
            fornecedorRepository.findByTenantIdAndCnpj(tenantId, cnpj).ifPresent(existente -> {
                if (!existente.getId().equals(id)) {
                    throw new IllegalArgumentException("Já existe fornecedor cadastrado com este CNPJ");
                }
            });
            fornecedor.setCnpj(cnpj);
        }
        if (dto.razaoSocial() != null && !dto.razaoSocial().isBlank()) {
            fornecedor.setRazaoSocial(dto.razaoSocial().trim());
        }
        if (dto.nomeFantasia() != null) fornecedor.setNomeFantasia(dto.nomeFantasia());
        if (dto.inscricaoEstadual() != null) fornecedor.setInscricaoEstadual(dto.inscricaoEstadual());
        if (dto.email() != null) fornecedor.setEmail(dto.email());
        if (dto.telefone() != null) fornecedor.setTelefone(dto.telefone());
        if (dto.enderecoLogradouro() != null) fornecedor.setEnderecoLogradouro(dto.enderecoLogradouro());
        if (dto.enderecoNumero() != null) fornecedor.setEnderecoNumero(dto.enderecoNumero());
        if (dto.enderecoBairro() != null) fornecedor.setEnderecoBairro(dto.enderecoBairro());
        if (dto.enderecoCidade() != null) fornecedor.setEnderecoCidade(dto.enderecoCidade());
        if (dto.enderecoUf() != null) fornecedor.setEnderecoUf(dto.enderecoUf());
        if (dto.enderecoCep() != null) fornecedor.setEnderecoCep(dto.enderecoCep());
        if (dto.observacoes() != null) fornecedor.setObservacoes(dto.observacoes());
        if (dto.ativo() != null) fornecedor.setAtivo(dto.ativo());

        return FornecedorResponseDTO.from(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public void inativarFornecedor(UUID id, UUID tenantId) {
        Fornecedor fornecedor = buscarFornecedorDoTenant(id, tenantId);
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    // ============================ PEDIDOS DE COMPRA ============================

    @Transactional(readOnly = true)
    public List<PedidoCompraResponseDTO> listarPedidos(UUID tenantId) {
        Map<UUID, String> fornecedores = mapaNomesFornecedores(tenantId);
        Map<UUID, Material> materiais = mapaMateriais(tenantId);

        return pedidoRepository.findAllByTenantIdOrderByDataEmissaoDesc(tenantId).stream()
                .map(pedido -> PedidoCompraResponseDTO.from(
                        pedido,
                        fornecedores.getOrDefault(pedido.getFornecedor().getId(), pedido.getFornecedor().getRazaoSocial()),
                        mapearItens(pedido, materiais)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoCompraResponseDTO buscarPedido(UUID id, UUID tenantId) {
        PedidoCompra pedido = buscarPedidoDoTenant(id, tenantId);
        Map<UUID, Material> materiais = mapaMateriais(tenantId);
        return PedidoCompraResponseDTO.from(
                pedido,
                pedido.getFornecedor().getRazaoSocial(),
                mapearItens(pedido, materiais));
    }

    @Transactional
    public PedidoCompraResponseDTO criarPedido(CadastrarPedidoCompraDTO dto, UUID tenantId) {
        return criarPedidoPorItens(
                dto.fornecedorId(),
                dto.objeto(),
                dto.prazoEntrega(),
                dto.contratoId(),
                dto.empenhoNumero(),
                dto.observacoes(),
                dto.itens(),
                tenantId);
    }

    /**
     * Cria pedido de compra a partir de itens já definidos (usado pelo fluxo de
     * requisição & cotação, em que os itens vencedores geram o pedido/AF).
     */
    @Transactional
    public PedidoCompraResponseDTO criarPedidoPorItens(
            UUID fornecedorId,
            String objeto,
            LocalDate prazoEntrega,
            UUID contratoId,
            String empenhoNumero,
            String observacoes,
            List<PedidoCompraItemDTO> itensDTO,
            UUID tenantId) {

        Fornecedor fornecedor = buscarFornecedorDoTenant(fornecedorId, tenantId);
        if (!Boolean.TRUE.equals(fornecedor.getAtivo())) {
            throw new IllegalArgumentException("Fornecedor inativo não pode receber novo pedido");
        }

        Map<UUID, Material> materiais = mapaMateriais(tenantId);

        List<PedidoCompraItem> itens = new ArrayList<>();
        Map<String, BigDecimal> quantidadePorMaterial = new HashMap<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (PedidoCompraItemDTO itemDTO : itensDTO) {
            Material material = materiais.get(itemDTO.materialId());
            if (material == null) {
                throw new IllegalArgumentException("Material não encontrado para um dos itens");
            }
            String chave = itemDTO.materialId().toString();
            if (quantidadePorMaterial.containsKey(chave)) {
                throw new IllegalArgumentException("Material duplicado no pedido: " + material.getDescricao());
            }
            quantidadePorMaterial.put(chave, itemDTO.quantidade());

            PedidoCompraItem item = PedidoCompraItem.builder()
                    .tenantId(tenantId)
                    .materialId(itemDTO.materialId())
                    .quantidade(itemDTO.quantidade())
                    .valorUnitario(itemDTO.valorUnitario())
                    .quantidadeRecebida(BigDecimal.ZERO)
                    .build();
            itens.add(item);
            valorTotal = valorTotal.add(item.getValorTotalItem());
        }

        if (contratoId != null) {
            validarContratoDoTenant(contratoId, tenantId);
        }

        PedidoCompra pedido = PedidoCompra.builder()
                .tenantId(tenantId)
                .numero(proximoNumeroPedido(tenantId))
                .fornecedor(fornecedor)
                .objeto(objeto)
                .dataEmissao(LocalDate.now())
                .prazoEntrega(prazoEntrega)
                .contratoId(contratoId)
                .empenhoNumero(empenhoNumero)
                .valorTotal(valorTotal.setScale(2, RoundingMode.HALF_UP))
                .status(PedidoCompraStatus.EMITIDO)
                .observacoes(observacoes)
                .build();

        itens.forEach(pedido::adicionarItem);
        pedido = pedidoRepository.save(pedido);

        return PedidoCompraResponseDTO.from(
                pedido, fornecedor.getRazaoSocial(), mapearItens(pedido, materiais));
    }

    @Transactional
    public void cancelarPedido(UUID id, UUID tenantId) {
        PedidoCompra pedido = buscarPedidoDoTenant(id, tenantId);
        if (pedido.getStatus() == PedidoCompraStatus.RECEBIDO) {
            throw new IllegalArgumentException("Pedido já recebido não pode ser cancelado");
        }
        if (pedido.getStatus() == PedidoCompraStatus.CANCELADO) {
            throw new IllegalArgumentException("Pedido já está cancelado");
        }
        pedido.setStatus(PedidoCompraStatus.CANCELADO);
        pedidoRepository.save(pedido);
    }

    // ============================ RECEBIMENTO POR NFE ============================

    @Transactional
    public EntradaNfeResponseDTO receberNfe(ReceberNfeDTO dto, UUID tenantId, UUID usuarioCpcId) {
        String chave = dto.chaveNfe().trim();
        if (!validarChaveNfe(chave)) {
            throw new IllegalArgumentException("Chave NFe inválida (44 dígitos ou dígito verificador incorreto)");
        }
        if (entradaNfeRepository.findByTenantIdAndChaveNfe(tenantId, chave).isPresent()) {
            throw new IllegalArgumentException("Chave NFe já registrada para este CNPJ/empresa");
        }

        String conteudoXml = dto.xmlNfe() != null && !dto.xmlNfe().isBlank() ? dto.xmlNfe().trim() : null;
        NfeImportadoDTO importada = null;
        if (conteudoXml != null) {
            importada = NfeXmlParser.parsear(conteudoXml);
            if (!importada.chaveNfe().equals(chave)) {
                throw new IllegalArgumentException("Chave NFe do XML não confere com a chave informada");
            }
        }

        PedidoCompra pedido = buscarPedidoDoTenant(dto.pedidoId(), tenantId);
        if (pedido.getStatus() == PedidoCompraStatus.CANCELADO) {
            throw new IllegalArgumentException("Não é possível receber pedido cancelado");
        }

        Map<UUID, PedidoCompraItem> itensPorMaterial = new HashMap<>();
        pedido.getItens().forEach(item -> itensPorMaterial.put(item.getMaterialId(), item));

        String tipoTermo = dto.tipoTermo() != null && !dto.tipoTermo().isBlank()
                ? dto.tipoTermo().trim().toUpperCase() : "DEFINITIVO";
        if ("PROVISORIO".equals(tipoTermo)
                && (dto.numeroTermo() == null || dto.numeroTermo().isBlank())) {
            throw new IllegalArgumentException("Número do termo é obrigatório para recebimento provisório");
        }

        BigDecimal valorEntrada = BigDecimal.ZERO;

        for (ItemNfeDTO item : dto.itens()) {
            PedidoCompraItem pedidoItem = itensPorMaterial.get(item.materialId());
            if (pedidoItem == null) {
                throw new IllegalArgumentException("Material " + item.materialId() + " não pertence ao pedido");
            }
            BigDecimal disponivel = pedidoItem.getQuantidade()
                    .subtract(pedidoItem.getQuantidadeRecebida());
            if (item.quantidade().compareTo(disponivel) > 0) {
                throw new IllegalArgumentException(
                        "Quantidade recebida excede o saldo do item do pedido (disponível: " + disponivel + ")");
            }

            pedidoItem.setQuantidadeRecebida(pedidoItem.getQuantidadeRecebida().add(item.quantidade()));

            estoqueMovimentacaoService.registrarEntrada(
                    new EntradaMaterialDTO(
                            dto.almoxarifadoId(),
                            item.materialId(),
                            item.quantidade(),
                            pedidoItem.getValorUnitario(),
                            null,
                            null,
                            chave,
                            tipoTermo,
                            dto.numeroTermo()),
                    tenantId,
                    usuarioCpcId);

            valorEntrada = valorEntrada.add(item.quantidade().multiply(pedidoItem.getValorUnitario()));
        }

        valorEntrada = valorEntrada.setScale(2, RoundingMode.HALF_UP);

        if (pedido.todasQuantidadesRecebidas()) {
            pedido.setStatus(PedidoCompraStatus.RECEBIDO);
        } else {
            pedido.setStatus(PedidoCompraStatus.RECEBIDO_PARCIAL);
        }
        pedidoRepository.save(pedido);

        if (pedido.getContratoId() != null) {
            liquidarContrato(pedido, valorEntrada, tenantId);
        }

        String notaOrigem = importada != null
                ? " | Documento validado a partir do XML da NFe"
                : " | " + NFE_SEM_WEBSERVICE;

        EntradaNfe entrada = EntradaNfe.builder()
                .tenantId(tenantId)
                .chaveNfe(chave)
                .numeroNfe(preferir(dto.numeroNfe(), importada != null ? importada.numero() : null))
                .serie(preferir(dto.serie(), importada != null ? importada.serie() : null))
                .dataEmissao(dto.dataEmissao() != null
                        ? dto.dataEmissao()
                        : (importada != null ? importada.dataEmissao() : null))
                .valorNota(dto.valorNota() != null
                        ? dto.valorNota()
                        : (importada != null ? importada.valorNota() : null))
                .fornecedorId(pedido.getFornecedor().getId())
                .pedidoId(pedido.getId())
                .contratoId(pedido.getContratoId())
                .empenhoNumero(pedido.getEmpenhoNumero())
                .almoxarifadoId(dto.almoxarifadoId())
                .tipoTermo(tipoTermo)
                .numeroTermo(dto.numeroTermo())
                .cnpjEmitente(preferir(dto.cnpjEmitente(), importada != null ? importada.cnpjEmitente() : null))
                .razaoEmitente(preferir(dto.razaoEmitente(), importada != null ? importada.razaoEmitente() : null))
                .xmlNfe(conteudoXml)
                .observacoes(dto.observacoes() != null
                        ? dto.observacoes() + notaOrigem
                        : notaOrigem)
                .build();

        return EntradaNfeResponseDTO.from(entradaNfeRepository.save(entrada), pedido.getNumero());
    }

    @Transactional(readOnly = true)
    public List<EntradaNfeResponseDTO> listarEntradasNfe(UUID tenantId) {
        return entradaNfeRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId).stream()
                .map(entrada -> {
                    String pedidoNumero = pedidoRepository.findById(entrada.getPedidoId())
                            .map(PedidoCompra::getNumero)
                            .orElse("");
                    return EntradaNfeResponseDTO.from(entrada, pedidoNumero);
                })
                .toList();
    }

    // ============================ BANCO DE PREÇOS ============================

    @Transactional(readOnly = true)
    public List<PrecoConsultaResponseDTO> consultarPrecos(UUID tenantId) {
        Map<UUID, Material> materiais = mapaMateriais(tenantId);

        Map<UUID, List<PrecoMaterialProjecao>> porMaterial = new LinkedHashMap<>();
        for (PrecoMaterialProjecao proj : pedidoItemRepository.projecaoPrecosPorMaterial(tenantId)) {
            porMaterial.computeIfAbsent(proj.materialId(), k -> new ArrayList<>()).add(proj);
        }

        List<PrecoConsultaResponseDTO> resultado = new ArrayList<>();
        porMaterial.forEach((materialId, precos) -> {
            Material material = materiais.get(materialId);
            String descricao = material != null ? material.getDescricao() : "Material";
            String unidade = material != null ? material.getUnidadeMedida() : "";

            BigDecimal soma = BigDecimal.ZERO;
            for (PrecoMaterialProjecao p : precos) {
                soma = soma.add(p.valorUnitario());
            }
            BigDecimal medio = soma.divide(BigDecimal.valueOf(precos.size()), 4, RoundingMode.HALF_UP);
            PrecoMaterialProjecao ultimo = precos.get(0);

            resultado.add(new PrecoConsultaResponseDTO(
                    materialId, descricao, unidade,
                    ultimo.dataEmissao(), ultimo.valorUnitario(), medio, precos.size()));
        });

        resultado.sort((a, b) -> a.materialDescricao().compareToIgnoreCase(b.materialDescricao()));
        return resultado;
    }

    // ============================ AUXILIARES ============================

    private Fornecedor buscarFornecedorDoTenant(UUID id, UUID tenantId) {
        return fornecedorRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado"));
    }

    private PedidoCompra buscarPedidoDoTenant(UUID id, UUID tenantId) {
        return pedidoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido de compra não encontrado"));
    }

    private void validarContratoDoTenant(UUID contratoId, UUID tenantId) {
        Contrato contrato = contratoRepositoryPort.buscarPorId(contratoId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));
        if (!contrato.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Contrato não pertence à empresa");
        }
    }

    private void liquidarContrato(PedidoCompra pedido, BigDecimal valorEntrada, UUID tenantId) {
        Contrato contrato = contratoRepositoryPort.buscarPorId(pedido.getContratoId())
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));
        if (!contrato.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Contrato não pertence à empresa");
        }

        BigDecimal novaLiquidacao = contrato.getValorLiquidado().add(valorEntrada);
        if (novaLiquidacao.compareTo(contrato.getValorEmpenhado()) > 0) {
            throw new IllegalArgumentException(
                    "Valor da entrada excede o saldo empenhado do contrato (empenhado: "
                            + contrato.getValorEmpenhado() + ", liquidado após entrada: " + novaLiquidacao + ")");
        }

        contrato.atualizarSaldo(null, novaLiquidacao, pedido.getEmpenhoNumero(), null);
        contratoRepositoryPort.salvar(contrato);
    }

    private String proximoNumeroPedido(UUID tenantId) {
        long proximo = pedidoRepository.findAllByTenantIdOrderByDataEmissaoDesc(tenantId).size() + 1;
        return "PC-" + LocalDate.now().getYear() + "-" + String.format("%06d", proximo);
    }

    private String preferir(String informado, String doXml) {
        return informado != null && !informado.isBlank() ? informado : doXml;
    }

    private Map<UUID, String> mapaNomesFornecedores(UUID tenantId) {
        Map<UUID, String> mapa = new HashMap<>();
        fornecedorRepository.findAllByTenantIdOrderByRazaoSocial(tenantId)
                .forEach(f -> mapa.put(f.getId(), f.getRazaoSocial()));
        return mapa;
    }

    private Map<UUID, Material> mapaMateriais(UUID tenantId) {
        Map<UUID, Material> mapa = new HashMap<>();
        materialRepository.findAllByTenantId(tenantId).forEach(m -> mapa.put(m.getId(), m));
        return mapa;
    }

    private List<PedidoCompraItemResponseDTO> mapearItens(PedidoCompra pedido, Map<UUID, Material> materiais) {
        return pedido.getItens().stream()
                .map(item -> {
                    Material material = materiais.get(item.getMaterialId());
                    return PedidoCompraItemResponseDTO.from(
                            item,
                            material != null ? material.getDescricao() : "Material",
                            material != null ? material.getUnidadeMedida() : "");
                })
                .toList();
    }

    static boolean validarChaveNfe(String chave) {
        if (chave == null) return false;
        String c = chave.replaceAll("[^0-9]", "");
        if (c.length() != 44) return false;

        String base = c.substring(0, 43);
        int dvInformado = c.charAt(43) - '0';
        int soma = 0;
        int peso = 2;
        for (int i = base.length() - 1; i >= 0; i--) {
            soma += (base.charAt(i) - '0') * peso;
            peso++;
            if (peso > 9) peso = 2;
        }
        int resto = soma % 11;
        int dvCalculado = resto < 2 ? 0 : 11 - resto;
        return dvCalculado == dvInformado;
    }
}