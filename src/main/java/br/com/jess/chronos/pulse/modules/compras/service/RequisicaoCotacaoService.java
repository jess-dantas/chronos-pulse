package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoFornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoProposta;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoStatus;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoCompraItem;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoStatus;
import br.com.jess.chronos.pulse.modules.compras.repository.CotacaoPropostaRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.CotacaoRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.FornecedorRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.RequisicaoCompraRepository;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarCotacaoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarRequisicaoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CotacaoResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.FornecedorCotacaoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PropostaItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PropostaResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RegistrarPropostasDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RequisicaoItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RequisicaoItemResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RequisicaoResponseDTO;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequisicaoCotacaoService {

    private final RequisicaoCompraRepository requisicaoRepository;
    private final CotacaoRepository cotacaoRepository;
    private final CotacaoPropostaRepository propostaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final MaterialRepository materialRepository;
    private final CpcUsuarioRepositoryPort cpcUsuarioRepositoryPort;
    private final ComprasService comprasService;

    // ============================ REQUISIÇÕES ============================

    @Transactional(readOnly = true)
    public List<RequisicaoResponseDTO> listarRequisicoes(UUID tenantId) {
        Map<UUID, String> solicitantes = mapaNomesSolicitantes(tenantId);
        Map<UUID, Material> materiais = mapaMateriais(tenantId);

        return requisicaoRepository.findAllByTenantIdOrderByDataRequisicaoDesc(tenantId).stream()
                .map(req -> RequisicaoResponseDTO.from(req, nomeOuVazio(solicitantes, req), mapearItens(req, materiais)))
                .toList();
    }

    @Transactional(readOnly = true)
    public RequisicaoResponseDTO buscarRequisicao(UUID id, UUID tenantId) {
        RequisicaoCompra requisicao = buscarRequisicaoDoTenant(id, tenantId);
        Map<UUID, Material> materiais = mapaMateriais(tenantId);
        CpcUsuario solicitante = cpcUsuarioRepositoryPort.buscarPorId(requisicao.getSolicitanteCpcId()).orElse(null);
        return RequisicaoResponseDTO.from(
                requisicao,
                solicitante != null ? solicitante.getNome() : "",
                mapearItens(requisicao, materiais));
    }

    @Transactional
    public RequisicaoResponseDTO criarRequisicao(CadastrarRequisicaoDTO dto, UUID tenantId) {
        CpcUsuario solicitante = cpcUsuarioRepositoryPort.buscarPorId(dto.solicitanteCpcId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitante não encontrado"));
        if (!solicitante.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Solicitante não pertence à empresa");
        }

        Map<UUID, Material> materiais = mapaMateriais(tenantId);

        List<RequisicaoCompraItem> itens = new ArrayList<>();
        Map<UUID, BigDecimal> quantidadePorMaterial = new HashMap<>();
        for (RequisicaoItemDTO itemDTO : dto.itens()) {
            Material material = materiais.get(itemDTO.materialId());
            if (material == null) {
                throw new IllegalArgumentException("Material não encontrado para um dos itens");
            }
            if (quantidadePorMaterial.containsKey(itemDTO.materialId())) {
                throw new IllegalArgumentException("Material duplicado na requisição: " + material.getDescricao());
            }
            quantidadePorMaterial.put(itemDTO.materialId(), itemDTO.quantidade());

            itens.add(RequisicaoCompraItem.builder()
                    .tenantId(tenantId)
                    .materialId(itemDTO.materialId())
                    .quantidade(itemDTO.quantidade())
                    .observacao(itemDTO.observacao())
                    .build());
        }

        RequisicaoCompra requisicao = RequisicaoCompra.builder()
                .tenantId(tenantId)
                .numero(proximoNumeroRequisicao(tenantId))
                .solicitanteCpcId(dto.solicitanteCpcId())
                .justificativa(dto.justificativa().trim())
                .dataRequisicao(LocalDate.now())
                .observacoes(dto.observacoes())
                .status(RequisicaoStatus.EM_ABERTO)
                .build();

        itens.forEach(requisicao::adicionarItem);
        requisicao = requisicaoRepository.save(requisicao);

        return RequisicaoResponseDTO.from(
                requisicao, solicitante.getNome(), mapearItens(requisicao, materiais));
    }

    @Transactional
    public void cancelarRequisicao(UUID id, UUID tenantId) {
        RequisicaoCompra requisicao = buscarRequisicaoDoTenant(id, tenantId);
        if (requisicao.getStatus() == RequisicaoStatus.CANCELADA) {
            throw new IllegalArgumentException("Requisição já está cancelada");
        }
        if (requisicao.getStatus() == RequisicaoStatus.COTADA) {
            throw new IllegalArgumentException("Requisição já cotada não pode ser cancelada");
        }
        cotacaoRepository.findByRequisicaoIdAndTenantId(id, tenantId).ifPresent(cotacao -> {
            if (cotacao.getStatus() != CotacaoStatus.CANCELADA) {
                throw new IllegalArgumentException(
                        "Existe uma cotação em andamento/concluída para esta requisição; cancele-a primeiro");
            }
        });
        requisicao.setStatus(RequisicaoStatus.CANCELADA);
        requisicaoRepository.save(requisicao);
    }

    // ============================ COTAÇÕES ============================

    @Transactional(readOnly = true)
    public List<CotacaoResponseDTO> listarCotacoes(UUID tenantId) {
        Map<UUID, Material> materiais = mapaMateriais(tenantId);

        return cotacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId).stream()
                .map(cotacao -> mapearCotacao(cotacao, materiais))
                .toList();
    }

    @Transactional(readOnly = true)
    public CotacaoResponseDTO buscarCotacao(UUID id, UUID tenantId) {
        CotacaoCompra cotacao = buscarCotacaoDoTenant(id, tenantId);
        return mapearCotacao(cotacao, mapaMateriais(tenantId));
    }

    @Transactional
    public CotacaoResponseDTO criarCotacao(CadastrarCotacaoDTO dto, UUID tenantId) {
        RequisicaoCompra requisicao = buscarRequisicaoDoTenant(dto.requisicaoId(), tenantId);
        if (requisicao.getStatus() != RequisicaoStatus.EM_ABERTO) {
            throw new IllegalArgumentException("Somente requisições em aberto podem ser cotadas");
        }
        cotacaoRepository.findByRequisicaoIdAndTenantId(requisicao.getId(), tenantId).ifPresent(existente -> {
            throw new IllegalArgumentException("Já existe cotação para esta requisição");
        });
        if (dto.fornecedoresIds().stream().distinct().count() != dto.fornecedoresIds().size()) {
            throw new IllegalArgumentException("Fornecedor duplicado na lista de convidados");
        }

        List<CotacaoFornecedor> convidados = new ArrayList<>();
        for (UUID fornecedorId : dto.fornecedoresIds()) {
            Fornecedor fornecedor = buscarFornecedorDoTenant(fornecedorId, tenantId);
            if (!Boolean.TRUE.equals(fornecedor.getAtivo())) {
                throw new IllegalArgumentException("Fornecedor inativo não pode ser convidado: " + fornecedor.getRazaoSocial());
            }
            convidados.add(CotacaoFornecedor.builder()
                    .tenantId(tenantId)
                    .fornecedor(fornecedor)
                    .build());
        }

        CotacaoCompra cotacao = CotacaoCompra.builder()
                .tenantId(tenantId)
                .numero(proximoNumeroCotacao(tenantId))
                .requisicao(requisicao)
                .dataLimite(dto.dataLimite())
                .observacoes(dto.observacoes())
                .status(CotacaoStatus.EM_ANDAMENTO)
                .pedidoGerado(Boolean.FALSE)
                .build();

        convidados.forEach(cotacao::adicionarFornecedor);
        cotacao = cotacaoRepository.save(cotacao);

        return mapearCotacao(cotacao, mapaMateriais(tenantId));
    }

    @Transactional
    public CotacaoResponseDTO registrarPropostas(UUID cotacaoId, RegistrarPropostasDTO dto, UUID tenantId) {
        CotacaoCompra cotacao = buscarCotacaoDoTenant(cotacaoId, tenantId);
        validarCotacaoEmAndamento(cotacao);

        List<UUID> convidados = cotacao.getFornecedores().stream()
                .map(c -> c.getFornecedor().getId())
                .toList();
        if (!convidados.contains(dto.fornecedorId())) {
            throw new IllegalArgumentException("Fornecedor não participa desta cotação");
        }

        Map<UUID, RequisicaoCompraItem> itensRequisicao = new HashMap<>();
        cotacao.getRequisicao().getItens()
                .forEach(item -> itensRequisicao.put(item.getMaterialId(), item));

        Map<UUID, BigDecimal> porMaterial = new HashMap<>();
        for (PropostaItemDTO itemDTO : dto.itens()) {
            if (!itensRequisicao.containsKey(itemDTO.materialId())) {
                throw new IllegalArgumentException("Material proposto não pertence à requisição");
            }
            if (porMaterial.containsKey(itemDTO.materialId())) {
                throw new IllegalArgumentException("Material duplicado na proposta: " + itemDTO.materialId());
            }
            porMaterial.put(itemDTO.materialId(), itemDTO.valorUnitario());
        }

        List<CotacaoProposta> atuais = propostaRepository.findAllByCotacaoId(cotacaoId).stream()
                .filter(p -> p.getFornecedorId().equals(dto.fornecedorId()))
                .toList();
        cotacao.getPropostas().removeAll(atuais);

        for (Map.Entry<UUID, BigDecimal> item : porMaterial.entrySet()) {
            cotacao.adicionarProposta(CotacaoProposta.builder()
                    .tenantId(tenantId)
                    .fornecedorId(dto.fornecedorId())
                    .materialId(item.getKey())
                    .valorUnitario(item.getValue())
                    .vencedor(Boolean.FALSE)
                    .build());
        }

        cotacaoRepository.save(cotacao);
        return mapearCotacao(cotacao, mapaMateriais(tenantId));
    }

    @Transactional
    public CotacaoResponseDTO concluirCotacao(UUID cotacaoId, UUID tenantId) {
        CotacaoCompra cotacao = buscarCotacaoDoTenant(cotacaoId, tenantId);
        validarCotacaoEmAndamento(cotacao);

        List<UUID> materiaisRequisicao = cotacao.getRequisicao().getItens().stream()
                .map(RequisicaoCompraItem::getMaterialId)
                .toList();

        cotacao.getPropostas().forEach(proposta -> proposta.setVencedor(Boolean.FALSE));
        for (UUID materialId : materiaisRequisicao) {
            CotacaoProposta melhor = cotacao.getPropostas().stream()
                    .filter(p -> p.getMaterialId().equals(materialId))
                    .min(Comparator.comparing(CotacaoProposta::getValorUnitario))
                    .orElse(null);
            if (melhor == null) {
                throw new IllegalArgumentException(
                        "Todos os itens da requisição precisam de ao menos uma proposta para concluir a cotação");
            }
            melhor.setVencedor(Boolean.TRUE);
        }

        cotacao.setStatus(CotacaoStatus.CONCLUIDA);
        cotacao.getRequisicao().setStatus(RequisicaoStatus.COTADA);
        cotacaoRepository.save(cotacao);

        return mapearCotacao(cotacao, mapaMateriais(tenantId));
    }

    @Transactional
    public void cancelarCotacao(UUID id, UUID tenantId) {
        CotacaoCompra cotacao = buscarCotacaoDoTenant(id, tenantId);
        if (cotacao.getStatus() != CotacaoStatus.EM_ANDAMENTO) {
            throw new IllegalArgumentException("Somente cotações em andamento podem ser canceladas");
        }
        cotacao.setStatus(CotacaoStatus.CANCELADA);
        cotacaoRepository.save(cotacao);
    }

    @Transactional
    public List<PedidoCompraResponseDTO> gerarPedidos(UUID cotacaoId, UUID tenantId) {
        CotacaoCompra cotacao = buscarCotacaoDoTenant(cotacaoId, tenantId);
        if (cotacao.getStatus() != CotacaoStatus.CONCLUIDA) {
            throw new IllegalArgumentException("Somente cotações concluídas geram pedido de compra");
        }
        if (Boolean.TRUE.equals(cotacao.getPedidoGerado())) {
            throw new IllegalArgumentException("Pedido(s) já gerado(s) para esta cotação");
        }

        Map<UUID, BigDecimal> quantidadePorMaterial = new HashMap<>();
        cotacao.getRequisicao().getItens()
                .forEach(item -> quantidadePorMaterial.put(item.getMaterialId(), item.getQuantidade()));

        Map<UUID, List<CotacaoProposta>> vencedoresPorFornecedor = new LinkedHashMap<>();
        for (CotacaoProposta proposta : cotacao.getPropostas()) {
            if (Boolean.TRUE.equals(proposta.getVencedor())) {
                vencedoresPorFornecedor
                        .computeIfAbsent(proposta.getFornecedorId(), k -> new ArrayList<>())
                        .add(proposta);
            }
        }
        if (vencedoresPorFornecedor.isEmpty()) {
            throw new IllegalArgumentException("Nenhum vencedor definido na cotação");
        }

        String objeto = kotacaoObjeto(cotacao);
        List<PedidoCompraResponseDTO> pedidos = new ArrayList<>();
        for (Map.Entry<UUID, List<CotacaoProposta>> grupo : vencedoresPorFornecedor.entrySet()) {
            List<PedidoCompraItemDTO> itensDTO = grupo.getValue().stream()
                    .map(p -> new PedidoCompraItemDTO(
                            p.getMaterialId(),
                            quantidadePorMaterial.getOrDefault(p.getMaterialId(), BigDecimal.ZERO),
                            p.getValorUnitario()))
                    .toList();
            pedidos.add(comprasService.criarPedidoPorItens(
                    grupo.getKey(), objeto, null, null, null, cotacao.getObservacoes(), itensDTO, tenantId));
        }

        cotacao.setPedidoGerado(Boolean.TRUE);
        cotacaoRepository.save(cotacao);
        return pedidos;
    }

    // ============================ AUXILIARES ============================

    private String kotacaoObjeto(CotacaoCompra cotacao) {
        String justificativa = cotacao.getRequisicao().getJustificativa();
        String prefixo = "Aquisição referente à " + cotacao.getNumero();
        return justificativa == null || justificativa.isBlank()
                ? prefixo
                : prefixo + " — " + justificativa.trim();
    }

    private RequisicaoCompra buscarRequisicaoDoTenant(UUID id, UUID tenantId) {
        return requisicaoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Requisição de compra não encontrada"));
    }

    private CotacaoCompra buscarCotacaoDoTenant(UUID id, UUID tenantId) {
        return cotacaoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Cotação não encontrada"));
    }

    private Fornecedor buscarFornecedorDoTenant(UUID id, UUID tenantId) {
        return fornecedorRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado"));
    }

    private void validarCotacaoEmAndamento(CotacaoCompra cotacao) {
        if (cotacao.getStatus() != CotacaoStatus.EM_ANDAMENTO) {
            throw new IllegalArgumentException("Cotação não está em andamento");
        }
    }

    private String proximoNumeroRequisicao(UUID tenantId) {
        long proximo = requisicaoRepository.findAllByTenantIdOrderByDataRequisicaoDesc(tenantId).size() + 1;
        return "RC-" + LocalDate.now().getYear() + "-" + String.format("%06d", proximo);
    }

    private String proximoNumeroCotacao(UUID tenantId) {
        long proximo = cotacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId).size() + 1;
        return "COT-" + LocalDate.now().getYear() + "-" + String.format("%06d", proximo);
    }

    private Map<UUID, String> mapaNomesSolicitantes(UUID tenantId) {
        Map<UUID, String> mapa = new HashMap<>();
        requisicaoRepository.findAllByTenantIdOrderByDataRequisicaoDesc(tenantId).stream()
                .map(RequisicaoCompra::getSolicitanteCpcId)
                .distinct()
                .forEach(cpcId -> cpcUsuarioRepositoryPort.buscarPorId(cpcId)
                        .ifPresent(u -> mapa.put(cpcId, u.getNome())));
        return mapa;
    }

    private String nomeOuVazio(Map<UUID, String> mapa, RequisicaoCompra requisicao) {
        return mapa.getOrDefault(requisicao.getSolicitanteCpcId(), "");
    }

    private Map<UUID, Material> mapaMateriais(UUID tenantId) {
        Map<UUID, Material> mapa = new HashMap<>();
        materialRepository.findAllByTenantId(tenantId).forEach(m -> mapa.put(m.getId(), m));
        return mapa;
    }

    private List<RequisicaoItemResponseDTO> mapearItens(RequisicaoCompra requisicao, Map<UUID, Material> materiais) {
        return requisicao.getItens().stream()
                .map(item -> {
                    Material material = materiais.get(item.getMaterialId());
                    return RequisicaoItemResponseDTO.from(
                            item,
                            material != null ? material.getDescricao() : "Material",
                            material != null ? material.getUnidadeMedida() : "");
                })
                .toList();
    }

    private CotacaoResponseDTO mapearCotacao(CotacaoCompra cotacao, Map<UUID, Material> materiais) {
        List<FornecedorCotacaoDTO> fornecedores = cotacao.getFornecedores().stream()
                .map(f -> new FornecedorCotacaoDTO(
                        f.getFornecedor().getId(), f.getFornecedor().getRazaoSocial()))
                .toList();
        Map<UUID, String> nomesFornecedores = new HashMap<>();
        fornecedores.forEach(f -> nomesFornecedores.put(f.fornecedorId(), f.razaoSocial()));

        List<PropostaResponseDTO> propostas = cotacao.getPropostas().stream()
                .map(p -> {
                    Material material = materiais.get(p.getMaterialId());
                    return PropostaResponseDTO.from(
                            p,
                            nomesFornecedores.getOrDefault(p.getFornecedorId(), ""),
                            material != null ? material.getDescricao() : "Material");
                })
                .toList();

        return CotacaoResponseDTO.from(
                cotacao,
                cotacao.getRequisicao().getNumero(),
                fornecedores,
                propostas);
    }
}