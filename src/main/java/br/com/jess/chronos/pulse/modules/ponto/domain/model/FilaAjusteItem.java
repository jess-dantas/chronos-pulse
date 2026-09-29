package br.com.jess.chronos.pulse.modules.ponto.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Item consolidado da fila de aprovação de ajustes: o ajuste pendente,
 * o nome do colaborador e as marcações do próprio dia (contexto do espelho)
 * para o gestor decidir sem sair da tela.
 */
public record FilaAjusteItem(
        RegistroPonto ajuste,
        String colaboradorNome,
        List<MarcacaoDoDia> marcacoesDoDia
) {

    public record MarcacaoDoDia(
            Instant dataHora,
            TipoRegistro tipoRegistro,
            boolean ajuste
    ) {}
}
