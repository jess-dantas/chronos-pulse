package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GeradorArquivoAFDAdapterTest {

    private final GeradorArquivoAFDAdapter adapter = new GeradorArquivoAFDAdapter();

    private Instant dh(String valor) {
        return Instant.parse(valor);
    }

    private RegistroPonto registro(TipoRegistro tipo, long nsr, String dhMarcacao) {
        RegistroPonto r = new RegistroPonto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                dh(dhMarcacao), dh(dhMarcacao), tipo,
                BigDecimal.ZERO, BigDecimal.ZERO, null, null, false, nsr);
        r.atribuirHash("abc123");
        return r;
    }

    private GeradorArquivoAFDAdapter.GerarAFD dados(List<RegistroPonto> pontos) {
        return new GeradorArquivoAFDAdapter.GerarAFD(
                "12345678000195", "Empresa Teste", null, "BL20250115",
                "12345678901", "46411071000130",
                dh("2024-01-01T00:00:00-03:00"),
                dh("2024-01-31T23:59:59-03:00"),
                dh("2024-01-31T18:00:00-03:00"),
                pontos);
    }

    @Test
    void crc16KermitDeveGerarVetorDeReferenciaDoLeiaute() {
        assertThat(Crc16Kermit.hex(Crc16Kermit.calcular("123456789"))).isEqualTo("2189");
    }

    @Test
    void deveGerarCabecalhoComLayoutFixoDe302Posicoes() {
        String conteudo = adapter.gerarConteudoAFD(dados(List.of(registro(TipoRegistro.ENTRADA, 1L,
                "2024-01-15T08:00:00-03:00"))));
        String cabecalho = conteudo.split("\\r?\\n")[0];
        assertThat(cabecalho).hasSize(302);
        assertThat(cabecalho.substring(0, 9)).isEqualTo("000000000");
        assertThat(cabecalho.charAt(9)).isEqualTo('1');
        assertThat(cabecalho.substring(11, 25)).isEqualTo("12345678000195");
        assertThat(cabecalho.substring(39, 189).trim()).isEqualTo("Empresa Teste");
        assertThat(cabecalho.substring(189, 206).trim()).isEqualTo("BL20250115");
        assertThat(cabecalho.substring(206, 216)).isEqualTo("2024-01-01");
        assertThat(cabecalho.substring(216, 226)).isEqualTo("2024-01-31");
        assertThat(cabecalho.substring(250, 253)).isEqualTo("004");
        assertThat(cabecalho.substring(254, 268)).isEqualTo("46411071000130");
        assertThat(cabecalho.substring(268, 298).trim()).isEqualTo("CHRONOS PULSE");
        assertThat(cabecalho.substring(298, 302)).matches("[0-9A-F]{4}");
    }

    @Test
    void deveGerarMarcacaoRepPComHashSha256E137Posicoes() {
        String conteudo = adapter.gerarConteudoAFD(dados(List.of(registro(TipoRegistro.ENTRADA, 7L,
                "2024-01-15T08:00:00-03:00"))));
        String marcacao = conteudo.split("\\r?\\n")[1];
        assertThat(marcacao).hasSize(137);
        assertThat(marcacao.substring(0, 9)).isEqualTo("000000001");
        assertThat(marcacao.charAt(9)).isEqualTo('7');
        assertThat(marcacao.substring(10, 34)).isEqualTo("2024-01-15T08:00:00-0300");
        assertThat(marcacao.substring(34, 46)).isEqualTo("012345678901");
        assertThat(marcacao.substring(70, 72)).isEqualTo("01"); // coletor app mobile
        assertThat(marcacao.charAt(72)).isEqualTo('0'); // on-line
        assertThat(marcacao.substring(73, 137)).matches("[0-9a-f]{64}");
    }

    @Test
    void deveRenumerarNsrDeFormaSequencialPorEstabelecimento() {
        List<RegistroPonto> jornada = List.of(
                registro(TipoRegistro.SAIDA, 42L, "2024-01-15T18:00:00-03:00"),
                registro(TipoRegistro.ENTRADA, 7L, "2024-01-15T08:00:00-03:00"));
        String conteudo = adapter.gerarConteudoAFD(dados(jornada));
        String[] linhas = conteudo.split("\\r?\\n");
        assertThat(linhas[1].substring(0, 9)).isEqualTo("000000001"); // ENTRADA 08:00
        assertThat(linhas[2].substring(0, 9)).isEqualTo("000000002"); // SAIDA 18:00
    }

    @Test
    void deveEncadearHashEntreRegistros() {
        List<RegistroPonto> jornada = List.of(
                registro(TipoRegistro.ENTRADA, 1L, "2024-01-15T08:00:00-03:00"),
                registro(TipoRegistro.SAIDA, 2L, "2024-01-15T18:00:00-03:00"));
        String conteudo = adapter.gerarConteudoAFD(dados(jornada));
        String[] linhas = conteudo.split("\\r?\\n");
        String hash1 = linhas[1].substring(73, 137);
        String hash2 = linhas[2].substring(73, 137);
        assertThat(hash1).isNotEqualTo(hash2);

        GeradorArquivoAFDAdapter.GerarAFD soUm = dados(jornada.subList(0, 1));
        String hash1Sozinho = adapter.gerarConteudoAFD(soUm).split("\\r?\\n")[1].substring(73, 137);
        assertThat(hash1).isEqualTo(hash1Sozinho);

        RegistroPonto trocado = registro(TipoRegistro.ENTRADA, 1L,
                "2024-01-15T08:01:00-03:00");
        GeradorArquivoAFDAdapter.GerarAFD outro = dados(List.of(trocado, jornada.get(1)));
        String hashAlt = adapter.gerarConteudoAFD(outro).split("\\r?\\n")[1].substring(73, 137);
        assertThat(hashAlt).isNotEqualTo(hash1);
    }

    @Test
    void deveGerarTrailerComContagemNoLeiaute() {
        List<RegistroPonto> jornada = List.of(
                registro(TipoRegistro.ENTRADA, 1L, "2024-01-15T08:00:00-03:00"),
                registro(TipoRegistro.SAIDA, 2L, "2024-01-15T18:00:00-03:00"));
        String conteudo = adapter.gerarConteudoAFD(dados(jornada));
        String trailer = conteudo.split("\\r?\\n")[3];
        assertThat(trailer).hasSize(64);
        assertThat(trailer.substring(0, 9)).isEqualTo("999999999");
        assertThat(trailer.substring(54, 63)).isEqualTo("000000002");
        assertThat(trailer.charAt(63)).isEqualTo('9');
    }

    @Test
    void naoDeveConterLinhasEmBranco() {
        String conteudo = adapter.gerarConteudoAFD(dados(List.of(
                registro(TipoRegistro.ENTRADA, 1L, "2024-01-15T08:00:00-03:00"))));
        for (String linha : conteudo.split("\\r?\\n")) {
            assertThat(linha).isNotBlank();
        }
    }
}