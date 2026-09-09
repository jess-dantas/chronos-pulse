package br.com.jess.chronos.pulse.modules.telemetria.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "chronos.telemetria")
public class TelemetriaProperties {

    private boolean enabled = true;

    private int retentionDias = 30;

    private String appVersao = "1.0.0";

    private boolean requestLog = true;
}