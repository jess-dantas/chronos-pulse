package br.com.jess.chronos.pulse.modules.ponto.domain.service;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class GeradorHashServiceTest {

    private RegistroPonto registro() {
        return new RegistroPonto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2024-01-15T08:00:00Z"),
                null, TipoRegistro.ENTRADA, new BigDecimal("-23.5505"), new BigDecimal("-46.6333"),
                new BigDecimal("5.0"), null, false, null);
    }

    @Test
    void deveGerarHashSHA256ComFormatoHexadecimal() {
        String hash = GeradorHashService.gerarHashRegistro(registro(), "12345678901");
        assertThat(hash).hasSize(64).matches("[a-f0-9]+");
    }

    @Test
    void deveGerarHashDeterministico() {
        RegistroPonto r = registro();
        assertThat(GeradorHashService.gerarHashRegistro(r, "12345678901"))
                .isEqualTo(GeradorHashService.gerarHashRegistro(r, "12345678901"));
    }

    @Test
    void deveGerarHashDiferenteParaCpfsDiferentes() {
        RegistroPonto r = registro();
        assertThat(GeradorHashService.gerarHashRegistro(r, "11111111111"))
                .isNotEqualTo(GeradorHashService.gerarHashRegistro(r, "22222222222"));
    }

    @Test
    void deveGerarHashQuandoTipoRegistroNulo() {
        RegistroPonto r = new RegistroPonto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                Instant.now(), null, null, BigDecimal.ZERO, BigDecimal.ZERO, null, null, false, null);
        assertThat(GeradorHashService.gerarHashRegistro(r, "12345678901")).hasSize(64);
    }
}
