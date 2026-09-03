package br.com.jess.chronos.pulse.modules.estoque.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraPmpServiceTest {

    private CalculadoraPmpService calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraPmpService();
    }

    @Test
    @DisplayName("Deve definir o custo unitário da entrada como novo PMP quando o saldo inicial for zero")
    void deveDefinirNovoPmpQuandoSaldoZero() {
        BigDecimal qtdAtual = BigDecimal.ZERO;
        BigDecimal custoAtual = BigDecimal.ZERO;
        BigDecimal qtdEntrada = new BigDecimal("100.000");
        BigDecimal valorUnitarioEntrada = new BigDecimal("15.5000");

        BigDecimal novoPmp = calculadora.calcularNovoCustoMedio(qtdAtual, custoAtual, qtdEntrada, valorUnitarioEntrada);

        assertEquals(new BigDecimal("15.5000"), novoPmp);
    }

    @Test
    @DisplayName("Deve calcular o Custo Médio Ponderado corretamente com múltiplas aquisições a preços diferentes (MCASP)")
    void deveCalcularPmpCorretamenteComPrecosDiferentes() {
        // Cenário MCASP:
        // Saldo atual: 100 unidades a R$ 10,0000 = R$ 1.000,0000
        // Nova entrada: 50 unidades a R$ 16,0000 = R$ 800,0000
        // Total esperado: 150 unidades por R$ 1.800,0000 -> PMP = R$ 12,0000
        BigDecimal qtdAtual = new BigDecimal("100.000");
        BigDecimal custoAtual = new BigDecimal("10.0000");
        BigDecimal qtdEntrada = new BigDecimal("50.000");
        BigDecimal valorUnitarioEntrada = new BigDecimal("16.0000");

        BigDecimal novoPmp = calculadora.calcularNovoCustoMedio(qtdAtual, custoAtual, qtdEntrada, valorUnitarioEntrada);

        assertEquals(new BigDecimal("12.0000"), novoPmp);
    }

    @Test
    @DisplayName("Deve aplicar arredondamento HALF_UP para 4 casas decimais no cálculo do PMP")
    void deveArredondarPmpPara4CasasDecimais() {
        // 10 unidades a R$ 3,3300 = R$ 33,30
        // Nova entrada: 7 unidades a R$ 4,5500 = R$ 31,85
        // Total = 17 unidades por R$ 65,15 -> 65.15 / 17 = 3.8323529... -> 3.8324
        BigDecimal qtdAtual = new BigDecimal("10.000");
        BigDecimal custoAtual = new BigDecimal("3.3300");
        BigDecimal qtdEntrada = new BigDecimal("7.000");
        BigDecimal valorUnitarioEntrada = new BigDecimal("4.5500");

        BigDecimal novoPmp = calculadora.calcularNovoCustoMedio(qtdAtual, custoAtual, qtdEntrada, valorUnitarioEntrada);

        assertEquals(new BigDecimal("3.8324"), novoPmp);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a quantidade de entrada for zero ou negativa")
    void deveLancarExcecaoQuandoQtdEntradaInvalida() {
        assertThrows(IllegalArgumentException.class, () ->
                calculadora.calcularNovoCustoMedio(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN)
        );

        assertThrows(IllegalArgumentException.class, () ->
                calculadora.calcularNovoCustoMedio(BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("-5"), BigDecimal.TEN)
        );
    }

    @Test
    @DisplayName("Deve calcular o valor total financeiro da saída com base no PMP atual sem alterar o custo unitário")
    void deveCalcularValorTotalSaida() {
        BigDecimal qtdSaida = new BigDecimal("25.000");
        BigDecimal custoMedioAtual = new BigDecimal("12.5000");

        BigDecimal valorTotal = calculadora.calcularValorTotalSaida(qtdSaida, custoMedioAtual);

        assertEquals(new BigDecimal("312.5000"), valorTotal);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a quantidade de saída for menor ou igual a zero")
    void deveLancarExcecaoQuandoQtdSaidaInvalida() {
        assertThrows(IllegalArgumentException.class, () ->
                calculadora.calcularValorTotalSaida(BigDecimal.ZERO, BigDecimal.TEN)
        );
    }
}
