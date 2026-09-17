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
 * linhas em branco. Registros: "01" cabeçalho do empregador, "02" REPs
 * utilizados, "03" vínculos, "04" horário contratual, "05" marcações tratadas,
 * "07" ausências e banco de horas e "99" trailer (raridade dos tipos).
 */
@Component
public class GeradorArquivoAEJAdapter {

    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DH =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(FUSO_BRASIL);

    /** Par entrada/saída de um horário contratual no formato "HHmm". */
    public record ParJornada(String entrada, String saida) {
    }

    /** Horário contratual (registro "04") de um vínculo. */
    public record AejHorarioContratual(String codHorario, int durJornadaMinutos, List<ParJornada> pares) {

        public AejHorarioContratual {
            pares = pares == null ? List.of() : pares;
        }
    }

    /** REP utilizado (registro "02"). Para REP-P, {@code nrRep} é o nº do INPI. */
    public record AejRep(int idRepAej, int tpRep, String nrRep) {
    }

    /**
     * Ausência ou banco de horas (registro "07"). Tipos: "1" DSR, "2" falta não
     * justificada, "3" movimento no banco de horas (exige {@code qtMinutos} e
     * {@code tipoMovBH}) e "4" folga compensatória de feriado.
     */
    public record AejAusencia(int tipoAusenOuComp, LocalDate data, Integer qtMinutos, Integer tipoMovBH) {
    }

    /**
     * Vínculo (empregado) dentro do AEJ: CPF, nome, numeração no arquivo,
     * horário contratual (registro "04") e ausências/banco de horas (registro "07").
     */
    public record AejVinculo(int idtVinculo, String cpf, String nome, List<RegistroPonto> registros,
                             AejHorarioContratual horarioContratual, List<AejAusencia> ausencias) {

        public AejVinculo {
            registros = registros == null ? List.of() : registros;
            ausencias = ausencias == null ? List.of() : ausencias;
        }

        public AejVinculo(int idtVinculo, String cpf, String nome, List<RegistroPonto> registros) {
            this(idtVinculo, cpf, nome, registros, null, List.of());
        }
    }

    /** Dados de geração do AEJ para um estabelecimento no período. */
    public record GerarAEJ(String cnpjEmpresa, String razaoSocial, String cno,
                           Instant dataInicial, Instant dataFinal, Instant dataHoraGeracao,
                           List<AejVinculo> vinculos, List<AejRep> reps) {

        public GerarAEJ {
            vinculos = vinculos == null ? List.of() : vinculos;
            reps = reps == null ? List.of() : reps;
        }

        public GerarAEJ(String cnpjEmpresa, String razaoSocial, String cno,
                        Instant dataInicial, Instant dataFinal, Instant dataHoraGeracao,
                        List<AejVinculo> vinculos) {
            this(cnpjEmpresa, razaoSocial, cno, dataInicial, dataFinal, dataHoraGeracao, vinculos, List.of());
        }
    }

    public String gerarConteudoAEJ(GerarAEJ dados) {
        StringBuilder sb = new StringBuilder();
        sb.append(cabecalho(dados)).append("\r\n");

        for (AejRep rep : dados.reps()) {
            sb.append(linhaRep(rep)).append("\r\n");
        }
        int qt02 = dados.reps().size();

        int qt03 = 0;
        int qt04 = 0;
        int qt05 = 0;
        int qt07 = 0;
        for (AejVinculo v : dados.vinculos()) {
            sb.append("03|").append(v.idtVinculo()).append("|").append(cpf(v.cpf()))
                    .append("|").append(v.nome()).append("\r\n");
            qt03++;

            if (v.horarioContratual() != null) {
                sb.append(linhaHorario(v.horarioContratual())).append("\r\n");
                qt04++;
            }

            for (RegistroPonto p : registrosOrdenados(v.registros())) {
                sb.append(marcacao(dados, v, p)).append("\r\n");
                qt05++;
            }

            for (AejAusencia a : v.ausencias()) {
                sb.append(linhaAusencia(v, a)).append("\r\n");
                qt07++;
            }
        }

        sb.append(trailer(qt02, qt03, qt04, qt05, qt07)).append("\r\n");
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

    private String linhaRep(AejRep rep) {
        return "02|" + rep.idRepAej() + "|" + rep.tpRep() + "|" + (rep.nrRep() == null ? "" : rep.nrRep());
    }

    private String linhaHorario(AejHorarioContratual h) {
        StringBuilder linha = new StringBuilder();
        linha.append("04|").append(h.codHorario()).append("|").append(h.durJornadaMinutos());
        for (ParJornada par : h.pares()) {
            linha.append("|").append(par.entrada()).append("|").append(par.saida());
        }
        return linha.toString();
    }

    private String marcacao(GerarAEJ dados, AejVinculo v, RegistroPonto p) {
        boolean ajuste = Boolean.TRUE.equals(p.getAjusteManual());
        String tipo = tipoMarcacao(p.getTipoRegistro());
        int seq = sequenciaEntradaSaida(v.registros(), p);
        String fonte = ajuste ? "I" : "O";
        String motivo = ajuste && p.getJustificativa() != null ? p.getJustificativa() : "";
        String idRepAej = "O".equals(fonte) ? idRepPParaMarcacao(dados) : "";
        String codHorContratual = v.horarioContratual() != null && "E".equals(tipo) && seq == 1
                ? v.horarioContratual().codHorario()
                : "";
        return "05"
                + "|" + v.idtVinculo()
                + "|" + DH.format(p.getDataHora())
                + "|" + idRepAej
                + "|" + tipo
                + "|" + String.format("%03d", seq)
                + "|" + fonte
                + "|" + codHorContratual
                + "|" + motivo;
    }

    private String linhaAusencia(AejVinculo v, AejAusencia a) {
        StringBuilder linha = new StringBuilder();
        linha.append("07|").append(v.idtVinculo())
                .append("|").append(a.tipoAusenOuComp())
                .append("|").append(a.data().format(DATA));
        if (a.tipoAusenOuComp() == 3) {
            linha.append("|").append(a.qtMinutos()).append("|").append(a.tipoMovBH());
        }
        return linha.toString();
    }

    private String trailer(int qt02, int qt03, int qt04, int qt05, int qt07) {
        return "99|1|" + qt02 + "|" + qt03 + "|" + qt04 + "|" + qt05 + "|0|" + qt07 + "|0";
    }

    /** Identificador do REP-P (registro "02") a ser referenciado nas marcações. */
    private static String idRepPParaMarcacao(GerarAEJ dados) {
        return dados.reps().stream()
                .filter(r -> r.tpRep() == 3)
                .map(r -> String.valueOf(r.idRepAej()))
                .findFirst()
                .orElse("");
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