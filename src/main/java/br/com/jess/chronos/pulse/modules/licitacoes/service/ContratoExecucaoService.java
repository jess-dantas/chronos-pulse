package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.*;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.*;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoExecucaoService {

    private static final Set<String> TIPOS_ADITIVO =
            Set.of("VALOR", "PRAZO", "QUANTITATIVO", "OBJETO");
    private static final Set<String> GRAVIDADES_APONTAMENTO =
            Set.of("LEVE", "MEDIA", "GRAVE");
    private static final Set<String> TIPOS_SANCAO = Set.of(
            "ADVERTENCIA", "MULTA", "SUSPENSAO_TEMPORARIA", "IMPEDIMENTO", "DECLARACAO_INIDONEIDADE");
    private static final Set<String> TIPOS_RESCISAO =
            Set.of("UNILATERAL", "AMIGAVEL", "JUDICIAL");

    private final LicitacaoContratoRepository contratoRepository;
    private final ContratoAditivoRepository aditivoRepository;
    private final ContratoApontamentoRepository apontamentoRepository;
    private final ContratoMedicaoRepository medicaoRepository;
    private final ContratoSancaoRepository sancaoRepository;
    private final ContratoRescisaoRepository rescisaoRepository;

    @Transactional(readOnly = true)
    public List<ContratoExecucaoResponseDTO> listarExecucoes(UUID tenantId) {
        return contratoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId).stream()
                .map(c -> buscarExecucao(c.getId(), tenantId))
                .toList();
    }

    @Transactional(readOnly = true)
    public ContratoExecucaoResponseDTO buscarExecucao(UUID contratoId, UUID tenantId) {
        ContratoLicitacao contrato = buscarContratoDoTenant(contratoId, tenantId);
        return montarExecucao(contrato, tenantId);
    }

    @Transactional
    public ContratoAditivoResponseDTO registrarAditivo(
            UUID contratoId, AdicionarAditivoDTO dto, UUID tenantId, UUID criadoPor) {
        ContratoLicitacao contrato = buscarContratoDoTenant(contratoId, tenantId);
        validarContratoNaoRescindido(contrato);
        validarTipoAditivo(dto);

        if ("PRAZO".equals(dto.tipo())) {
            if (dto.prazoAdicionadoDias() == null || dto.prazoAdicionadoDias() <= 0) {
                throw new IllegalArgumentException("Aditivo de prazo exige prazoAdicionadoDias positivo");
            }
        }
        if ("VALOR".equals(dto.tipo())) {
            if (dto.novoValorTotal() == null || dto.novoValorTotal().signum() <= 0) {
                throw new IllegalArgumentException("Aditivo de valor exige novoValorTotal positivo");
            }
        }

        ContratoAditivo aditivo = ContratoAditivo.builder()
                .tenantId(tenantId)
                .contratoId(contratoId)
                .tipo(dto.tipo())
                .descricao(dto.descricao())
                .justificativa(dto.justificativa())
                .prazoAdicionadoDias(dto.prazoAdicionadoDias())
                .novoValorTotal(dto.novoValorTotal())
                .aprovado(Boolean.TRUE)
                .criadoPor(criadoPor)
                .build();
        aditivoRepository.save(aditivo);

        if (dto.prazoAdicionadoDias() != null) {
            contrato.setDataFim(contrato.getDataFim().plusDays(dto.prazoAdicionadoDias()));
        }
        if (dto.novoValorTotal() != null) {
            contrato.setValorTotal(dto.novoValorTotal());
        }
        contratoRepository.save(contrato);

        return ContratoAditivoResponseDTO.from(aditivo);
    }

    @Transactional
    public ContratoApontamentoResponseDTO registrarApontamento(
            UUID contratoId, AdicionarApontamentoDTO dto, UUID tenantId, UUID criadoPor) {
        ContratoLicitacao contrato = buscarContratoDoTenant(contratoId, tenantId);
        validarContratoNaoRescindido(contrato);
        if (!GRAVIDADES_APONTAMENTO.contains(dto.gravidade())) {
            throw new IllegalArgumentException("Gravidade do apontamento inválida: " + dto.gravidade());
        }

        ContratoApontamento apontamento = ContratoApontamento.builder()
                .tenantId(tenantId)
                .contratoId(contratoId)
                .fiscal(dto.fiscal())
                .descricao(dto.descricao())
                .gravidade(dto.gravidade())
                .resolvido(Boolean.FALSE)
                .criadoPor(criadoPor)
                .build();
        apontamentoRepository.save(apontamento);
        return ContratoApontamentoResponseDTO.from(apontamento);
    }

    @Transactional
    public ContratoApontamentoResponseDTO resolverApontamento(
            UUID contratoId, UUID apontamentoId, UUID tenantId) {
        buscarContratoDoTenant(contratoId, tenantId);
        ContratoApontamento apontamento = apontamentoRepository
                .findByIdAndContratoIdAndTenantId(apontamentoId, contratoId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Apontamento não encontrado"));
        if (Boolean.TRUE.equals(apontamento.getResolvido())) {
            throw new IllegalArgumentException("Apontamento já resolvido");
        }
        apontamento.setResolvido(Boolean.TRUE);
        apontamento.setResolvidoEm(Instant.now());
        apontamentoRepository.save(apontamento);
        return ContratoApontamentoResponseDTO.from(apontamento);
    }

    @Transactional
    public ContratoMedicaoResponseDTO registrarMedicao(
            UUID contratoId, RegistrarMedicaoDTO dto, UUID tenantId, UUID criadoPor) {
        ContratoLicitacao contrato = buscarContratoDoTenant(contratoId, tenantId);
        validarContratoNaoRescindido(contrato);

        ContratoMedicao medicao = ContratoMedicao.builder()
                .tenantId(tenantId)
                .contratoId(contratoId)
                .periodo(dto.periodo())
                .valorMedido(dto.valorMedido() == null ? BigDecimal.ZERO : dto.valorMedido())
                .valorPago(dto.valorPago() == null ? BigDecimal.ZERO : dto.valorPago())
                .pagoEm(dto.pagoEm())
                .observacao(dto.observacao())
                .criadoPor(criadoPor)
                .build();
        medicaoRepository.save(medicao);

        if (medicao.getValorPago().signum() > 0) {
            contrato.setValorLiquidado(contrato.getValorLiquidado().add(medicao.getValorPago())
                    .setScale(2, RoundingMode.HALF_UP));
            contratoRepository.save(contrato);
        }
        return ContratoMedicaoResponseDTO.from(medicao);
    }

    @Transactional
    public ContratoSancaoResponseDTO registrarSancao(
            UUID contratoId, AdicionarSancaoDTO dto, UUID tenantId, UUID criadoPor) {
        ContratoLicitacao contrato = buscarContratoDoTenant(contratoId, tenantId);
        validarContratoNaoRescindido(contrato);
        if (!TIPOS_SANCAO.contains(dto.tipo())) {
            throw new IllegalArgumentException("Tipo de sanção inválido: " + dto.tipo());
        }
        if (dto.percentualMulta() == null && dto.valorMulta() == null) {
            throw new IllegalArgumentException("Sanção de multa exige percentualMulta ou valorMulta");
        }

        ContratoSancao sancao = ContratoSancao.builder()
                .tenantId(tenantId)
                .contratoId(contratoId)
                .tipo(dto.tipo())
                .baseLegal(dto.baseLegal())
                .descricao(dto.descricao())
                .percentualMulta(dto.percentualMulta())
                .valorMulta(dto.valorMulta())
                .aplicadaEm(dto.aplicadaEm())
                .criadoPor(criadoPor)
                .build();
        sancaoRepository.save(sancao);
        return ContratoSancaoResponseDTO.from(sancao);
    }

    @Transactional
    public ContratoRescisaoResponseDTO rescindirContrato(
            UUID contratoId, RescindirContratoDTO dto, UUID tenantId, UUID criadoPor) {
        ContratoLicitacao contrato = buscarContratoDoTenant(contratoId, tenantId);
        if ("RESCINDIDO".equals(contrato.getStatus())) {
            throw new IllegalArgumentException("Contrato já rescindido");
        }
        if (!TIPOS_RESCISAO.contains(dto.tipo())) {
            throw new IllegalArgumentException("Tipo de rescisão inválido: " + dto.tipo());
        }
        if (dto.dataRescisao().isBefore(contrato.getDataInicio())) {
            throw new IllegalArgumentException("A data da rescisão não pode ser anterior ao início do contrato");
        }

        ContratoRescisao rescisao = ContratoRescisao.builder()
                .tenantId(tenantId)
                .contratoId(contratoId)
                .tipo(dto.tipo())
                .motivo(dto.motivo())
                .dataRescisao(dto.dataRescisao())
                .criadoPor(criadoPor)
                .build();
        rescisaoRepository.save(rescisao);

        contrato.setStatus("RESCINDIDO");
        contrato.setDataFim(dto.dataRescisao());
        contratoRepository.save(contrato);
        return ContratoRescisaoResponseDTO.from(rescisao);
    }

    // ============================ AUXILIARES ============================

    private ContratoLicitacao buscarContratoDoTenant(UUID id, UUID tenantId) {
        return contratoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Contrato não encontrado"));
    }

    private void validarContratoNaoRescindido(ContratoLicitacao contrato) {
        if ("RESCINDIDO".equals(contrato.getStatus())) {
            throw new IllegalArgumentException("Contrato rescindido não aceita novas movimentações");
        }
    }

    private void validarTipoAditivo(AdicionarAditivoDTO dto) {
        if (!TIPOS_ADITIVO.contains(dto.tipo())) {
            throw new IllegalArgumentException("Tipo de aditivo inválido: " + dto.tipo());
        }
    }

    private ContratoExecucaoResponseDTO montarExecucao(ContratoLicitacao contrato, UUID tenantId) {
        List<ContratoAditivoResponseDTO> aditivos = aditivoRepository
                .findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contrato.getId(), tenantId).stream()
                .map(ContratoAditivoResponseDTO::from)
                .toList();
        List<ContratoApontamentoResponseDTO> apontamentos = apontamentoRepository
                .findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contrato.getId(), tenantId).stream()
                .map(ContratoApontamentoResponseDTO::from)
                .toList();
        List<ContratoMedicaoResponseDTO> medicoes = medicaoRepository
                .findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contrato.getId(), tenantId).stream()
                .map(ContratoMedicaoResponseDTO::from)
                .toList();
        List<ContratoSancaoResponseDTO> sancoes = sancaoRepository
                .findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contrato.getId(), tenantId).stream()
                .map(ContratoSancaoResponseDTO::from)
                .toList();
        ContratoRescisaoResponseDTO rescisao = rescisaoRepository
                .findByContratoIdAndTenantId(contrato.getId(), tenantId)
                .map(ContratoRescisaoResponseDTO::from)
                .orElse(null);

        LocalDate hoje = LocalDate.now();
        String situacao;
        long diasParaVencimento = ChronoUnit.DAYS.between(hoje, contrato.getDataFim());
        boolean atrasado = false;
        if ("RESCINDIDO".equals(contrato.getStatus())) {
            situacao = "RESCINDIDO";
        } else if (diasParaVencimento < 0) {
            situacao = "VENCIDO";
            atrasado = true;
        } else if (diasParaVencimento <= contrato.getVencimentoAvisoDias()) {
            situacao = "EXPIRANDO";
        } else {
            situacao = "VIGENTE";
        }

        return new ContratoExecucaoResponseDTO(
                contrato.getId(), contrato.getNumero(), contrato.getObjeto(),
                contrato.getDataInicio(), contrato.getDataFim(),
                contrato.getValorMensal(), contrato.getValorTotal(),
                contrato.getValorEmpenhado(), contrato.getValorLiquidado(),
                contrato.getEmpenhoNumero(), contrato.getStatus(), situacao,
                (int) diasParaVencimento, atrasado, contrato.getObservacoes(),
                contrato.getLicitacaoId(), aditivos, apontamentos, medicoes, sancoes, rescisao);
    }
}