package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.ponto.domain.ports.input.ConsultarRelatorioEspelhoPontoUseCase;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record RelatorioEspelhoPontoDTO(
        Periodo periodo,
        Instant dataEmissao,
        Empregador empregador,
        Trabalhador trabalhador,
        JornadaContratual jornadaContratual,
        List<EspelhoPontoItemDTO> marcacoes,
        String codigoVerificacao
) {

    public record Periodo(LocalDate inicio, LocalDate fim) {
    }

    public record Empregador(String nome, String cnpj) {
    }

    public record Trabalhador(String nome, String cpf, LocalDate dataAdmissao, String cargo,
                              String matricula, String departamento) {
    }

    public record JornadaContratual(String nome, Integer cargaHorariaDiariaMinutos,
                                    Integer intervaloMinimoMinutos) {
    }

    public static RelatorioEspelhoPontoDTO fromDomain(ConsultarRelatorioEspelhoPontoUseCase.RelatorioEspelho r) {
        return new RelatorioEspelhoPontoDTO(
                new Periodo(r.dataInicio(), r.dataFim()),
                r.dataEmissao(),
                new Empregador(r.empregadorNome(), r.empregadorCnpj()),
                new Trabalhador(r.trabalhadorNome(), r.trabalhadorCpf(), r.dataAdmissao(),
                        r.cargo(), r.matricula(), r.departamento()),
                new JornadaContratual(r.jornadaNome(), r.cargaHorariaDiariaMinutos(),
                        r.intervaloMinimoMinutos()),
                r.marcacoes().stream().map(EspelhoPontoItemDTO::fromDomain).toList(),
                r.codigoVerificacao()
        );
    }
}