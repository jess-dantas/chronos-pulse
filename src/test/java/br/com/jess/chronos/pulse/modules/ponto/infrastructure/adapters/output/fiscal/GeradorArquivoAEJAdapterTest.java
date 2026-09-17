package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
        assertThat(linhas).hasSize(4);
        assertThat(linhas[0])
                .startsWith("01|1|12345678000195||Empresa Teste|2024-01-01|2024-01-02|2024-01-31T18:00:00-0300|001");
    }

    @Test
    void deveGerarTrailerComQuantidadePorTipoDeRegistro() {
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of()));
        String[] linhas = conteudo.split("\\r?\\n");
        assertThat(linhas[1]).isEqualTo("08|||1|||");
        assertThat(linhas[2]).isEqualTo("99|1|0|0|0|0|0|0|1");
    }

    @Test
    void deveGerarRegistro08DeIdentificacaoDoPrtp() {
        GeradorArquivoAEJAdapter.GerarAEJ comPrtp = new GeradorArquivoAEJAdapter.GerarAEJ(
                "12345678000195", "Empresa Teste", null,
                dh(1, 8), dh(2, 18), Instant.parse("2024-01-31T18:00:00-03:00"),
                List.of(), List.of(),
                new GeradorArquivoAEJAdapter.AejPrtp("CHRONOS PULSE", "1.0.0", 1,
                        "46411071000130", "Jess Tecnologia", "contato@chronos.com.br"));
        String conteudo = adapter.gerarConteudoAEJ(comPrtp);
        assertThat(conteudo)
                .contains("08|CHRONOS PULSE|1.0.0|1|46411071000130|Jess Tecnologia|contato@chronos.com.br")
                .contains("99|1|0|0|0|0|0|0|1");
    }

    @Test
    void deveTerminarComLinhaDeAssinaturaDigital() {
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of()));
        String[] linhas = conteudo.split("\\r?\\n");
        assertThat(linhas[linhas.length - 1]).hasSize(100);
        assertThat(linhas[linhas.length - 1]).matches("ASSINATURA_DIGITAL_EM_ARQUIVO_P7S {67}");
    }

    @Test
    void deveGerarRegistro02DoRepPComNumeroInpi() {
        GeradorArquivoAEJAdapter.GerarAEJ comRep = new GeradorArquivoAEJAdapter.GerarAEJ(
                "12345678000195", "Empresa Teste", null,
                dh(1, 8), dh(2, 18), Instant.parse("2024-01-31T18:00:00-03:00"),
                List.of(), List.of(new GeradorArquivoAEJAdapter.AejRep(1, 3, "BR512019000001-7")));
        String conteudo = adapter.gerarConteudoAEJ(comRep);
        assertThat(conteudo).contains("02|1|3|BR512019000001-7");
    }

    @Test
    void deveGerarRegistro04DoHorarioContratual() {
        GeradorArquivoAEJAdapter.AejHorarioContratual horario =
                new GeradorArquivoAEJAdapter.AejHorarioContratual("1", 480,
                        List.of(new GeradorArquivoAEJAdapter.ParJornada("0800", "1200"),
                                new GeradorArquivoAEJAdapter.ParJornada("1300", "1800")));
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of(
                new GeradorArquivoAEJAdapter.AejVinculo(1, "123.456.789-01", "Maria Silva",
                        List.of(), horario, List.of()))));
        assertThat(conteudo).contains("04|1|480|0800|1200|1300|1800");
    }

    @Test
    void deveGerarRegistro07DeAusenciasEBancoDeHoras() {
        GeradorArquivoAEJAdapter.AejAusencia banco =
                new GeradorArquivoAEJAdapter.AejAusencia(3, LocalDate.parse("2024-01-10"), 120, 2);
        GeradorArquivoAEJAdapter.AejAusencia falta =
                new GeradorArquivoAEJAdapter.AejAusencia(2, LocalDate.parse("2024-01-12"), null, null);
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of(
                new GeradorArquivoAEJAdapter.AejVinculo(1, "123.456.789-01", "Maria Silva",
                        List.of(), null, List.of(banco, falta)))));

        assertThat(conteudo).contains("07|1|3|2024-01-10|120|2");
        assertThat(conteudo).contains("07|1|2|2024-01-12");
    }

    @Test
    void devePreencherCodigoDoHorarioNaPrimeiraEntrada() {
        GeradorArquivoAEJAdapter.AejHorarioContratual horario =
                new GeradorArquivoAEJAdapter.AejHorarioContratual("XPTO", 480,
                        List.of(new GeradorArquivoAEJAdapter.ParJornada("0800", "1800")));
        List<RegistroPonto> jornada = List.of(
                registro(TipoRegistro.ENTRADA, 1L, 15, 8),
                registro(TipoRegistro.ENTRADA, 2L, 15, 13),
                registro(TipoRegistro.SAIDA, 3L, 15, 18));
        String conteudo = adapter.gerarConteudoAEJ(dados(List.of(
                new GeradorArquivoAEJAdapter.AejVinculo(1, "12345678901", "Maria Silva",
                        jornada, horario, List.of()))));

        assertThat(conteudo)
                .contains("05|1|2024-01-15T08:00:00-0300||E|001|O|XPTO|")
                .contains("05|1|2024-01-15T13:00:00-0300||E|002|O||")
                .contains("05|1|2024-01-15T18:00:00-0300||S|003|O||");
    }

    @Test
    void deveReferenciarRepPNaMarcacao() {
        GeradorArquivoAEJAdapter.GerarAEJ comRep = new GeradorArquivoAEJAdapter.GerarAEJ(
                "12345678000195", "Empresa Teste", null,
                dh(1, 8), dh(2, 18), Instant.parse("2024-01-31T18:00:00-03:00"),
                List.of(new GeradorArquivoAEJAdapter.AejVinculo(1, "12345678901", "Maria Silva",
                        List.of(registro(TipoRegistro.ENTRADA, 1L, 15, 8)))),
                List.of(new GeradorArquivoAEJAdapter.AejRep(1, 3, "BR512019000001-7")));
        String conteudo = adapter.gerarConteudoAEJ(comRep);
        assertThat(conteudo).contains("05|1|2024-01-15T08:00:00-0300|1|E|001|O||");
    }

    @Test
    void deveGerarTrailerComContagemTotal() {
        GeradorArquivoAEJAdapter.AejHorarioContratual horario =
                new GeradorArquivoAEJAdapter.AejHorarioContratual("1", 480,
                        List.of(new GeradorArquivoAEJAdapter.ParJornada("0800", "1800")));
        GeradorArquivoAEJAdapter.GerarAEJ completo = new GeradorArquivoAEJAdapter.GerarAEJ(
                "12345678000195", "Empresa Teste", null,
                dh(1, 8), dh(2, 18), Instant.parse("2024-01-31T18:00:00-03:00"),
                List.of(new GeradorArquivoAEJAdapter.AejVinculo(1, "12345678901", "Maria Silva",
                        List.of(registro(TipoRegistro.ENTRADA, 1L, 15, 8),
                                registro(TipoRegistro.SAIDA, 2L, 15, 18)), horario,
                        List.of(new GeradorArquivoAEJAdapter.AejAusencia(2,
                                LocalDate.parse("2024-01-12"), null, null)))),
                List.of(new GeradorArquivoAEJAdapter.AejRep(1, 3, "BR512019000001-7")));
        String conteudo = adapter.gerarConteudoAEJ(completo);
        assertThat(conteudo).contains("99|1|1|1|1|2|0|1|1");
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