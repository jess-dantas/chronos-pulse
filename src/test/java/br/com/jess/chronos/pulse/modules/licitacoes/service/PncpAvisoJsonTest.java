package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoEdital;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoItem;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoModalidade;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PncpAvisoJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Deve montar o aviso com dados da licitação e do edital")
    void deveMontarAviso() throws Exception {
        Licitacao licitacao = Licitacao.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .numero("LIC-2026-000001")
                .modalidade(LicitacaoModalidade.PREGAO)
                .objeto("Aquisição de material de escritório")
                .dataAbertura(LocalDate.of(2026, 10, 5))
                .status(LicitacaoStatus.PUBLICADA)
                .build();
        licitacao.adicionarItem(LicitacaoItem.builder()
                .tenantId(licitacao.getTenantId())
                .descricao("Papel A4 - Resma")
                .quantidade(new BigDecimal("100"))
                .build());
        licitacao.adicionarItem(LicitacaoItem.builder()
                .tenantId(licitacao.getTenantId())
                .descricao("Caneta esferográfica azul")
                .quantidade(new BigDecimal("50"))
                .build());

        LicitacaoEdital edital = LicitacaoEdital.builder()
                .numeroProcesso("PA-2026-0001")
                .numeroEdital("ED-2026-0001")
                .localSessao("Sala de Sessões — Av. Principal, 100")
                .dataAberturaSessao(LocalDate.of(2026, 10, 5))
                .horarioAbertura(LocalTime.of(9, 0))
                .formaEntregaPropostas("ELETRONICA")
                .build();

        String json = PncpAvisoJson.montar(licitacao, edital, "11222333000181");
        JsonNode raiz = mapper.readTree(json);

        assertEquals("11222333000181", raiz.path("orgao").path("cnpj").asText());
        assertEquals(1, raiz.path("modalidadeId").asInt());
        assertEquals("2026", raiz.path("ano").asText());
        assertEquals("000001", raiz.path("numeroSequencial").asText());
        assertEquals("Aquisição de material de escritório", raiz.path("objeto").asText());
        assertEquals("2026-10-05", raiz.path("dataAberturaProposta").asText());
        assertEquals("publicada", raiz.path("situacao").asText());
        assertEquals(2, raiz.path("itens").size());
        assertEquals("Papel A4 - Resma", raiz.path("itens").get(0).path("descricao").asText());
        assertEquals("UN", raiz.path("itens").get(0).path("unidadeMedida").asText());
        assertEquals("PA-2026-0001", raiz.path("numeroProcesso").asText());
        assertEquals("ELETRONICA", raiz.path("formaEntregaPropostas").asText());
    }

    @Test
    @DisplayName("Deve mapear a modalidade para o código PNCP")
    void deveMapearModalidades() {
        assertEquals(1, PncpAvisoJson.modalidadeId(LicitacaoModalidade.PREGAO));
        assertEquals(2, PncpAvisoJson.modalidadeId(LicitacaoModalidade.CONCORRENCIA));
        assertEquals(3, PncpAvisoJson.modalidadeId(LicitacaoModalidade.LEILAO));
        assertEquals(4, PncpAvisoJson.modalidadeId(LicitacaoModalidade.CONCURSO));
        assertEquals(5, PncpAvisoJson.modalidadeId(LicitacaoModalidade.DIALOGO_COMPETITIVO));
    }

    @Test
    @DisplayName("Deve extrair o protocolo da resposta do PNCP")
    void deveExtrairProtocolo() {
        assertEquals("PNCP-2026-000123",
                PncpAvisoJson.extrairProtocolo("{\"protocolo\":\"PNCP-2026-000123\"}"));
    }

    @Test
    @DisplayName("Deve falhar quando a resposta não contém protocolo")
    void deveFalharSemProtocolo() {
        assertThrows(IllegalArgumentException.class,
                () -> PncpAvisoJson.extrairProtocolo("{}"));
        assertThrows(IllegalArgumentException.class,
                () -> PncpAvisoJson.extrairProtocolo("resposta inválida"));
    }
}