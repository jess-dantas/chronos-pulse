package br.com.jess.chronos.pulse.modules.ponto.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracaoFiscalTest {

    private static final UUID TENANT = UUID.randomUUID();

    @Test
    void deveUsarPadraoQuandoSemConfiguracaoESemParametro() {
        ConfiguracaoFiscal.DadosEfetivos r = ConfiguracaoFiscal.resolver(null,
                null, null, null, null, null, null, null);

        assertThat(r.numeroRegistroInpi()).isNull();
        assertThat(r.cnpjDesenvolvedor()).isEmpty();
        assertThat(r.prtpNome()).isEqualTo("CHRONOS PULSE");
        assertThat(r.prtpVersao()).isEqualTo("1.0.0");
        assertThat(r.prtpRazaoDesenv()).isEmpty();
        assertThat(r.prtpEmail()).isEmpty();
        assertThat(r.cno()).isNull();
    }

    @Test
    void parametroExplicitoTemPrecedenciaSobreConfiguracao() {
        ConfiguracaoFiscal config = new ConfiguracaoFiscal(TENANT, "BR51202400012345",
                "12.345.678/0001-90", "OUTRO SISTEMA", "2.0.0", "Sistema X", "x@exemplo.com", "123456789012345");

        ConfiguracaoFiscal.DadosEfetivos r = ConfiguracaoFiscal.resolver(config,
                "PATCH_0134", "98.765.432/0001-11", "SISTEMA OVERRIDE", "9.9.9",
                "Override", "override@exemplo.com", null);

        assertThat(r.numeroRegistroInpi()).isEqualTo("PATCH_0134");
        assertThat(r.cnpjDesenvolvedor()).isEqualTo("98.765.432/0001-11");
        assertThat(r.prtpNome()).isEqualTo("SISTEMA OVERRIDE");
        assertThat(r.prtpVersao()).isEqualTo("9.9.9");
        assertThat(r.prtpEmail()).isEqualTo("override@exemplo.com");
        assertThat(r.cno()).isEqualTo("123456789012345");
    }

    @Test
    void configuraçãoCompletaQuandoParametroVazio() {
        ConfiguracaoFiscal config = new ConfiguracaoFiscal(TENANT, "BR51202400012345",
                "12.345.678/0001-90", "OUTRO SISTEMA", "2.0.0", "Sistema X", "x@exemplo.com", "123456789012345");

        ConfiguracaoFiscal.DadosEfetivos r = ConfiguracaoFiscal.resolver(config,
                "  ", "", "", "", "", "", "");

        assertThat(r.numeroRegistroInpi()).isEqualTo("BR51202400012345");
        assertThat(r.cnpjDesenvolvedor()).isEqualTo("12.345.678/0001-90");
        assertThat(r.prtpNome()).isEqualTo("OUTRO SISTEMA");
        assertThat(r.prtpVersao()).isEqualTo("2.0.0");
        assertThat(r.prtpRazaoDesenv()).isEqualTo("Sistema X");
        assertThat(r.prtpEmail()).isEqualTo("x@exemplo.com");
        assertThat(r.cno()).isEqualTo("123456789012345");
    }
}