package br.com.jess.chronos.pulse.modules.ponto.domain.service;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GeradorCodigoVerificacaoEspelhoTest {

    private Instant inicio = Instant.parse("2026-09-01T00:00:00Z");
    private Instant fim = Instant.parse("2026-09-30T23:59:59Z");

    private RegistroPonto registro(Instant dataHora, Long nsr) {
        return new RegistroPonto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), dataHora,
                dataHora, TipoRegistro.ENTRADA, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "hash-" + nsr, false, nsr);
    }

    @Test
    void deveGerarCodigoDeVerificacaoCom64DigitosHexadecimal() {
        String codigo = GeradorCodigoVerificacaoEspelho.gerar("12345678000199", "12345678901",
                inicio, fim, List.of(registro(Instant.parse("2026-09-01T08:00:00Z"), 1L)));
        assertThat(codigo).hasSize(64).matches("[a-f0-9]{64}");
    }

    @Test
    void deveSerDeterministicoParaOMesmoConteudo() {
        List<RegistroPonto> marcacoes = List.of(
                registro(Instant.parse("2026-09-01T08:00:00Z"), 1L),
                registro(Instant.parse("2026-09-01T17:00:00Z"), 2L));
        String primeiro = GeradorCodigoVerificacaoEspelho.gerar("12345678000199", "12345678901", inicio, fim, marcacoes);
        String segundo = GeradorCodigoVerificacaoEspelho.gerar("12345678000199", "12345678901", inicio, fim, marcacoes);
        assertThat(primeiro).isEqualTo(segundo);
    }

    @Test
    void deveSerIndependenteDaOrdemDasMarcacoes() {
        RegistroPonto entrada = registro(Instant.parse("2026-09-01T08:00:00Z"), 1L);
        RegistroPonto saida = registro(Instant.parse("2026-09-01T17:00:00Z"), 2L);
        String primeira = GeradorCodigoVerificacaoEspelho.gerar("12345678000199", "12345678901",
                inicio, fim, List.of(entrada, saida));
        String invertida = GeradorCodigoVerificacaoEspelho.gerar("12345678000199", "12345678901",
                inicio, fim, List.of(saida, entrada));
        assertThat(invertida).isEqualTo(primeira);
    }

    @Test
    void deveMudarQuandoConteudoOuPeriodoMudar() {
        String base = GeradorCodigoVerificacaoEspelho.gerar("12345678000199", "12345678901",
                inicio, fim, List.of(registro(Instant.parse("2026-09-01T08:00:00Z"), 1L)));

        String outroCnpj = GeradorCodigoVerificacaoEspelho.gerar("99999999000191", "12345678901",
                inicio, fim, List.of(registro(Instant.parse("2026-09-01T08:00:00Z"), 1L)));
        String outrasMarcacoes = GeradorCodigoVerificacaoEspelho.gerar("12345678000199", "12345678901",
                inicio, fim, List.of(registro(Instant.parse("2026-09-02T08:00:00Z"), 1L)));

        assertThat(outroCnpj).isNotEqualTo(base);
        assertThat(outrasMarcacoes).isNotEqualTo(base);
    }

    @Test
    void deveSuportarEntradasVaziasOuNulas() {
        String codigo = GeradorCodigoVerificacaoEspelho.gerar(null, null, null, null, List.of());
        assertThat(codigo).hasSize(64).matches("[a-f0-9]{64}");
    }
}