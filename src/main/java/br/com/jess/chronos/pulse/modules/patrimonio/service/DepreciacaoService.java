package br.com.jess.chronos.pulse.modules.patrimonio.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class DepreciacaoService {

    private static final BigDecimal VALOR_RESIDUAL = new BigDecimal("0.10");

    public BigDecimal calcularTaxaMensal(BigDecimal valorAquisicao, Integer vidaUtilMeses) {
        if (valorAquisicao == null || vidaUtilMeses == null || vidaUtilMeses <= 0) {
            return BigDecimal.ZERO;
        }
        return valorAquisicao
                .multiply(BigDecimal.ONE.subtract(VALOR_RESIDUAL))
                .divide(BigDecimal.valueOf(vidaUtilMeses), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularDepreciado(BigDecimal valorAquisicao, Integer vidaUtilMeses, LocalDate inicio) {
        if (valorAquisicao == null || vidaUtilMeses == null || vidaUtilMeses <= 0 || inicio == null) {
            return BigDecimal.ZERO;
        }
        long meses = ChronoUnit.MONTHS.between(inicio, LocalDate.now());
        if (meses <= 0) {
            return BigDecimal.ZERO;
        }
        long mesesCap = Math.min(meses, vidaUtilMeses);
        BigDecimal depreciado = calcularTaxaMensal(valorAquisicao, vidaUtilMeses)
                .multiply(BigDecimal.valueOf(mesesCap));
        BigDecimal valorResidual = valorAquisicao.multiply(VALOR_RESIDUAL);
        BigDecimal teto = valorAquisicao.subtract(valorResidual);
        if (depreciado.compareTo(teto) > 0) {
            return teto;
        }
        return depreciado;
    }

    public BigDecimal calcularValorAtual(BigDecimal valorAquisicao, BigDecimal valorDepreciado) {
        if (valorAquisicao == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal depreciado = valorDepreciado != null ? valorDepreciado : BigDecimal.ZERO;
        return valorAquisicao.subtract(depreciado).max(BigDecimal.ZERO);
    }
}