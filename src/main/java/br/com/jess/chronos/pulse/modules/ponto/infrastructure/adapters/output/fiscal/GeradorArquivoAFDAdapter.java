package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;

/**
 * Gerador do Arquivo Fonte de Dados (AFD) no leiaute do Anexo V da Portaria MTP
 * n. 671/2021 para REP-P. Arquivo texto, registros por linha terminando em CRLF,
 * campos preenchidos da esquerda para a direita com espaços nas posições não
 * utilizadas, registros ordenados por NSR e sem linhas em branco. Registros dos
 * tipos "1" a "5" levam CRC-16/CCITT-TRUE; a marcação do tipo "7" (REP-P) leva
 * código hash SHA-256 encadeado ao registro anterior.
 */
@Component
public class GeradorArquivoAFDAdapter {

    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO_DH =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(FUSO_BRASIL);
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String VERSAO_LEIAUTE = "004";
    private static final String MODELO = "CHRONOS PULSE";
    private static final String COLETOR_APP_MOBILE = "01";

    /** DTO de entrada para a geração do AFD de um estabelecimento. */
    public record GerarAFD(
            String cnpjEmpresa,
            String razaoSocial,
            String cno,
            String numeroRegistroInpi,
            String cpfColaborador,
            String cnpjDesenvolvedor,
            Instant dataInicial,
            Instant dataFinal,
            Instant dataHoraGeracao,
            List<RegistroPonto> pontos) {

        public GerarAFD {
            pontos = pontos == null ? List.of() : pontos;
        }
    }

    public String gerarConteudoAFD(GerarAFD dados) {
        StringBuilder sb = new StringBuilder();

        sb.append(linhaCabecalho(dados)).append("\r\n");

        String hashAnterior = "";
        long indice = 0;
        for (RegistroPonto p : dados.pontos()) {
            String linha = linhaMarcacao(dados, p, hashAnterior);
            sb.append(linha).append("\r\n");
            hashAnterior = extrairHash(linha);
            indice++;
        }

        sb.append(linhaTrailer(dados.pontos().size())).append("\r\n");
        return sb.toString();
    }

    private String linhaCabecalho(GerarAFD dados) {
        StringBuilder linha = new StringBuilder();
        linha.append(campo("000000000", 9, '0'));
        linha.append("1");
        linha.append("1"); // tipo de identificador do empregador: 1 = CNPJ
        linha.append(espacos(cnpj(dados.cnpjEmpresa()), 14));
        linha.append(espacos(dados.cno() == null ? "" : dados.cno(), 14));
        linha.append(espacos(dados.razaoSocial() == null ? "" : dados.razaoSocial(), 150));
        linha.append(espacos(
                dados.numeroRegistroInpi() == null ? "" : dados.numeroRegistroInpi(), 17));
        linha.append(FORMATO_DATA.format(dados.dataInicial().atZone(FUSO_BRASIL)));
        linha.append(FORMATO_DATA.format(dados.dataFinal().atZone(FUSO_BRASIL)));
        linha.append(FORMATO_DH.format(dados.dataHoraGeracao()));
        linha.append(VERSAO_LEIAUTE);
        linha.append("1"); // tipo de identificador do desenvolvedor: 1 = CNPJ
        linha.append(espacos(cnpj(dados.cnpjDesenvolvedor()), 14));
        linha.append(espacos(MODELO, 30));
        linha.append("    "); // placeholder do CRC-16 (4)
        String semCrc = linha.substring(0, linha.length() - 4);
        String crc = Crc16Kermit.hex(Crc16Kermit.calcular(semCrc));
        linha.replace(linha.length() - 4, linha.length(), crc);
        return linha.toString();
    }

    private String linhaMarcacao(GerarAFD dados, RegistroPonto p, String hashAnterior) {
        String cpf = cpf12(dados.cpfColaborador());
        String dhMarcacao = FORMATO_DH.format(p.getDataHora());
        String dhGravacao = FORMATO_DH.format(
                p.getDataHoraServidor() != null ? p.getDataHoraServidor() : p.getDataHora());
        String online = Boolean.TRUE.equals(p.getSincronizadoOffline()) ? "1" : "0";

        String hash = hashEncadeado(dhMarcacao, cpf, dhGravacao, COLETOR_APP_MOBILE, online, hashAnterior);

        String nsr = p.getNsr() == null ? "0" : String.valueOf(p.getNsr());
        StringBuilder linha = new StringBuilder();
        linha.append(campo(nsr, 9, '0'));
        linha.append("7");
        linha.append(dhMarcacao);
        linha.append(cpf);
        linha.append(dhGravacao);
        linha.append(COLETOR_APP_MOBILE);
        linha.append(online);
        linha.append(hash);
        return linha.toString();
    }

    private String linhaTrailer(int quantidadeMarcacoes) {
        StringBuilder linha = new StringBuilder();
        linha.append(campo("999999999", 9, '9'));
        linha.append(campo("0", 9, '0')); // registros tipo "2"
        linha.append(campo("0", 9, '0')); // registros tipo "3"
        linha.append(campo("0", 9, '0')); // registros tipo "4"
        linha.append(campo("0", 9, '0')); // registros tipo "5"
        linha.append(campo("0", 9, '0')); // registros tipo "6"
        linha.append(campo(String.valueOf(quantidadeMarcacoes), 9, '0')); // registros tipo "7"
        linha.append("9");
        return linha.toString();
    }

    private static String hashEncadeado(String dhMarcacao, String cpf, String dhGravacao,
                                        String coletor, String online, String hashAnterior) {
        String base = dhMarcacao + cpf + dhGravacao + coletor + online + (hashAnterior == null ? "" : hashAnterior);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(base.getBytes(StandardCharsets.ISO_8859_1));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    /** Extrai as posições 74-137 da linha de marcação (o hash SHA-256). */
    private static String extrairHash(String linha) {
        if (linha == null || linha.length() < 137) {
            return "";
        }
        return linha.substring(73, 137);
    }

    private static String cpf12(String cpf) {
        String apenasDigitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        return campo(apenasDigitos.length() > 11 ? apenasDigitos.substring(0, 11) : apenasDigitos, 12, '0');
    }

    private static String cnpj(String cnpj) {
        String apenasDigitos = cnpj == null ? "" : cnpj.replaceAll("\\D", "");
        return apenasDigitos.length() > 14 ? apenasDigitos.substring(0, 14) : apenasDigitos;
    }

    private static String espacos(String valor, int largura) {
        return campo(valor, largura, ' ');
    }

    private static String campo(String valor, int largura, char preenchimento) {
        String v = valor == null ? "" : valor;
        if (v.length() > largura) {
            return v.substring(0, largura);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = v.length(); i < largura; i++) {
            sb.append(preenchimento);
        }
        sb.append(v);
        return sb.toString();
    }
}