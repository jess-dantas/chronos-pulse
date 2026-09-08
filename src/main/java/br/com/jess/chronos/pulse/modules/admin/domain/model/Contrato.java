package br.com.jess.chronos.pulse.modules.admin.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class Contrato {

    private final UUID id;
    private final UUID tenantId;
    private String numero;
    private String objeto;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private BigDecimal valorMensal;
    private BigDecimal valorTotal;
    private String status;
    private String observacoes;
    private BigDecimal valorEmpenhado;
    private BigDecimal valorLiquidado;
    private String empenhoNumero;
    private int vencimentoAvisoDias;
    private final Instant criadoEm;
    private Instant atualizadoEm;

    public Contrato(UUID id, UUID tenantId, String numero, String objeto,
                    LocalDate dataInicio, LocalDate dataFim,
                    BigDecimal valorMensal, BigDecimal valorTotal, String status) {
        this(id, tenantId, numero, objeto, dataInicio, dataFim, valorMensal, valorTotal, status, null);
    }

    public Contrato(UUID id, UUID tenantId, String numero, String objeto,
                    LocalDate dataInicio, LocalDate dataFim,
                    BigDecimal valorMensal, BigDecimal valorTotal,
                    String status, String observacoes) {
        this(id, tenantId, numero, objeto, dataInicio, dataFim, valorMensal, valorTotal,
                status, observacoes, BigDecimal.ZERO, BigDecimal.ZERO, null, 30);
    }

    public Contrato(UUID id, UUID tenantId, String numero, String objeto,
                    LocalDate dataInicio, LocalDate dataFim,
                    BigDecimal valorMensal, BigDecimal valorTotal,
                    String status, String observacoes,
                    BigDecimal valorEmpenhado, BigDecimal valorLiquidado,
                    String empenhoNumero, Integer vencimentoAvisoDias) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.numero = numero;
        this.objeto = objeto;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.valorMensal = valorMensal;
        this.valorTotal = valorTotal;
        this.status = status != null ? status : "ATIVO";
        this.observacoes = observacoes;
        this.valorEmpenhado = valorEmpenhado != null ? valorEmpenhado : BigDecimal.ZERO;
        this.valorLiquidado = valorLiquidado != null ? valorLiquidado : BigDecimal.ZERO;
        this.empenhoNumero = empenhoNumero;
        this.vencimentoAvisoDias = vencimentoAvisoDias != null ? vencimentoAvisoDias : 30;
        this.criadoEm = Instant.now();
        this.atualizadoEm = Instant.now();
    }

    public void atualizar(String numero, String objeto, LocalDate dataInicio, LocalDate dataFim,
                          BigDecimal valorMensal, BigDecimal valorTotal, String status, String observacoes) {
        if (numero != null) this.numero = numero;
        if (objeto != null) this.objeto = objeto;
        if (dataInicio != null) this.dataInicio = dataInicio;
        if (dataFim != null) this.dataFim = dataFim;
        if (valorMensal != null) this.valorMensal = valorMensal;
        if (valorTotal != null) this.valorTotal = valorTotal;
        if (status != null) this.status = status;
        this.observacoes = observacoes;
        this.atualizadoEm = Instant.now();
    }

    public void atualizarSaldo(BigDecimal valorEmpenhado, BigDecimal valorLiquidado,
                               String empenhoNumero, Integer vencimentoAvisoDias) {
        if (valorEmpenhado != null) this.valorEmpenhado = valorEmpenhado;
        if (valorLiquidado != null) this.valorLiquidado = valorLiquidado;
        if (empenhoNumero != null) this.empenhoNumero = empenhoNumero;
        if (vencimentoAvisoDias != null) this.vencimentoAvisoDias = vencimentoAvisoDias;
        this.atualizadoEm = Instant.now();
    }

    public BigDecimal getSaldo() {
        BigDecimal saldo = valorEmpenhado.subtract(valorLiquidado);
        return saldo.max(BigDecimal.ZERO);
    }

    public long getDiasParaVencimento() {
        if (dataFim == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dataFim);
    }

    public String getStatusVigencia() {
        if (dataFim == null) return "VIGENTE";
        long dias = getDiasParaVencimento();
        if (dias < 0) return "VENCIDO";
        if (dias <= vencimentoAvisoDias) return "VENCENDO";
        return "VIGENTE";
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getNumero() { return numero; }
    public String getObjeto() { return objeto; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public BigDecimal getValorMensal() { return valorMensal; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public String getStatus() { return status; }
    public String getObservacoes() { return observacoes; }
    public BigDecimal getValorEmpenhado() { return valorEmpenhado; }
    public BigDecimal getValorLiquidado() { return valorLiquidado; }
    public String getEmpenhoNumero() { return empenhoNumero; }
    public int getVencimentoAvisoDias() { return vencimentoAvisoDias; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
