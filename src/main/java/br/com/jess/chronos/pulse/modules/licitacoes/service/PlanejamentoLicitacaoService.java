package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.*;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoEditalRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoEtpRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoTrRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanejamentoLicitacaoService {

    private final LicitacaoRepository licitacaoRepository;
    private final LicitacaoEtpRepository etpRepository;
    private final LicitacaoTrRepository trRepository;
    private final LicitacaoEditalRepository editalRepository;

    @Transactional(readOnly = true)
    public PlanejamentoLicitacaoResponseDTO buscarPlanejamento(UUID licitacaoId, UUID tenantId) {
        return montarPlanejamento(buscarLicitacao(licitacaoId, tenantId));
    }

    @Transactional
    public PlanejamentoLicitacaoResponseDTO salvarEtp(UUID licitacaoId, EtpDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoEmElaboracao(licitacaoId, tenantId);
        exigirTexto(dto.objeto(), "Objeto do ETP é obrigatório");
        exigirTexto(dto.justificativa(), "Justificativa do ETP é obrigatória");
        exigirTexto(dto.requisitos(), "Requisitos do ETP são obrigatórios");

        LicitacaoEtp etp = etpRepository.findByLicitacaoId(licitacaoId).orElse(null);
        if (etp != null && etp.getStatus() == EtpStatus.APROVADO) {
            throw new IllegalArgumentException("ETP já aprovado não pode ser alterado");
        }
        if (etp == null) {
            etp = LicitacaoEtp.builder()
                    .licitacao(licitacao)
                    .tenantId(tenantId)
                    .objeto(dto.objeto())
                    .justificativa(dto.justificativa())
                    .requisitos(dto.requisitos())
                    .alternativas(dto.alternativas())
                    .valorEstimado(dto.valorEstimado())
                    .riscos(dto.riscos())
                    .conclusao(dto.conclusao())
                    .status(EtpStatus.RASCUNHO)
                    .build();
        } else {
            etp.setObjeto(dto.objeto());
            etp.setJustificativa(dto.justificativa());
            etp.setRequisitos(dto.requisitos());
            etp.setAlternativas(dto.alternativas());
            etp.setValorEstimado(dto.valorEstimado());
            etp.setRiscos(dto.riscos());
            etp.setConclusao(dto.conclusao());
        }
        etpRepository.save(etp);
        return montarPlanejamento(licitacao);
    }

    @Transactional
    public PlanejamentoLicitacaoResponseDTO aprovarEtp(UUID licitacaoId, AprovacaoDocumentoDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoEmElaboracao(licitacaoId, tenantId);
        LicitacaoEtp etp = etpRepository.findByLicitacaoId(licitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Elabore o ETP antes de aprová-lo"));
        if (etp.getStatus() == EtpStatus.APROVADO) {
            throw new IllegalArgumentException("ETP já aprovado");
        }
        String responsavel = dto.responsavel();
        exigirTexto(responsavel, "Responsável pela aprovação é obrigatório");
        etp.setResponsavel(responsavel);
        etp.setStatus(EtpStatus.APROVADO);
        etp.setDataAprovacao(Instant.now());
        etpRepository.save(etp);
        return montarPlanejamento(licitacao);
    }

    @Transactional
    public PlanejamentoLicitacaoResponseDTO salvarTr(UUID licitacaoId, TrDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoEmElaboracao(licitacaoId, tenantId);
        LicitacaoEtp etp = buscarEtpAprovado(licitacaoId);
        exigirTexto(dto.especificacoes(), "Especificações do objeto são obrigatórias");
        exigirTexto(dto.condicoesFornecimento(), "Condições de fornecimento são obrigatórias");
        exigirTexto(dto.obrigacoes(), "Obrigações do fornecedor são obrigatórias");
        exigirTexto(dto.criteriosAceitacao(), "Critérios de aceitação são obrigatórios");
        exigirTexto(dto.prazosEntrega(), "Prazos de entrega são obrigatórios");

        LicitacaoTr tr = trRepository.findByLicitacaoId(licitacaoId).orElse(null);
        if (tr != null && tr.getStatus() == TrStatus.APROVADO) {
            throw new IllegalArgumentException("Termo de Referência já aprovado não pode ser alterado");
        }
        if (tr == null) {
            tr = LicitacaoTr.builder()
                    .licitacao(licitacao)
                    .etp(etp)
                    .tenantId(tenantId)
                    .especificacoes(dto.especificacoes())
                    .condicoesFornecimento(dto.condicoesFornecimento())
                    .obrigacoes(dto.obrigacoes())
                    .criteriosAceitacao(dto.criteriosAceitacao())
                    .prazosEntrega(dto.prazosEntrega())
                    .garantia(dto.garantia())
                    .formaPagamento(dto.formaPagamento())
                    .status(TrStatus.RASCUNHO)
                    .build();
        } else {
            tr.setEspecificacoes(dto.especificacoes());
            tr.setCondicoesFornecimento(dto.condicoesFornecimento());
            tr.setObrigacoes(dto.obrigacoes());
            tr.setCriteriosAceitacao(dto.criteriosAceitacao());
            tr.setPrazosEntrega(dto.prazosEntrega());
            tr.setGarantia(dto.garantia());
            tr.setFormaPagamento(dto.formaPagamento());
        }
        trRepository.save(tr);
        return montarPlanejamento(licitacao);
    }

    @Transactional
    public PlanejamentoLicitacaoResponseDTO aprovarTr(UUID licitacaoId, AprovacaoDocumentoDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoEmElaboracao(licitacaoId, tenantId);
        LicitacaoTr tr = trRepository.findByLicitacaoId(licitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Elabore o Termo de Referência antes de aprová-lo"));
        if (tr.getStatus() == TrStatus.APROVADO) {
            throw new IllegalArgumentException("Termo de Referência já aprovado");
        }
        String responsavel = dto.responsavel();
        exigirTexto(responsavel, "Responsável pela aprovação é obrigatório");
        tr.setResponsavel(responsavel);
        tr.setStatus(TrStatus.APROVADO);
        tr.setDataAprovacao(Instant.now());
        trRepository.save(tr);
        return montarPlanejamento(licitacao);
    }

    @Transactional
    public PlanejamentoLicitacaoResponseDTO salvarEdital(UUID licitacaoId, EditalDTO dto, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoEmElaboracao(licitacaoId, tenantId);
        LicitacaoTr tr = buscarTrAprovado(licitacaoId);
        validarFormaEntrega(dto.formaEntregaPropostas());
        if (dto.dataAberturaSessao() == null && dto.horarioAbertura() != null) {
            throw new IllegalArgumentException("Informe a data da sessão ao informar o horário de abertura");
        }

        LicitacaoEdital edital = editalRepository.findByLicitacaoId(licitacaoId).orElse(null);
        if (edital != null && edital.getStatus() == EditalStatus.PUBLICADO) {
            throw new IllegalArgumentException("Edital já publicado não pode ser alterado");
        }
        if (edital == null) {
            edital = LicitacaoEdital.builder()
                    .licitacao(licitacao)
                    .tr(tr)
                    .tenantId(tenantId)
                    .numeroProcesso(dto.numeroProcesso())
                    .numeroEdital(dto.numeroEdital())
                    .localSessao(dto.localSessao())
                    .dataAberturaSessao(dto.dataAberturaSessao())
                    .horarioAbertura(dto.horarioAbertura())
                    .formaEntregaPropostas(dto.formaEntregaPropostas())
                    .anexos(dto.anexos())
                    .observacoes(dto.observacoes())
                    .status(EditalStatus.EM_ELABORACAO)
                    .build();
        } else {
            edital.setNumeroProcesso(dto.numeroProcesso());
            edital.setNumeroEdital(dto.numeroEdital());
            edital.setLocalSessao(dto.localSessao());
            edital.setDataAberturaSessao(dto.dataAberturaSessao());
            edital.setHorarioAbertura(dto.horarioAbertura());
            edital.setFormaEntregaPropostas(dto.formaEntregaPropostas());
            edital.setAnexos(dto.anexos());
            edital.setObservacoes(dto.observacoes());
        }
        editalRepository.save(edital);
        return montarPlanejamento(licitacao);
    }

    @Transactional
    public PlanejamentoLicitacaoResponseDTO publicarEdital(UUID licitacaoId, UUID tenantId) {
        Licitacao licitacao = buscarLicitacaoEmElaboracao(licitacaoId, tenantId);
        LicitacaoEdital edital = editalRepository.findByLicitacaoId(licitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Elabore o edital antes de publicá-lo"));
        if (edital.getStatus() == EditalStatus.PUBLICADO) {
            throw new IllegalArgumentException("Edital já publicado");
        }
        edital.setStatus(EditalStatus.PUBLICADO);
        edital.setDataPublicacao(Instant.now());
        editalRepository.save(edital);
        return montarPlanejamento(licitacao);
    }

    // ============================ AUXILIARES ============================

    private Licitacao buscarLicitacao(UUID licitacaoId, UUID tenantId) {
        return licitacaoRepository.findByIdAndTenantId(licitacaoId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Licitação não encontrada"));
    }

    private Licitacao buscarLicitacaoEmElaboracao(UUID licitacaoId, UUID tenantId) {
        Licitacao licitacao = buscarLicitacao(licitacaoId, tenantId);
        if (licitacao.getStatus() != LicitacaoStatus.EM_ELABORACAO) {
            throw new IllegalArgumentException("Planejamento disponível somente para licitações em elaboração");
        }
        return licitacao;
    }

    private LicitacaoEtp buscarEtpAprovado(UUID licitacaoId) {
        LicitacaoEtp etp = etpRepository.findByLicitacaoId(licitacaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aprove o ETP antes de elaborar o Termo de Referência"));
        if (etp.getStatus() != EtpStatus.APROVADO) {
            throw new IllegalArgumentException("Aprove o ETP antes de elaborar o Termo de Referência");
        }
        return etp;
    }

    private LicitacaoTr buscarTrAprovado(UUID licitacaoId) {
        LicitacaoTr tr = trRepository.findByLicitacaoId(licitacaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aprove o Termo de Referência antes de elaborar o edital"));
        if (tr.getStatus() != TrStatus.APROVADO) {
            throw new IllegalArgumentException("Aprove o Termo de Referência antes de elaborar o edital");
        }
        return tr;
    }

    private void validarFormaEntrega(String forma) {
        if (forma == null || forma.isBlank()) {
            return;
        }
        if (!forma.equals("PRESENCIAL") && !forma.equals("ELETRONICA")) {
            throw new IllegalArgumentException("Forma de entrega de propostas inválida: " + forma);
        }
    }

    private void exigirTexto(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    private PlanejamentoLicitacaoResponseDTO montarPlanejamento(Licitacao licitacao) {
        LicitacaoEtp etp = etpRepository.findByLicitacaoId(licitacao.getId()).orElse(null);
        LicitacaoTr tr = trRepository.findByLicitacaoId(licitacao.getId()).orElse(null);
        LicitacaoEdital edital = editalRepository.findByLicitacaoId(licitacao.getId()).orElse(null);
        return new PlanejamentoLicitacaoResponseDTO(
                licitacao.getId(),
                licitacao.getNumero(),
                licitacao.getStatus().name(),
                EtpResponseDTO.from(etp),
                TrResponseDTO.from(tr),
                EditalResponseDTO.from(edital));
    }
}