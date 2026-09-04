package br.com.jess.chronos.pulse.shared.util;

import java.util.regex.Pattern;

/**
 * Valida CNPJ no formato tradicional (somente dígitos) e no novo padrão
 * alfanumérico instituído pela Receita Federal (MP 1.151/2022 / IN RFB 2.251),
 * em que a base de 12 posições pode conter letras (A=10 ... Z=35) e o algoritmo
 * dos dígitos verificadores usa os mesmos pesos do CNPJ numérico.
 */
public final class CnpjValidator {

    private static final Pattern LIMPAR = Pattern.compile("[^0-9A-Za-z]");

    private static final int[] PESOS_PRIMEIRO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private CnpjValidator() {
    }

    public static String normalizar(String cnpj) {
        return cnpj == null ? "" : LIMPAR.matcher(cnpj).replaceAll("").toUpperCase();
    }

    public static boolean validar(String cnpj) {
        String c = normalizar(cnpj);
        if (c.length() != 14) {
            return false;
        }
        String base = c.substring(0, 12);
        return digitoVerificador(base, PESOS_PRIMEIRO) == valor(c.charAt(12))
                && digitoVerificador(base + c.charAt(12), PESOS_SEGUNDO) == valor(c.charAt(13));
    }

    private static int digitoVerificador(String caracteres, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < caracteres.length(); i++) {
            soma += valor(caracteres.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int valor(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        return ch - 'A' + 10;
    }
}