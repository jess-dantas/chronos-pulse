package br.com.jess.chronos.pulse.modules.ponto.domain.service;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Gera o código de verificação (hash SHA-256) do Relatório Espelho de Ponto
 * Eletrônico (art. 84 da Portaria MTP n. 671/2021). O hash é determinístico
 * sobre o conteúdo essencial do relatório: empregador (CNPJ), trabalhador
 * (CPF), período apurado e cada marcação tratada ordenada cronologicamente.
 * Com o mesmo conteúdo, o código é reproduzível para conferência de autoria e
 * integridade do documento.
 */
public class GeradorCodigoVerificacaoEspelho {

    private GeradorCodigoVerificacaoEspelho() {
    }

    public static String gerar(String cnpjEmpregador, String cpfTrabalhador,
                               Instant inicioPeriodo, Instant fimPeriodo,
                               List<RegistroPonto> marcacoes) {
        StringBuilder payload = new StringBuilder();
        payload.append(segmento(cnpjEmpregador))
                .append(segmento(cpfTrabalhador))
                .append(segmento(inicioPeriodo == null ? "" : String.valueOf(inicioPeriodo.toEpochMilli())))
                .append(segmento(fimPeriodo == null ? "" : String.valueOf(fimPeriodo.toEpochMilli())));

        marcacoes.stream()
                .sorted(Comparator.comparing(RegistroPonto::getDataHora))
                .forEach(m -> payload.append(segmento(String.format("%s|%d|%s|%s",
                        m.getTipoRegistro() != null ? m.getTipoRegistro().name() : "",
                        m.getDataHora() != null ? m.getDataHora().toEpochMilli() : 0L,
                        m.getNsr() == null ? "" : m.getNsr(),
                        m.getHashIntegridade() == null ? "" : m.getHashIntegridade()))));

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar código de verificação do espelho de ponto", e);
        }
    }

    private static String segmento(String valor) {
        return valor + "\n";
    }
}