package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GeradorArquivoAEJAdapterTest {

    private final GeradorArquivoAEJAdapter adapter = new GeradorArquivoAEJAdapter();

    private Instant dh(int dia, int hora) {
        return Instant.parse(String.format("2024-01-%02dT%02d:00:00-03:00", dia, hora));
    }

    private RegistroPonto registro(TipoRegistro tipo, long nsr, int dia, int hora) {
        RegistroPonto r = new RegistroPonto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                dh(dia, hora), null, tipo,
                BigDecimal.ZERO, BigDecimal.ZERO, null, null, false, nsr);
        r.atribuirHash("abc123");
        return r;
    }

    private GeradorArquivoAEJAdapter.GerarAEJ dados(List<GeradorArquivoAEJAdapter.AejVinculo> vinculos) {
        return new GeradorArquivoAEJAdapter.GerarAEJ(
                "12345678000195", "Empresa Teste", null,
                dh(1, 8), dh(2, 18), Instant.parse("2024-01-31T18:00:00-03:00"),
                vinculos);
    }

    @Test
    void deveGerarCabecalhoComLayoutDoAnexoVi() {
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of()));
        String[] linhas = conteudo.split("\\r?\\n");
        assertThat(linhas).hasSize(1);
        assertThat(linhas[0])
                .startsWith("01|1|12345678000195||Empresa Teste|2024-01-01|2024-01-02|2024-01-31T18:00:00-0300|001");
    }

    @Test
    void deveGerarVinculoComCpfENome() {
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of(
                new GeradorArquivoAEJAdapter.AejVinculo(1, "123.456.789-01", "Maria Silva", List.of()))));
        assertThat(conteudo).contains("03|1|12345678901|Maria Silva");
    }

    @Test
    void deveGerarMarcacaoComTipoFonteESeq() {
        List<RegistroPonto> jornada = List.of(
                registro(TipoRegistro.ENTRADA, 1L, 15, 8),
                registro(TipoRegistro.SAIDA, 2L, 15, 18));
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of(
                new GeradorArquivoAEJAdapter.AejVinculo(1, "12345678901", "Maria Silva", jornada))));
        assertThat(conteudo)
                .contains("05|1|2024-01-15T08:00:00-0300||E|001|O||")
                .contains("05|1|2024-01-15T18:00:00-0300||S|002|O||");
    }

    @Test
    void deveMarcarInclusaoManualComoFonteIComMotivo() {
        RegistroPonto ajuste = registro(TipoRegistro.SAIDA, 2L, 15, 18);
        ajuste.atribuirAjusteManual(true, "Esqueceu de bater o ponto", null);
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of(
                new GeradorArquivoAEJAdapter.AejVinculo(1, "12345678901", "Maria Silva",
                        List.of(registro(TipoRegistro.ENTRADA, 1L, 15, 8), ajuste)))));
        assertThat(conteudo).contains("05|1|2024-01-15T18:00:00-0300||S|002|I||Esqueceu de bater o ponto");
    }

    @Test
    void naoDeveConterLinhasEmBranco() {
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of(
                new GeradorArquivoAEJAdapter.AejVinculo(1, "12345678901", "Maria Silva",
                        List.of(registro(TipoRegistro.ENTRADA, 1L, 15, 8))))));
        for (String linha : conteudo.split("\\r?\\n")) {
            assertThat(linha).isNotBlank();
        }
    }
}