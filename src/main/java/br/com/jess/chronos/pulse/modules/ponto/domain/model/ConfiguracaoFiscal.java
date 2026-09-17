package br.com.jess.chronos.pulse.modules.ponto.domain.model;

import java.util.UUID;

/**
 * Configuração de exportação fiscal (AFD/AEJ) do tenant: número de registro no
 * INPI, dados do desenvolvedor (PTRP) e CNO. Os valores podem ser sobrescritos
 * por parâmetro explícito no download; na ausência de ambos, usa o padrão.
 */
public record ConfiguracaoFiscal(
        UUID tenantId,
        String numeroRegistroInpi,
        String cnpjDesenvolvedor,
        String prtpNome,
        String prtpVersao,
        String prtpRazaoDesenv,
        String prtpEmail,
        String cno) {

    public static final String PRTP_NOME_PADRAO = "CHRONOS PULSE";
    public static final String PRTP_VERSAO_PADRAO = "1.0.0";

    public static ConfiguracaoFiscal padrao(UUID tenantId) {
        return new ConfiguracaoFiscal(tenantId, null, null, PRTP_NOME_PADRAO, PRTP_VERSAO_PADRAO, null, null, null);
    }

    /**
     * Aplica a precedência: parâmetro explícito na requisição &gt; valor salvo na
     * configuração do tenant &gt; padrão do sistema.
     */
    public static DadosEfetivos resolver(ConfiguracaoFiscal config,
                                         String numeroRegistroInpiReq,
                                         String cnpjDesenvolvedorReq,
                                         String prtpNomeReq,
                                         String prtpVersaoReq,
                                         String prtpRazaoDesenvReq,
                                         String prtpEmailReq,
                                         String cnoReq) {
        String configNumero = config == null ? null : config.numeroRegistroInpi();
        String configCnpj = config == null ? null : config.cnpjDesenvolvedor();
        String configNome = config == null ? null : config.prtpNome();
        String configVersao = config == null ? null : config.prtpVersao();
        String configRazao = config == null ? null : config.prtpRazaoDesenv();
        String configEmail = config == null ? null : config.prtpEmail();
        String configCno = config == null ? null : config.cno();
        return new DadosEfetivos(
                efetivo(numeroRegistroInpiReq, configNumero, null),
                efetivo(cnpjDesenvolvedorReq, configCnpj, ""),
                efetivo(prtpNomeReq, configNome, PRTP_NOME_PADRAO),
                efetivo(prtpVersaoReq, configVersao, PRTP_VERSAO_PADRAO),
                efetivo(prtpRazaoDesenvReq, configRazao, ""),
                efetivo(prtpEmailReq, configEmail, ""),
                efetivo(cnoReq, configCno, null));
    }

    private static String efetivo(String explicito, String configurado, String padrao) {
        if (explicito != null && !explicito.isBlank()) {
            return explicito.trim();
        }
        if (configurado != null && !configurado.isBlank()) {
            return configurado.trim();
        }
        return padrao;
    }

    /** Valores efetivos já resolvidos para gravar no arquivo fiscal. */
    public record DadosEfetivos(
            String numeroRegistroInpi,
            String cnpjDesenvolvedor,
            String prtpNome,
            String prtpVersao,
            String prtpRazaoDesenv,
            String prtpEmail,
            String cno) {
    }
}