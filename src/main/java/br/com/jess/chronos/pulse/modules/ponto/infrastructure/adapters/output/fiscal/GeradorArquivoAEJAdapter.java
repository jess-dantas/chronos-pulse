package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class GeradorArquivoAEJAdapter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy").withZone(ZoneId.of("UTC"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm").withZone(ZoneId.of("UTC"));

    public String gerarConteudoAEJ(String cnpjEmpresa, String razaoSocial, List<RegistroPonto> pontos, String cpfColaborador) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("1|%14s|%s\n", cnpjEmpresa, razaoSocial));

        for (RegistroPonto p : pontos) {
            String dataStr = DATE_FORMATTER.format(p.getDataHoraDispositivo());
            String horaStr = TIME_FORMATTER.format(p.getDataHoraDispositivo());

            sb.append(String.format("2|%d|%s|%s|%s|%s|%s\n",
                    p.getNsr(),
                    cpfColaborador,
                    dataStr,
                    horaStr,
                    p.getTipoRegistro().name(),
                    p.getHashIntegridade()
            ));
        }

        sb.append(String.format("9|TOTAL_REGISTROS=%d\n", pontos.size()));

        return sb.toString();
    }
}
