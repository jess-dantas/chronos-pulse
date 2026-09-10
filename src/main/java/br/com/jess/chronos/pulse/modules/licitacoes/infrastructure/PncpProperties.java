package br.com.jess.chronos.pulse.modules.licitacoes.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração opcional da publicação de avisos no PNCP
 * (Portal Nacional de Contratações Públicas).
 * Quando {@code publicacao-enabled} for false (padrão), a publicação fica
 * desabilitada e o serviço registra a falha com mensagem amigável no app.
 *
 * <pre>
 * Variáveis de ambiente:
 *   APP_LICITACOES_PNCP_PUBLICACAO_ENABLED=false|true
 *   APP_LICITACOES_PNCP_BASE_URL=https://pncp.gov.br/api
 *   APP_LICITACOES_PNCP_TOKEN_ACESSO=&lt;token&gt;
 *   APP_LICITACOES_PNCP_CNPJ_ORGAO=&lt;opcional — sobrescreve o CNPJ do tenant&gt;
 *   APP_LICITACOES_PNCP_AMBIENTE_PRODUCAO=false|true
 * </pre>
 */
@ConfigurationProperties(prefix = "app.licitacoes.pncp")
public record PncpProperties(
        boolean publicacaoEnabled,
        String baseUrl,
        String tokenAcesso,
        String cnpjOrgao,
        boolean ambienteProducao
) {
    public PncpProperties {
        if (baseUrl == null) baseUrl = "";
        if (tokenAcesso == null) tokenAcesso = "";
        if (cnpjOrgao == null) cnpjOrgao = "";
    }
}