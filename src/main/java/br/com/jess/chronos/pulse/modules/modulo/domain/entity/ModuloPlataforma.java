package br.com.jess.chronos.pulse.modules.modulo.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "modulo_plataforma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuloPlataforma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Builder.Default
    private Boolean ativo = true;
}