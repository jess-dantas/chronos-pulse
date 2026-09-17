package br.com.jess.chronos.pulse.modules.ponto.domain.ports.input;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Consulta os dados do Relatório Espelho de Ponto Eletrônico (art. 84 da
 * Portaria MTP n. 671/2021): identificação do empregador (nome, CNPJ), do
 * trabalhador (nome, CPF, data de admissão e cargo/função), data de emissão e
 * período, horário/jornada contratual, marcações tratadas e código de
 * verificação do conteúdo do relatório.
 */
public interface ConsultarRelatorioEspelhoPontoUseCase {

    RelatorioEspelho executar(UUID colaboradorId, UUID tenantId, int mes, int ano);

    record RelatorioEspelho(
            LocalDate dataInicio,
            LocalDate dataFim,
            Instant dataEmissao,
            String empregadorNome,
            String empregadorCnpj,
            String trabalhadorNome,
            String trabalhadorCpf,
            LocalDate dataAdmissao,
            String cargo,
            String matricula,
            String departamento,
            String jornadaNome,
            Integer cargaHorariaDiariaMinutos,
            Integer intervaloMinimoMinutos,
            List<RegistroPonto> marcacoes,
            String codigoVerificacao
    ) {
    }
}