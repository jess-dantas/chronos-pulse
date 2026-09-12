package br.com.jess.chronos.pulse.modules.licitacoes.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ContratoExecucaoResponseDTO(
        UUID id,
        String numero,
        String objeto,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal valorMensal,
        BigDecimal valorTotal,
        BigDecimal valorEmpenhado,
        BigDecimal valorLiquidado,
        String empenhoNumero,
        String status,
        String situacao,
        Integer diasParaVencimento,
        boolean atrasado,
        String observacoes,
        UUID licitacaoId,
        List<ContratoAditivoResponseDTO> aditivos,
        List<ContratoApontamentoResponseDTO> apontamentos,
        List<ContratoMedicaoResponseDTO> medicoes,
        List<ContratoSancaoResponseDTO> sancoes,
        ContratoRescisaoResponseDTO rescisao
) {}