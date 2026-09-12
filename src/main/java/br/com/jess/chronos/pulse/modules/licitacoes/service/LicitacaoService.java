package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import br.com.jess.chronos.pulse.modules.compras.repository.FornecedorRepository;
import br.com.jess.chronos.pulse.modules.compras.service.ComprasService;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.*;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoContratoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoEditalRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoLanceRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoPropostaRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LicitacaoService {

    private final LicitacaoRepository licitacaoRepository;
    private final LicitacaoPropostaRepository propostaRepository;
    private final LicitacaoLanceRepository lanceRepository;
    private final LicitacaoEditalRepository editalRepository;
    private final LicitacaoContratoRepository contratoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final MaterialRepository materialRepository;
    private final ComprasService comprasService;
    private final PncpService pncpService;

    @Transactional(readOnly = true)
    public List<LicitacaoResponseDTO> listarLicitacoes(UUID tenantId) {
        Map<UUID, Material> materiais = mapaMateriais(tenantId);
        return licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId).stream()
                .map(l -> mapearLicitacao(l, materiais))
                .toList();
    }

    @Transactional(readOnly = true)
    public LicitacaoResponseDTO buscarLicitacao(UUID id, UUID tenantId) {
        return mapearLicitacao(buscarLicitacaoDoTenant(id, tenantId), mapaMateriais(tenantId));
    }

    @Transactional
    public LicitacaoResponseDTO criarLicitacao(CadastrarLicitacaoDTO dto, UUID tenantId) {
        LicitacaoModalidade modalidade = validarModalidade(dto.modalidade());
        LicitacaoTipoJulgamento julgamento = validarJulgamento(dto.tipoJulgamento());

        Map<UUID, Material> materiais = mapaMateriais(tenantId);
        Map<String, String> descricoes = new HashMap<>();
        List<LicitacaoItem> itens = new ArrayList<>();
        BigDecimal valorEstimado = BigDecimal.ZERO;
        for (LicitacaoItemDTO itemDTO : dto.itens()) {
            Material material = materiais.get(itemDTO.materialId());
            if (material == null) {
                throw new IllegalArgumentException("Material não encontrado para um dos itens");
            }
            String chave = itemDTO.materialId().toString();
            if (descricoes.containsKey(chave)) {
                throw new IllegalArgumentException("Material duplicado na licitação: " + material.getDescricao());
            }
            descricoes.put(chave, material.getDescricao());

            LicitacaoItem item = LicitacaoItem.builder()
                    .tenantId(tenantId)
                    .materialId(itemDTO.materialId())
                    .descricao(material.getDescricao())
                    .quantidade(itemDTO.quantidade())
                    .valorEstimadoUnitario(itemDTO.valorEstimadoUnitario())
                    .build();
            itens.add(item);
            valorEstimado = valorEstimado.add(item.getValorEstimadoTotal());
        }

        Licitacao licitacao = Licitacao.builder()
                .tenantId(tenantId)
                .numero(proximoNumeroLicitacao(tenantId))
                .modalidade(modalidade)
                .tipoJulgamento(julgamento)
                .objeto(dto.objeto())
                .dataAbertura(dto.dataAbertura())
                .valorEstimado(valorEstimado.setScale(2, RoundingMode.HALF_UP))
                .observacoes(dto.observacoes())
                .status(LicitacaoStatus.EM_ELABORACAO)
                .build();

        itens.forEach(licitacao::adicionarItem);
        licitacao = licitacaoRepository.save(licitacao);

        return mapearLicitacao(licitacao, materiais);
    }

    @Transactional
    public LicitacaoResponseDTO publicarLicitacao(UUID id, PublicarLicitacaoDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(id, tenantId);
        if (licitacao.getStatus() != LicitacaoStatus.EM_ELABORACAO) {
            throw new IllegalArgumentException("Somente licitações em elaboração podem ser publicadas");
        }
        if (dto.fornecedoresIds().stream().distinct().count() != dto.fornecedoresIds().size()) {
            throw new IllegalArgumentException("Fornecedor duplicado na lista de habilitados");
        }
        for (UUID fornecedorId : dto.fornecedoresIds()) {
            Fornecedor fornecedor = buscarFornecedorDoTenant(fornecedorId, tenantId);
            if (!Boolean.TRUE.equals(fornecedor.getAtivo())) {
                throw new IllegalArgumentException("Fornecedor inativo não pode participar: " + fornecedor.getRazaoSocial());
            }
            licitacao.adicionarParticipante(LicitacaoParticipante.builder()
                    .tenantId(tenantId)
                    .fornecedor(fornecedor)
                    .build());
        }
        licitacao.setStatus(LicitacaoStatus.PUBLICADA);
        licitacao = licitacaoRepository.save(licitacao);
        return mapearLicitacao(licitacao, mapaMateriais(tenantId));
    }

    @Transactional
    public LicitacaoResponseDTO publicarPncp(UUID id, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(id, tenantId);
        if (licitacao.getStatus() != LicitacaoStatus.PUBLICADA
                && licitacao.getStatus() != LicitacaoStatus.ABERTA) {
            throw new IllegalArgumentException(
                    "Somente licitações publicadas/em disputa podem ter o aviso publicado no PNCP");
        }
        if (licitacao.getPncpStatus() == LicitacaoPncpStatus.PUBLICADO) {
            throw new IllegalArgumentException(
                    "Aviso da licitação " + licitacao.getNumero() + " já publicado no PNCP");
        }

        try {
            LicitacaoEdital edital = editalRepository.findByLicitacaoId(id).orElse(null);
            PncpResultado resultado = pncpService.publicarAviso(licitacao, edital);
            licitacao.setPncpStatus(LicitacaoPncpStatus.PUBLICADO);
            licitacao.setPncpProtocolo(resultado.protocolo());
            licitacao.setPncpPublicadoEm(resultado.publicadoEm());
            licitacao.setPncpErro(null);
        } catch (Exception e) {
            licitacao.setPncpStatus(LicitacaoPncpStatus.FALHA);
            licitacao.setPncpProtocolo(null);
            licitacao.setPncpPublicadoEm(null);
            licitacao.setPncpErro(e.getMessage());
        }
        licitacao = licitacaoRepository.save(licitacao);
        return mapearLicitacao(licitacao, mapaMateriais(tenantId));
    }

    @Transactional
    public LicitacaoResponseDTO registrarPropostas(UUID licitacaoId, RegistrarPropostasLicitacaoDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(licitacaoId, tenantId);
        validarEmDisputa(licitacao);

        List<UUID> participanteIds = licitacao.getParticipantes().stream()
                .map(p -> p.getFornecedor().getId())
                .toList();
        if (!participanteIds.contains(dto.fornecedorId())) {
            throw new IllegalArgumentException("Fornecedor não habilitado para esta licitação");
        }

        Set<UUID> materiaisLicitacao = new HashSet<>();
        licitacao.getItens().forEach(item -> materiaisLicitacao.add(item.getMaterialId()));

        Map<UUID, BigDecimal> porMaterial = new HashMap<>();
        for (LicitacaoPropostaDTO itemDTO : dto.itens()) {
            if (!materiaisLicitacao.contains(itemDTO.materialId())) {
                throw new IllegalArgumentException("Material proposto não pertence à licitação");
            }
            if (porMaterial.containsKey(itemDTO.materialId())) {
                throw new IllegalArgumentException("Material duplicado na proposta: " + itemDTO.materialId());
            }
            porMaterial.put(itemDTO.materialId(), itemDTO.valorUnitario());
        }

        List<LicitacaoProposta> atuais = propostaRepository.findAllByLicitacaoId(licitacaoId).stream()
                .filter(p -> p.getFornecedorId().equals(dto.fornecedorId()))
                .toList();
        licitacao.getPropostas().removeAll(atuais);

        for (Map.Entry<UUID, BigDecimal> item : porMaterial.entrySet()) {
            licitacao.adicionarProposta(LicitacaoProposta.builder()
                    .tenantId(tenantId)
                    .fornecedorId(dto.fornecedorId())
                    .materialId(item.getKey())
                    .valorUnitario(item.getValue())
                    .vencedor(Boolean.FALSE)
                    .build());
        }

        licitacao = licitacaoRepository.save(licitacao);
        return mapearLicitacao(licitacao, mapaMateriais(tenantId));
    }

    @Transactional
    public LicitacaoResponseDTO adjudicarLicitacao(UUID licitacaoId, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(licitacaoId, tenantId);
        validarEmDisputa(licitacao);

        List<LicitacaoLance> lancesAtuais = lanceRepository.findAllByLicitacaoIdOrderByAtualizadoEmDesc(licitacaoId);
        boolean usarLances = !lancesAtuais.isEmpty();

        if (usarLances && licitacao.getTipoJulgamento() == LicitacaoTipoJulgamento.MENOR_PRECO) {
            adjudicarPorLancesMenorPreco(licitacao, lancesAtuais);
        } else if (usarLances && licitacao.getTipoJulgamento() == LicitacaoTipoJulgamento.MAIOR_LANCE) {
            adjudicarPorLancesMaiorLance(licitacao, lancesAtuais);
        } else if (!usarLances) {
            adjudicarPorPropostas(licitacao);
        } else {
            throw new IllegalArgumentException(
                    "Julgamento automático disponível apenas para menor preço e maior lance; "
                            + licitacao.getTipoJulgamento() + " exige análise técnica da comissão");
        }

        licitacao.setStatus(LicitacaoStatus.ADJUDICADA);
        licitacao = licitacaoRepository.save(licitacao);
        return mapearLicitacao(licitacao, mapaMateriais(tenantId));
    }

    @Transactional
    public LicitacaoResponseDTO abrirDisputa(UUID licitacaoId, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(licitacaoId, tenantId);
        if (licitacao.getStatus() != LicitacaoStatus.PUBLICADA) {
            throw new IllegalArgumentException("Somente licitações publicadas podem abrir disputa");
        }
        licitacao.setStatus(LicitacaoStatus.ABERTA);
        licitacao = licitacaoRepository.save(licitacao);
        return mapearLicitacao(licitacao, mapaMateriais(tenantId));
    }

    @Transactional
    public LicitacaoResponseDTO registrarLance(UUID licitacaoId, RegistrarLanceDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(licitacaoId, tenantId);
        validarEmDisputa(licitacao);

        List<UUID> participanteIds = licitacao.getParticipantes().stream()
                .map(p -> p.getFornecedor().getId())
                .toList();
        if (!participanteIds.contains(dto.fornecedorId())) {
            throw new IllegalArgumentException("Fornecedor não habilitado para esta licitação");
        }

        licitacao.getItens().stream()
                .filter(i -> i.getId().equals(dto.licitacaoItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não pertence a esta licitação"));

        Optional<LicitacaoLance> existenteOpt = lanceRepository
                .findByLicitacaoIdAndLicitacaoItemIdAndFornecedorId(
                        licitacaoId, dto.licitacaoItemId(), dto.fornecedorId());

        if (existenteOpt.isPresent()) {
            LicitacaoLance existente = existenteOpt.get();
            if (licitacao.getTipoJulgamento() == LicitacaoTipoJulgamento.MENOR_PRECO
                    && dto.valorUnitario().compareTo(existente.getValorUnitario()) >= 0) {
                throw new IllegalArgumentException(
                        "Lance deve ser inferior ao lance atual (R$ " + existente.getValorUnitario() + ")");
            }
            if (licitacao.getTipoJulgamento() == LicitacaoTipoJulgamento.MAIOR_LANCE
                    && dto.valorUnitario().compareTo(existente.getValorUnitario()) <= 0) {
                throw new IllegalArgumentException(
                        "Lance deve ser superior ao lance atual (R$ " + existente.getValorUnitario() + ")");
            }
            existente.setValorUnitario(dto.valorUnitario());
            existente.setObservacao(dto.observacao());
            existente.setAtualizadoEm(java.time.Instant.now());
            lanceRepository.save(existente);
        } else {
            licitacao.adicionarLance(LicitacaoLance.builder()
                    .tenantId(tenantId)
                    .licitacaoItemId(dto.licitacaoItemId())
                    .fornecedorId(dto.fornecedorId())
                    .valorUnitario(dto.valorUnitario())
                    .observacao(dto.observacao())
                    .build());
            licitacaoRepository.save(licitacao);
        }

        return mapearLicitacao(licitacao, mapaMateriais(tenantId));
    }

    @Transactional(readOnly = true)
    public List<LanceResponseDTO> listarLances(UUID licitacaoId, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(licitacaoId, tenantId);
        Map<UUID, Material> materiais = mapaMateriais(tenantId);
        Map<UUID, String> nomesFornecedores = new HashMap<>();
        licitacao.getParticipantes().forEach(p ->
                nomesFornecedores.put(p.getFornecedor().getId(), p.getFornecedor().getRazaoSocial()));

        return lanceRepository.findAllByLicitacaoIdOrderByAtualizadoEmDesc(licitacaoId).stream()
                .map(lance -> {
                    LicitacaoItem item = licitacao.getItens().stream()
                            .filter(i -> i.getId().equals(lance.getLicitacaoItemId()))
                            .findFirst().orElse(null);
                    Material material = item != null ? materiais.get(item.getMaterialId()) : null;
                    return LanceResponseDTO.from(
                            lance,
                            item != null ? item.getDescricao() : "",
                            nomesFornecedores.getOrDefault(lance.getFornecedorId(), ""),
                            item != null ? item.getValorEstimadoUnitario() : null);
                })
                .toList();
    }

    // ======================== ADJUDICAR POR LANCES ========================

    private void adjudicarPorLancesMenorPreco(Licitacao licitacao, List<LicitacaoLance> lancesAtuais) {
        licitacao.getPropostas().clear();

        for (LicitacaoItem item : licitacao.getItens()) {
            LicitacaoLance melhor = lancesAtuais.stream()
                    .filter(l -> l.getLicitacaoItemId().equals(item.getId()))
                    .min(Comparator.comparing(LicitacaoLance::getValorUnitario))
                    .orElse(null);
            if (melhor == null) {
                throw new IllegalArgumentException(
                        "Todos os itens precisam de ao menos um lance para adjudicar");
            }
            licitacao.adicionarProposta(LicitacaoProposta.builder()
                    .tenantId(licitacao.getTenantId())
                    .fornecedorId(melhor.getFornecedorId())
                    .materialId(item.getMaterialId())
                    .valorUnitario(melhor.getValorUnitario())
                    .vencedor(Boolean.TRUE)
                    .build());
        }
    }

    private void adjudicarPorLancesMaiorLance(Licitacao licitacao, List<LicitacaoLance> lancesAtuais) {
        licitacao.getPropostas().clear();

        for (LicitacaoItem item : licitacao.getItens()) {
            LicitacaoLance melhor = lancesAtuais.stream()
                    .filter(l -> l.getLicitacaoItemId().equals(item.getId()))
                    .max(Comparator.comparing(LicitacaoLance::getValorUnitario))
                    .orElse(null);
            if (melhor == null) {
                throw new IllegalArgumentException(
                        "Todos os itens precisam de ao menos um lance para adjudicar");
            }
            licitacao.adicionarProposta(LicitacaoProposta.builder()
                    .tenantId(licitacao.getTenantId())
                    .fornecedorId(melhor.getFornecedorId())
                    .materialId(item.getMaterialId())
                    .valorUnitario(melhor.getValorUnitario())
                    .vencedor(Boolean.TRUE)
                    .build());
        }
    }

    private void adjudicarPorPropostas(Licitacao licitacao) {
        if (licitacao.getTipoJulgamento() != LicitacaoTipoJulgamento.MENOR_PRECO) {
            throw new IllegalArgumentException(
                    "Julgamento automático disponível apenas para menor preço e maior lance; "
                            + licitacao.getTipoJulgamento() + " exige análise técnica da comissão");
        }

        List<UUID> materiais = licitacao.getItens().stream()
                .map(LicitacaoItem::getMaterialId).toList();

        licitacao.getPropostas().forEach(p -> p.setVencedor(Boolean.FALSE));
        for (UUID materialId : materiais) {
            LicitacaoProposta melhor = licitacao.getPropostas().stream()
                    .filter(p -> p.getMaterialId().equals(materialId))
                    .min(Comparator.comparing(LicitacaoProposta::getValorUnitario))
                    .orElse(null);
            if (melhor == null) {
                throw new IllegalArgumentException(
                        "Todos os itens da licitação precisam de ao menos uma proposta para adjudicar");
            }
            melhor.setVencedor(Boolean.TRUE);
        }
    }

    // ============================ AUXILIARES ============================

    @Transactional
    public LicitacaoResponseDTO homologarLicitacao(UUID licitacaoId, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(licitacaoId, tenantId);
        if (licitacao.getStatus() != LicitacaoStatus.ADJUDICADA) {
            throw new IllegalArgumentException("Somente licitações adjudicadas podem ser homologadas");
        }
        licitacao.setStatus(LicitacaoStatus.HOMOLOGADA);
        licitacao = licitacaoRepository.save(licitacao);
        return mapearLicitacao(licitacao, mapaMateriais(tenantId));
    }

    @Transactional
    public void cancelarLicitacao(UUID id, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(id, tenantId);
        if (licitacao.getStatus() == LicitacaoStatus.ADJUDICADA
                || licitacao.getStatus() == LicitacaoStatus.HOMOLOGADA
                || licitacao.getStatus() == LicitacaoStatus.CANCELADA) {
            throw new IllegalArgumentException(
                    "Licitação " + licitacao.getNumero() + " não pode ser cancelada "
                            + "após julgamento (" + licitacao.getStatus().name() + ")");
        }
        licitacao.setStatus(LicitacaoStatus.CANCELADA);
        licitacaoRepository.save(licitacao);
    }

    @Transactional
    public List<PedidoCompraResponseDTO> gerarPedidos(UUID licitacaoId, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(licitacaoId, tenantId);
        if (licitacao.getStatus() != LicitacaoStatus.HOMOLOGADA) {
            throw new IllegalArgumentException("Somente licitações homologadas geram pedido de compra");
        }
        if (Boolean.TRUE.equals(licitacao.getPedidoGerado())) {
            throw new IllegalArgumentException("Pedido(s) já gerado(s) para esta licitação");
        }

        Map<UUID, BigDecimal> quantidadePorMaterial = new HashMap<>();
        licitacao.getItens().forEach(item -> quantidadePorMaterial.put(item.getMaterialId(), item.getQuantidade()));

        Map<UUID, List<LicitacaoProposta>> vencedoresPorFornecedor = new LinkedHashMap<>();
        for (LicitacaoProposta proposta : licitacao.getPropostas()) {
            if (Boolean.TRUE.equals(proposta.getVencedor())) {
                vencedoresPorFornecedor
                        .computeIfAbsent(proposta.getFornecedorId(), k -> new ArrayList<>())
                        .add(proposta);
            }
        }
        if (vencedoresPorFornecedor.isEmpty()) {
            throw new IllegalArgumentException("Nenhum vencedor definido na licitação");
        }

        String objeto = "Fornecimento referente à " + licitacao.getNumero() + " — " + licitacao.getObjeto();
        List<PedidoCompraResponseDTO> pedidos = new ArrayList<>();
        for (Map.Entry<UUID, List<LicitacaoProposta>> grupo : vencedoresPorFornecedor.entrySet()) {
            List<PedidoCompraItemDTO> itensDTO = grupo.getValue().stream()
                    .map(p -> new PedidoCompraItemDTO(
                            p.getMaterialId(),
                            quantidadePorMaterial.getOrDefault(p.getMaterialId(), BigDecimal.ZERO),
                            p.getValorUnitario()))
                    .toList();
            pedidos.add(comprasService.criarPedidoPorItens(
                    grupo.getKey(), objeto, null, null, null, licitacao.getObservacoes(), itensDTO, tenantId));
        }

        licitacao.setPedidoGerado(Boolean.TRUE);
        licitacaoRepository.save(licitacao);
        return pedidos;
    }

    @Transactional
    public LicitacaoResponseDTO formalizarContrato(UUID licitacaoId, FormalizarContratoDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoDoTenant(licitacaoId, tenantId);
        if (licitacao.getStatus() != LicitacaoStatus.HOMOLOGADA) {
            throw new IllegalArgumentException("Somente licitações homologadas podem ter contrato formalizado");
        }
        if (Boolean.TRUE.equals(licitacao.getContratoGerado())) {
            throw new IllegalArgumentException("Contrato já formalizado para esta licitação");
        }
        if (dto.dataFim().isBefore(dto.dataInicio())) {
            throw new IllegalArgumentException("A data de fim não pode ser anterior à data de início");
        }

        Map<UUID, BigDecimal> quantidadePorMaterial = new HashMap<>();
        licitacao.getItens().forEach(item -> quantidadePorMaterial.put(item.getMaterialId(), item.getQuantidade()));

        List<LicitacaoProposta> vencedores = licitacao.getPropostas().stream()
                .filter(p -> Boolean.TRUE.equals(p.getVencedor()))
                .toList();
        if (vencedores.isEmpty()) {
            throw new IllegalArgumentException("Nenhum vencedor definido na licitação");
        }
        BigDecimal valorTotal = vencedores.stream()
                .map(p -> quantidadePorMaterial
                        .getOrDefault(p.getMaterialId(), BigDecimal.ZERO)
                        .multiply(p.getValorUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ContratoLicitacao contrato = ContratoLicitacao.builder()
                .tenantId(licitacao.getTenantId())
                .numero("CT-" + licitacao.getNumero())
                .objeto("Fornecimento referente à " + licitacao.getNumero() + " — " + licitacao.getObjeto())
                .dataInicio(dto.dataInicio())
                .dataFim(dto.dataFim())
                .valorMensal(dto.valorMensal() == null ? BigDecimal.ZERO : dto.valorMensal())
                .valorTotal(valorTotal)
                .status("ATIVO")
                .observacoes(dto.observacoes())
                .valorEmpenhado(dto.valorEmpenhado() == null ? BigDecimal.ZERO : dto.valorEmpenhado())
                .valorLiquidado(dto.valorLiquidado() == null ? BigDecimal.ZERO : dto.valorLiquidado())
                .empenhoNumero(dto.empenhoNumero())
                .vencimentoAvisoDias(dto.vencimentoAvisoDias() == null ? 30 : dto.vencimentoAvisoDias())
                .licitacaoId(licitacao.getId())
                .build();
        contratoRepository.save(contrato);

        licitacao.setContratoGerado(Boolean.TRUE);
        licitacaoRepository.save(licitacao);
        return mapearLicitacao(licitacao, mapaMateriais(tenantId));
    }

    // ============================ AUXILIARES ============================

    private LicitacaoModalidade validarModalidade(String valor) {
        try {
            return LicitacaoModalidade.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Modalidade inválida: " + valor);
        }
    }

    private LicitacaoTipoJulgamento validarJulgamento(String valor) {
        try {
            return LicitacaoTipoJulgamento.valueOf(valor);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Critério de julgamento inválido: " + valor);
        }
    }

    private Licitacao buscarLicitacaoDoTenant(UUID id, UUID tenantId) {
        return licitacaoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Licitação não encontrada"));
    }

    private Fornecedor buscarFornecedorDoTenant(UUID id, UUID tenantId) {
        return fornecedorRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Fornecedor não encontrado"));
    }

    private void validarEmDisputa(Licitacao licitacao) {
        if (licitacao.getStatus() != LicitacaoStatus.PUBLICADA
                && licitacao.getStatus() != LicitacaoStatus.ABERTA) {
            throw new IllegalArgumentException("Licitação não está em fase de propostas");
        }
    }

    private String proximoNumeroLicitacao(UUID tenantId) {
        long proximo = licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId).size() + 1;
        return "LIC-" + LocalDate.now().getYear() + "-" + String.format("%06d", proximo);
    }

    private Map<UUID, Material> mapaMateriais(UUID tenantId) {
        Map<UUID, Material> mapa = new HashMap<>();
        materialRepository.findAllByTenantId(tenantId).forEach(m -> mapa.put(m.getId(), m));
        return mapa;
    }

    private LicitacaoResponseDTO mapearLicitacao(Licitacao licitacao, Map<UUID, Material> materiais) {
        Map<UUID, String> nomesFornecedores = new HashMap<>();
        List<LicitacaoParticipanteResponseDTO> participantes = licitacao.getParticipantes().stream()
                .map(p -> {
                    nomesFornecedores.put(p.getFornecedor().getId(), p.getFornecedor().getRazaoSocial());
                    return new LicitacaoParticipanteResponseDTO(
                            p.getFornecedor().getId(), p.getFornecedor().getRazaoSocial(), p.getHabilitado());
                })
                .toList();

        List<LicitacaoItemResponseDTO> itens = licitacao.getItens().stream()
                .map(item -> {
                    Material material = materiais.get(item.getMaterialId());
                    return LicitacaoItemResponseDTO.from(
                            item,
                            material != null ? material.getUnidadeMedida() : "");
                })
                .toList();

        List<LicitacaoPropostaResponseDTO> propostas = licitacao.getPropostas().stream()
                .map(p -> {
                    Material material = materiais.get(p.getMaterialId());
                    return LicitacaoPropostaResponseDTO.from(
                            p,
                            nomesFornecedores.getOrDefault(p.getFornecedorId(), ""),
                            material != null ? material.getDescricao() : "Material");
                })
                .toList();

        List<LanceResponseDTO> lances = licitacao.getLances().stream()
                .sorted(Comparator.comparing(LicitacaoLance::getAtualizadoEm).reversed())
                .map(lance -> {
                    LicitacaoItem item = licitacao.getItens().stream()
                            .filter(i -> i.getId().equals(lance.getLicitacaoItemId()))
                            .findFirst().orElse(null);
                    Material material = item != null ? materiais.get(item.getMaterialId()) : null;
                    return LanceResponseDTO.from(
                            lance,
                            item != null ? item.getDescricao() : "",
                            nomesFornecedores.getOrDefault(lance.getFornecedorId(), ""),
                            item != null ? item.getValorEstimadoUnitario() : null);
                })
                .toList();

        Optional<ContratoLicitacao> contratoOptional = contratoRepository.findByLicitacaoIdAndTenantId(
                licitacao.getId(), licitacao.getTenantId());
        return LicitacaoResponseDTO.from(licitacao, itens, participantes, propostas, lances,
                contratoOptional != null ? contratoOptional.orElse(null) : null);
    }
}