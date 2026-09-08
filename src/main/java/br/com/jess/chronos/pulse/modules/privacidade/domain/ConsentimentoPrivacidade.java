package br.com.jess.chronos.pulse.modules.privacidade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_consentimento_privacidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentimentoPrivacidade {

    @Id
    private UUID id;

    @Column(name = "cpc_id", nullable = false)
    private UUID cpcId;

    @Column(name = "versao_politica", nullable = false, length = 50)
    private String versaoPolitica;

    @Column(name = "data_consentimento", nullable = false)
    private OffsetDateTime dataConsentimento;

    @Column(nullable = false)
    private boolean aceito;

    @Column(name = "ip_origem", length = 64)
    private String ipOrigem;
}