package br.com.jess.chronos.pulse.modules.estoque.domain.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Serviço de domínio responsável pelo cálculo do Custo Médio Ponderado (PMP)
 * em conformidade com as normas do MCASP (Manual de Contabilidade Aplicada ao Setor Público) / STN.
 */
@Service
public class CalculadoraPmpService {

    public static final int CASAS_DECIMAIS_VALOR = 4;
    public static final int CASAS_DECIMAIS_QUANTIDADE = 3;

    /**
     * Calcula o novo Custo Médio Ponderado Unitário após uma entrada de material no estoque.
     *
     * Fórmula:
     * Novo PMP = ((Qtd Atual * Custo Médio Atual) + (Qtd Entrada * Valor Unitário Entrada)) / (Qtd Atual + Qtd Entrada)
     *
     * @param qtdAtual Quantidade física atual no saldo
     * @param custoMedioAtual Custo médio unitário atual no saldo
     * @param qtdEntrada Quantidade que está dando entrada
     * @param valorUnitarioEntrada Valor unitário de aquisição/empenho da entrada
     * @return Novo custo médio ponderado unitário com 4 casas decimais
     */
    public BigDecimal calcularNovoCustoMedio(BigDecimal qtdAtual,
                                             BigDecimal custoMedioAtual,
                                             BigDecimal qtdEntrada,
                                             BigDecimal valorUnitarioEntrada) {
        if (qtdEntrada == null || qtdEntrada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade de entrada deve ser maior que zero.");
        }
        if (valorUnitarioEntrada == null || valorUnitarioEntrada.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor unitário de entrada não pode ser negativo.");
        }

        BigDecimal safeQtdAtual = (qtdAtual != null && qtdAtual.compareTo(BigDecimal.ZERO) > 0)
                ? qtdAtual : BigDecimal.ZERO;
        BigDecimal safeCustoAtual = (custoMedioAtual != null && custoMedioAtual.compareTo(BigDecimal.ZERO) > 0)
                ? custoMedioAtual : BigDecimal.ZERO;

        // Se o estoque atual for zero, o novo custo médio é o próprio valor unitário de entrada
        if (safeQtdAtual.compareTo(BigDecimal.ZERO) == 0) {
            return valorUnitarioEntrada.setScale(CASAS_DECIMAIS_VALOR, RoundingMode.HALF_UP);
        }

        BigDecimal valorTotalAtual = safeQtdAtual.multiply(safeCustoAtual);
        BigDecimal valorTotalEntrada = qtdEntrada.multiply(valorUnitarioEntrada);

        BigDecimal novaQtdTotal = safeQtdAtual.add(qtdEntrada);
        BigDecimal novoValorTotal = valorTotalAtual.add(valorTotalEntrada);

        return novoValorTotal.divide(novaQtdTotal, CASAS_DECIMAIS_VALOR, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o valor total financeiro da saída de material com base no Custo Médio Ponderado atual.
     * Na contabilidade pública, as saídas não alteram o custo médio unitário do item em estoque.
     *
     * @param qtdSaida Quantidade a ser baixada
     * @param custoMedioAtual Custo médio unitário atual do item
     * @return Valor total financeiro da saída
     */
    public BigDecimal calcularValorTotalSaida(BigDecimal qtdSaida, BigDecimal custoMedioAtual) {
        if (qtdSaida == null || qtdSaida.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade de saída deve ser maior que zero.");
        }
        if (custoMedioAtual == null || custoMedioAtual.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O custo médio atual não pode ser negativo.");
        }

        return qtdSaida.multiply(custoMedioAtual).setScale(CASAS_DECIMAIS_VALOR, RoundingMode.HALF_UP);
    }
}
