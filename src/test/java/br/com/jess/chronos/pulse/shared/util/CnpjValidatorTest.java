package br.com.jess.chronos.pulse.shared.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CnpjValidatorTest {

    @Test
    void deveValidarCnpjNumericoLegado() {
        assertThat(CnpjValidator.validar("12345678000195")).isTrue();
        assertThat(CnpjValidator.validar("49262262000113")).isTrue();
    }

    @Test
    void deveValidarCnpjAlfanumerico() {
        assertThat(CnpjValidator.validar("12ABC34501DE45")).isTrue();
    }

    @Test
    void deveAceitarFormatosComPontuacao() {
        assertThat(CnpjValidator.validar("49.262.262/0001-13")).isTrue();
        assertThat(CnpjValidator.validar("12.ABC.345/01DE-45")).isTrue();
    }

    @Test
    void deveRejeitarDigitoVerificadorInvalido() {
        assertThat(CnpjValidator.validar("12345678000188")).isFalse();
        assertThat(CnpjValidator.validar("12ABC34501DE44")).isFalse();
    }

    @Test
    void deveRejeitarTamanhoInvalido() {
        assertThat(CnpjValidator.validar("1234567800019")).isFalse();
        assertThat(CnpjValidator.validar("123456780001951")).isFalse();
        assertThat(CnpjValidator.validar("")).isFalse();
    }

    @Test
    void deveNormalizarMantendoLetras() {
        assertThat(CnpjValidator.normalizar("12.ABC.345/01DE-45")).isEqualTo("12ABC34501DE45");
        assertThat(CnpjValidator.normalizar("12.abc.345/01de-45")).isEqualTo("12ABC34501DE45");
    }
}