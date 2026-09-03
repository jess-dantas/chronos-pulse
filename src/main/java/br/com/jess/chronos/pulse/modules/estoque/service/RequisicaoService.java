package br.com.jess.chronos.pulse.modules.estoque.service;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.*;
import br.com.jess.chronos.pulse.modules.estoque.repository.AlmoxarifadoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.RequisicaoRepository;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequisicaoService {

    private final RequisicaoRepository requisicaoRepository;
    private final AlmoxarifadoRepository almoxarifadoRepository;
    private final MaterialRepository materialRepository;
    private final EstoqueMovimentacaoService estoqueMovimentacaoService;

    @Transactional
    public RequisicaoResponseDTO criarRequisicao(CriarRequisicaoDTO dto, UUID tenantId, UUID solicitanteCpcId) {
        Almoxarifado almoxarifado = almoxarifadoRepository.findByIdAndTenantId(dto.almoxarifadoId(), tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Almoxarifado não encontrado"));

        Requisicao requisicao = Requisicao.builder()
                .tenantId(tenantId)
                .almoxarifado(almoxarifado)
                .solicitanteCpcId(solicitanteCpcId)
                .departamento(dto.departamento())
                .justificativa(dto.justificativa())
                .status(RequisicaoStatus.PENDENTE)
                .build();

        for (var itemDto : dto.itens()) {
            Material material = materialRepository.findByIdAndTenantId(itemDto.materialId(), tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("Material não encontrado: " + itemDto.materialId()));

            RequisicaoItem item = RequisicaoItem.builder()
                    .material(material)
                    .quantidadeSolicitada(itemDto.quantidadeSolicitada())
                    .build();

            requisicao.adicionarItem(item);
        }

        Requisicao salva = requisicaoRepository.save(requisicao);
        return toResponseDTO(salva);
    }

    @Transactional(readOnly = true)
    public Page<RequisicaoResponseDTO> listarRequisicoes(UUID tenantId, RequisicaoStatus status, Pageable pageable) {
        if (status != null) {
            return requisicaoRepository.findAllByTenantIdAndStatusOrderByDataSolicitacaoDesc(tenantId, status, pageable)
                    .map(this::toResponseDTO);
        }
        return requisicaoRepository.findAllByTenantIdOrderByDataSolicitacaoDesc(tenantId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public RequisicaoResponseDTO buscarPorId(UUID id, UUID tenantId) {
        return requisicaoRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Requisição não encontrada"));
    }

    @Transactional
    public RequisicaoResponseDTO aprovarRequisicao(UUID id, UUID tenantId) {
        Requisicao requisicao = requisicaoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Requisição não encontrada"));

        if (requisicao.getStatus() != RequisicaoStatus.PENDENTE) {
            throw new IllegalStateException("Apenas requisições com status PENDENTE podem ser aprovadas");
        }

        requisicao.setStatus(RequisicaoStatus.APROVADA);
        return toResponseDTO(requisicaoRepository.save(requisicao));
    }

    @Transactional
    public RequisicaoResponseDTO atenderRequisicao(UUID id, UUID tenantId, UUID atendenteCpcId) {
        Requisicao requisicao = requisicaoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Requisição não encontrada"));

        if (requisicao.getStatus() != RequisicaoStatus.APROVADA && requisicao.getStatus() != RequisicaoStatus.PENDENTE) {
            throw new IllegalStateException("Apenas requisições PENDENTES ou APROVADAS podem ser atendidas");
        }

        // Realiza a saída do estoque para cada item da requisição
        for (RequisicaoItem item : requisicao.getItens()) {
            SaidaMaterialDTO saidaDTO = new SaidaMaterialDTO(
                    requisicao.getAlmoxarifado().getId(),
                    item.getMaterial().getId(),
                    item.getQuantidadeSolicitada(),
                    null, // lote se genérico
                    "REQ-" + requisicao.getId().toString().substring(0, 8),
                    "Atendimento da Requisição " + requisicao.getId()
            );

            estoqueMovimentacaoService.registrarSaida(saidaDTO, tenantId, atendenteCpcId);
            item.setQuantidadeAtendida(item.getQuantidadeSolicitada());
        }

        requisicao.setStatus(RequisicaoStatus.ATENDIDA);
        requisicao.setDataAtendimento(OffsetDateTime.now());
        requisicao.setAtendenteCpcId(atendenteCpcId);

        return toResponseDTO(requisicaoRepository.save(requisicao));
    }

    private RequisicaoResponseDTO toResponseDTO(Requisicao req) {
        var itensDTO = req.getItens().stream()
                .map(i -> new ItemRequisicaoResponseDTO(
                        i.getId(),
                        i.getMaterial().getId(),
                        i.getMaterial().getDescricao(),
                        i.getMaterial().getUnidadeMedida(),
                        i.getQuantidadeSolicitada(),
                        i.getQuantidadeAtendida()
                ))
                .toList();

        return new RequisicaoResponseDTO(
                req.getId(),
                req.getAlmoxarifado().getId(),
                req.getAlmoxarifado().getNome(),
                req.getSolicitanteCpcId(),
                req.getDepartamento(),
                req.getJustificativa(),
                req.getStatus(),
                req.getDataSolicitacao(),
                req.getDataAtendimento(),
                req.getAtendenteCpcId(),
                itensDTO
        );
    }
}
