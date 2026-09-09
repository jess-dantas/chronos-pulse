package br.com.jess.chronos.pulse.modules.transparencia.web.dto;

import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.StatusPublicacaoTransparencia;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransparenciaResumoDTO(
        ContratosResumoDTO contratos,
        ComprasResumoDTO compras,
        LicitacoesResumoDTO licitacoes,
        EstoqueResumoDTO estoque,
        PatrimonioResumoDTO patrimonio,
        FrotaResumoDTO frota,
        ColaboradoresResumoDTO colaboradores,
        PontoResumoDTO ponto,
        PublicacoesResumoDTO publicacoes
) {

    public record ContratosResumoDTO(
            long ativos,
            BigDecimal valorEmpenhado,
            BigDecimal valorLiquidado,
            BigDecimal saldoTotal,
            long vencendo30Dias,
            long vencendo60Dias,
            long vencendo90Dias,
            long vencidos
    ) {
    }

    public record ComprasResumoDTO(
            long fornecedoresAtivos,
            long pedidosEmitidos,
            BigDecimal valorPedidos,
            long notasFiscaisRecebidas,
            BigDecimal valorNotasFiscais
    ) {
    }

    public record LicitacoesResumoDTO(
            long total,
            long emElaboracao,
            long publicadas,
            long abertas,
            long adjudicadas,
            long homologadas,
            long canceladas,
            BigDecimal valorEstimadoTotal
    ) {
    }

    public record EstoqueResumoDTO(
            long itensEstoque,
            BigDecimal valorTotalEstoque,
            long acimaDoMinimo,
            long abaixoDoMinimo
    ) {
    }

    public record PatrimonioResumoDTO(
            long totalBens,
            long bensAtivos,
            BigDecimal valorAquisicao,
            BigDecimal valorAtual
    ) {
    }

    public record FrotaResumoDTO(
            long veiculos,
            long veiculosAtivos,
            long abastecimentosMes,
            BigDecimal valorAbastecimentosMes
    ) {
    }

    public record ColaboradoresResumoDTO(
            long total,
            long ativos
    ) {
    }

    public record PontoResumoDTO(
            long registrosMes
    ) {
    }

    public record PublicacoesResumoDTO(
            long publicadas,
            String ultimaCompetencia
    ) {
    }
}