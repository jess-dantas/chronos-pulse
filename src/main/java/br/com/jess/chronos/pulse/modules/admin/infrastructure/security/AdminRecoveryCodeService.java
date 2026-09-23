package br.com.jess.chronos.pulse.modules.admin.infrastructure.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Códigos de recuperação do Administrator: 8 códigos no formato XXXXX-XXXXX
 * (alfabeto sem 0/O/1/I), armazenados como hash SHA-256, uso único.
 */
@Component
public class AdminRecoveryCodeService {

    public static final int QUANTIDADE = 8;
    private static final String ALFABETO = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public List<String> gerarCodigos() {
        List<String> codigos = new ArrayList<>(QUANTIDADE);
        for (int i = 0; i < QUANTIDADE; i++) {
            codigos.add(gerarCodigo());
        }
        return codigos;
    }

    private String gerarCodigo() {
        StringBuilder bruto = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            bruto.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        return bruto.substring(0, 5) + "-" + bruto.substring(5);
    }

    public String normalizar(String codigo) {
        if (codigo == null) {
            return null;
        }
        String limpo = codigo.replaceAll("[^0-9A-Za-z]", "").toUpperCase();
        if (limpo.length() != 10) {
            return limpo;
        }
        return limpo.substring(0, 5) + "-" + limpo.substring(5);
    }

    public String hash(String codigo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(normalizar(codigo).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gerar hash do código de recuperação", e);
        }
    }

    public boolean confere(String codigoInformado, String hashArmazenado) {
        if (codigoInformado == null || hashArmazenado == null) {
            return false;
        }
        byte[] esperado = hashArmazenado.getBytes(StandardCharsets.UTF_8);
        byte[] obtido = hash(codigoInformado).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperado, obtido);
    }
}
