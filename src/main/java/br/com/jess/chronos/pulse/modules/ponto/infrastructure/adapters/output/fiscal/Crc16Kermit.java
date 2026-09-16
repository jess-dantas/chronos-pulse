package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import java.nio.charset.StandardCharsets;

/**
 * CRC-16/CCITT-TRUE (CRC-16/KERMIT), conforme exigido para o AFD do REP-A/REP-P
 * pelo Anexo V da Portaria MTP n. 671/2021. Exemplo de referência do próprio
 * leiaute: os 9 caracteres "123456789" geram o valor 0x2189 ("2189").
 */
public final class Crc16Kermit {

    private static final int POLYNOMIAL = 0x8408; // 0x1021 refletido

    private Crc16Kermit() {
    }

    public static int calcular(String conteudo) {
        return calcular(conteudo.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static int calcular(byte[] bytes) {
        int crc = 0x0000;
        for (byte b : bytes) {
            int item = crc ^ (b & 0xFF);
            for (int i = 0; i < 8; i++) {
                item = (item & 0x0001) != 0 ? (item >> 1) ^ POLYNOMIAL : item >> 1;
            }
            crc = item & 0xFFFF;
        }
        return crc;
    }

    /** Representação hexadecimal sem prefixo, com 4 dígitos (ex.: "2189"). */
    public static String hex(int crc) {
        return String.format("%04X", crc & 0xFFFF);
    }
}