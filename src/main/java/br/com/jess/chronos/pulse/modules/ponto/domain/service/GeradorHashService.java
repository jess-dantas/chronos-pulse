package br.com.jess.chronos.pulse.modules.ponto.domain.service;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GeradorHashService {

    public static String gerarHashRegistro(RegistroPonto registro, String cpfColaborador) {
        try {
            String payload = String.format("%s|%s|%s|%s|%s|%s",
                    cpfColaborador,
                    registro.getColaboradorId(),
                    registro.getDataHoraDispositivo().toEpochMilli(),
                    registro.getTipoRegistro() != null ? registro.getTipoRegistro().name() : "",
                    registro.getLatitude(),
                    registro.getLongitude()
            );

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar Hash SHA-256 do registro de ponto", e);
        }
    }
}