package br.com.jess.chronos.pulse.modules.patrimonio.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_inventario_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"inventario_id", "patrimonio_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventarioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @Column(name = "patrimonio_id", nullable = false)
    private UUID patrimonioId;

    @Column(name = "patrimonio_tombamento", length = 30)
    private String patrimonioTombamento;

    @Column(name = "patrimonio_descricao", length = 255)
    private String patrimonioDescricao;

    @Builder.Default
    private Boolean conferido = false;

    @Column(name = "conferido_por", length = 120)
    private String conferidoPor;

    @Column(name = "data_conferencia")
    private OffsetDateTime dataConferencia;

    @Column(length = 20)
    private String resultado;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}