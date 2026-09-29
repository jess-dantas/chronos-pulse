package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.AjusteStatus;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.FilaAjusteItem;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Fila consolidada do gestor RH: ajuste pendente + colaborador + marcações do dia.
 */
public record ResumoAjustePontoDTO(
        UUID id,
        UUID colaboradorId,
        String colaboradorNome,
        Instant dataHoraDispositivo,
        TipoRegistro tipoRegistro,
        Boolean ajusteManual,
        String justificativa,
        String observacao,
        Long nsr,
        Long nsrLogico,
        AjusteStatus ajusteStatus,
        List<MarcacaoDoDiaDTO> marcacoesDoDia
) {

    public record MarcacaoDoDiaDTO(
            Instant dataHora,
            TipoRegistro tipoRegistro,
            boolean ajuste
    ) {}

    public static ResumoAjustePontoDTO fromDomain(FilaAjusteItem item) {
        var ajuste = item.ajuste();
        return new ResumoAjustePontoDTO(
                ajuste.getId(),
                ajuste.getColaboradorId(),
                item.colaboradorNome(),
                ajuste.getDataHoraDispositivo(),
                ajuste.getTipoRegistro(),
                ajuste.getAjusteManual(),
                ajuste.getJustificativa(),
                ajuste.getObservacao(),
                ajuste.getNsr(),
                ajuste.getNsrLogico(),
                ajuste.getAjusteStatus(),
                item.marcacoesDoDia().stream()
                        .map(m -> new MarcacaoDoDiaDTO(m.dataHora(), m.tipoRegistro(), m.ajuste()))
                        .toList()
        );
    }
}
