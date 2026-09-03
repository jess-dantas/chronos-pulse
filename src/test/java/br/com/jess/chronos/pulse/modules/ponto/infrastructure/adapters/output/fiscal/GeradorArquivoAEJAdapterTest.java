package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class GeradorArquivoAEJAdapterTest {

    private final GeradorArquivoAEJAdapter adapter = new GeradorArquivoAEJAdapter();

    private RegistroPonto registro(TipoRegistro tipo, long nsr) {
        RegistroPonto r = new RegistroPonto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                Instant.parse("2024-01-15T08:00:00Z"), null, tipo,
                BigDecimal.ZERO, BigDecimal.ZERO, null, null, false, nsr);
        r.atribuirHash("abc123");
        return r;
    }

    @Test
    void deveGerarCabecalhoComCnpjERazaoSocial() {
        String conteudo = adapter.gerarConteudoAEJ("12345678000195", "Empresa Teste", Collections.emptyList(), "12345678901");
        assertThat(conteudo).startsWith("1|12345678000195|Empresa Teste");
    }

    @Test
    void deveGerarRodapeComTotalDeRegistros() {
        String conteudo = adapter.gerarConteudoAEJ("12345678000195", "Empresa", List.of(registro(TipoRegistro.ENTRADA, 1L)), "12345678901");
        assertThat(conteudo).contains("9|TOTAL_REGISTROS=1");
    }

    @Test
    void deveGerarLinhaDeRegistroComCamposCorretos() {
        String conteudo = adapter.gerarConteudoAEJ("12345678000195", "Empresa", List.of(registro(TipoRegistro.ENTRADA, 1L)), "12345678901");
        assertThat(conteudo).contains("2|1|12345678901|15012024|0800|ENTRADA|abc123");
    }

    @Test
    void deveGerarJornadaCompletaComQuatroBatidas() {
        List<RegistroPonto> jornada = List.of(
                registro(TipoRegistro.ENTRADA, 1L),
                registro(TipoRegistro.INTERVALO, 2L),
                registro(TipoRegistro.RETORNO, 3L),
                registro(TipoRegistro.SAIDA, 4L)
        );
        String conteudo = adapter.gerarConteudoAEJ("12345678000195", "Empresa", jornada, "12345678901");
        assertThat(conteudo).contains("ENTRADA", "INTERVALO", "RETORNO", "SAIDA");
        assertThat(conteudo).contains("9|TOTAL_REGISTROS=4");
    }

    @Test
    void deveGerarArquivoVazioComApenasHeaderEFooter() {
        String conteudo = adapter.gerarConteudoAEJ("12345678000195", "Empresa", Collections.emptyList(), "12345678901");
        assertThat(conteudo.lines().count()).isEqualTo(2);
    }
}
