package br.com.jess.chronos.pulse.modules.admin.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.net.URLEncoder;

/**
 * TOTP (RFC 6238) puro em Java — HMAC-SHA1, 30s, 6 dígitos, janela ±1.
 * Sem dependência externa; compatível com Google Authenticator.
 */
@Component
public class TotpService {

    private static final int DIGITS = 6;
    private static final int PERIOD_SECONDS = 30;
    private static final int WINDOW = 1;
    private static final String BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String gerarSegredo() {
        byte[] buffer = new byte[20];
        RANDOM.nextBytes(buffer);
        return encodeBase32(buffer);
    }

    public boolean validar(String codigo, String segredo) {
        if (codigo == null || segredo == null || segredo.isBlank()) {
            return false;
        }
        String limpo = codigo.replaceAll("\\s", "");
        if (!limpo.matches("\\d{" + DIGITS + "}")) {
            return false;
        }
        long contador = System.currentTimeMillis() / 1000L / PERIOD_SECONDS;
        for (int i = -WINDOW; i <= WINDOW; i++) {
            String esperado = gerarCodigo(segredo, contador + i);
            if (constantTimeEquals(esperado, limpo)) {
                return true;
            }
        }
        return false;
    }

    public String gerarOtpauthUri(String segredo, String conta, String emissor) {
        String e = encodeUri(emissor);
        String c = encodeUri(conta);
        return "otpauth://totp/" + e + ":" + c
                + "?secret=" + segredo
                + "&issuer=" + e
                + "&algorithm=SHA1"
                + "&digits=" + DIGITS
                + "&period=" + PERIOD_SECONDS;
    }

    private String gerarCodigo(String segredo, long contador) {
        try {
            byte[] key = decodeBase32(segredo);
            byte[] data = ByteBuffer.allocate(8).putLong(contador).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int otp = binary % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar código TOTP", e);
        }
    }

    private static String encodeBase32(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                sb.append(BASE32.charAt((buffer >> (bitsLeft - 5)) & 0x1F));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }
        return sb.toString();
    }

    private static byte[] decodeBase32(String segredo) {
        String limpo = segredo.replaceAll("=", "").replaceAll(" ", "").toUpperCase();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int buffer = 0;
        int bitsLeft = 0;
        for (char ch : limpo.toCharArray()) {
            int val = BASE32.indexOf(ch);
            if (val < 0) {
                throw new IllegalArgumentException("Segredo Base32 inválido");
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out.write((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return out.toByteArray();
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static String encodeUri(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
