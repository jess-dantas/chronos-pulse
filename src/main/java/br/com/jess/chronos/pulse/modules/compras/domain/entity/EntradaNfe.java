package br.com.jess.chronos.pulse.modules.compras.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_entrada_nfe")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntradaNfe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "chave_nfe", nullable = false, length = 44)
    private String chaveNfe;

    @Column(name = "numero_nfe", length = 9)
    private String numeroNfe;

    @Column(length = 3)
    private String serie;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "valor_nota", precision = 15, scale = 2)
    private BigDecimal valorNota;

    @Column(name = "fornecedor_id")
    private UUID fornecedorId;

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(name = "contrato_id")
    private UUID contratoId;

    @Column(name = "empenho_numero", length = 30)
    private String empenhoNumero;

    @Column(name = "almoxarifado_id", nullable = false)
    private UUID almoxarifadoId;

    @Column(name = "tipo_termo", nullable = false, length = 20)
    private String tipoTermo;

    @Column(name = "numero_termo", length = 30)
    private String numeroTermo;

    @Column(name = "cnpj_emitente", length = 14)
    private String cnpjEmitente;

    @Column(name = "razao_emitente", length = 160)
    private String razaoEmitente;

    @Column(name = "xml_nfe", columnDefinition = "TEXT")
    private String xmlNfe;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;
}