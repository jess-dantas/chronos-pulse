package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gerador do Arquivo Eletrônico de Jornada (AEJ) no leiaute do Anexo VI da
 * Portaria MTP n. 671/2021. Arquivo texto ASCII (ISO-8859-1), registros por
 * linha terminando em CRLF, campos separados por "|" (exceto o último), sem
 * linhas em branco. Registros: "01" cabeçalho do empregador, "03" vínculos,
 * "04" horário contratual (quando informado) e "05" marcações tratadas.
 */
@Component
public class GeradorArquivoAEJAdapter {

    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter DH =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(FUSO_BRASIL);

    /** Vínculo (empregado) dentro do AEJ: CPF, nome e numeração no arquivo. */
    public record AejVinculo(int idtVinculo, String cpf, String nome,
                             List<RegistroPonto> registros) {
    }

    /** Dados de geração do AEJ para um estabelecimento no período. */
    public record GerarAEJ(String cnpjEmpresa, String razaoSocial, String cno,
                           Instant dataInicial, Instant dataFinal, Instant dataHoraGeracao,
                           List<AejVinculo> vinculos) {

        public GerarAEJ {
            vinculos = vinculos == null ? List.of() : vinculos;
        }
    }

    public String gerarConteudoAEJ(GerarAEJ dados) {
        StringBuilder sb = new StringBuilder();
        sb.append(cabecalho(dados)).append("\r\n");

        for (AejVinculo v : dados.vinculos()) {
            sb.append("03|").append(v.idtVinculo()).append("|").append(cpf(v.cpf())).append("|").append(v.nome()).append("\r\n");
            for (RegistroPonto p : registrosOrdenados(v.registros())) {
                sb.append(marcacao(dados, v, p)).append("\r\n");
            }
        }
        return sb.toString();
    }

    private String cabecalho(GerarAEJ dados) {
        return "01|1"
                + "|" + apenasDigitos(dados.cnpjEmpresa(), 14)
                + "|" + (dados.cno() == null ? "" : dados.cno())
                + "|" + dados.razaoSocial()
                + "|" + DATA.format(dados.dataInicial().atZone(FUSO_BRASIL))
                + "|" + DATA.format(dados.dataFinal().atZone(FUSO_BRASIL))
                + "|" + DH.format(dados.dataHoraGeracao())
                + "|001";
    }

    private String marcacao(GerarAEJ dados, AejVinculo v, RegistroPonto p) {
        boolean ajuste = Boolean.TRUE.equals(p.getAjusteManual());
        String tipo = tipoMarcacao(p.getTipoRegistro());
        int seq = sequenciaEntradaSaida(v.registros(), p);
        String fonte = ajuste ? "I" : "O";
        String motivo = ajuste && p.getJustificativa() != null ? p.getJustificativa() : "";
        return "05"
                + "|" + v.idtVinculo()
                + "|" + DH.format(p.getDataHora())
                + "|" // idRepAej (sem registro de REP individualizado)
                + "|" + tipo
                + "|" + String.format("%03d", seq)
                + "|" + fonte
                + "|" + ""
                + "|" + motivo;
    }

    private static String tipoMarcacao(TipoRegistro tipo) {
        if (tipo == null) {
            return "E";
        }
        return switch (tipo) {
            case SAIDA, INTERVALO -> "S";
            case ENTRADA, RETORNO -> "E";
        };
    }

    private static int sequenciaEntradaSaida(List<RegistroPonto> registros, RegistroPonto atual) {
        LocalDate dia = atual.getDataHora().atZone(FUSO_BRASIL).toLocalDate();
        int seq = 0;
        for (RegistroPonto r : registrosOrdenados(registros)) {
            boolean mesmoDia = r.getDataHora() != null
                    && r.getDataHora().atZone(FUSO_BRASIL).toLocalDate().equals(dia);
            if (!mesmoDia) {
                continue;
            }
            seq++;
            if (r == atual) {
                return seq;
            }
        }
        return seq;
    }

    private static List<RegistroPonto> registrosOrdenados(List<RegistroPonto> registros) {
        return registros.stream()
                .sorted((a, b) -> a.getDataHora().compareTo(b.getDataHora()))
                .toList();
    }

    private static String cpf(String cpf) {
        return apenasDigitos(cpf, 11);
    }

    private static String apenasDigitos(String valor, int max) {
        if (valor == null) {
            return "";
        }
        String apenas = valor.replaceAll("\\D", "");
        return apenas.length() > max ? apenas.substring(0, max) : apenas;
    }
}