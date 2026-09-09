package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoEdital;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoItem;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoModalidade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Montagem e leitura do aviso de licitação do PNCP
 * (Portal Nacional de Contratações Públicas).
 */
public final class PncpAvisoJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PncpAvisoJson() {
    }

    public static String montar(Licitacao licitacao, LicitacaoEdital edital, String cnpjOrgao) {
        Map<String, Object> raiz = new LinkedHashMap<>();
        raiz.put("orgao", Map.of("cnpj", cnpjOrgao));
        raiz.put("modalidadeId", modalidadeId(licitacao.getModalidade()));
        raiz.put("ano", anoDoNumero(licitacao.getNumero()));
        raiz.put("numeroSequencial", sequencialDoNumero(licitacao.getNumero()));
        raiz.put("objeto", licitacao.getObjeto());
        raiz.put("dataAberturaProposta",
                licitacao.getDataAbertura() != null ? licitacao.getDataAbertura().toString() : null);
        raiz.put("situacao", "publicada");
        raiz.put("itens", itens(licitacao));

        if (edital != null) {
            raiz.put("numeroProcesso", edital.getNumeroProcesso());
            raiz.put("numeroEdital", edital.getNumeroEdital());
            raiz.put("localSessao", edital.getLocalSessao());
            raiz.put("dataAberturaSessao",
                    edital.getDataAberturaSessao() != null ? edital.getDataAberturaSessao().toString() : null);
            raiz.put("horarioAbertura",
                    edital.getHorarioAbertura() != null ? edital.getHorarioAbertura().toString() : null);
            raiz.put("formaEntregaPropostas", edital.getFormaEntregaPropostas());
        }

        try {
            return MAPPER.writeValueAsString(raiz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar o aviso da licitação para o PNCP", e);
        }
    }

    public static String extrairProtocolo(String corpoResposta) {
        try {
            JsonNode raiz = MAPPER.readTree(corpoResposta);
            JsonNode protocolo = raiz.path("protocolo");
            if (protocolo.isMissingNode() || protocolo.asText().isBlank()) {
                throw new IllegalArgumentException("Resposta do PNCP sem protocolo de confirmação");
            }
            return protocolo.asText();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Resposta do PNCP inválida: " + e.getMessage(), e);
        }
    }

    static int modalidadeId(LicitacaoModalidade modalidade) {
        return switch (modalidade) {
            case PREGAO -> 1;
            case CONCORRENCIA -> 2;
            case LEILAO -> 3;
            case CONCURSO -> 4;
            case DIALOGO_COMPETITIVO -> 5;
        };
    }

    private static List<Map<String, Object>> itens(Licitacao licitacao) {
        List<Map<String, Object>> itens = new ArrayList<>();
        int numero = 1;
        for (LicitacaoItem item : licitacao.getItens()) {
            Map<String, Object> itemJson = new LinkedHashMap<>();
            itemJson.put("numero", numero++);
            itemJson.put("descricao", item.getDescricao());
            itemJson.put("quantidade", item.getQuantidade());
            itemJson.put("unidadeMedida", "UN");
            itens.add(itemJson);
        }
        return itens;
    }

    private static String anoDoNumero(String numero) {
        String[] partes = numero != null ? numero.split("-") : new String[0];
        return partes.length >= 2 ? partes[1] : String.valueOf(java.time.LocalDate.now().getYear());
    }

    private static String sequencialDoNumero(String numero) {
        String[] partes = numero != null ? numero.split("-") : new String[0];
        return partes.length >= 3 ? partes[2] : "";
    }
}