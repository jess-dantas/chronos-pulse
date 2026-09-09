package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence.EmpresaJpaEntity;
import br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence.EmpresaJpaRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoEdital;
import br.com.jess.chronos.pulse.modules.licitacoes.infrastructure.PncpProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Publicação de avisos de licitação no PNCP (Portal Nacional de Contratações
 * Públicas — art. 54 da Lei 14.133/2021). Habilita com
 * {@code app.licitacoes.pncp.publicacao-enabled=true}; sem a configuração
 * (ambiente padrão), a publicação registra falha com mensagem amigável no app,
 * seguindo o mesmo modelo opcional da consulta à SEFAZ (R23).
 */
@Service
public class PncpService {

    private static final Logger log = LoggerFactory.getLogger(PncpService.class);

    private final PncpProperties properties;
    private final EmpresaJpaRepository empresaJpaRepository;
    private final HttpClient httpClient;

    @Autowired
    public PncpService(PncpProperties properties, EmpresaJpaRepository empresaJpaRepository) {
        this(properties, empresaJpaRepository, HttpClient.newHttpClient());
    }

    PncpService(PncpProperties properties, EmpresaJpaRepository empresaJpaRepository, HttpClient httpClient) {
        this.properties = properties;
        this.empresaJpaRepository = empresaJpaRepository;
        this.httpClient = httpClient;
    }

    public PncpResultado publicarAviso(Licitacao licitacao, LicitacaoEdital edital) {
        if (!properties.publicacaoEnabled()) {
            throw new IllegalArgumentException(
                    "Publicação no PNCP desabilitada. Configure a integração "
                            + "(APP_LICITACOES_PNCP_PUBLICACAO_ENABLED=true e APP_LICITACOES_PNCP_TOKEN_ACESSO)");
        }
        String baseUrl = properties.baseUrl().trim();
        String token = properties.tokenAcesso().trim();
        if (baseUrl.isBlank() || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Integração PNCP habilitada, porém baseUrl e/ou token de acesso não configurados (app.licitacoes.pncp)");
        }

        String cnpjOrgao = cnpjOrgao(licitacao.getTenantId());
        String body = PncpAvisoJson.montar(licitacao, edital, cnpjOrgao);

        String endpoint = baseUrl + "/public/v1/orgaos/" + cnpjOrgao + "/licitacoes";
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(25))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        try {
            log.info("Publicando aviso da licitação {} no PNCP (cnpj {})", licitacao.getNumero(), cnpjOrgao);
            HttpResponse<String> resposta = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
                throw new IllegalArgumentException(
                        "PNCP respondeu HTTP " + resposta.statusCode() + ": " + mensagemErro(resposta.body()));
            }
            String protocolo = PncpAvisoJson.extrairProtocolo(resposta.body());
            log.info("Aviso {} publicado no PNCP (protocolo {})", licitacao.getNumero(), protocolo);
            return new PncpResultado(protocolo, Instant.now());
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha na publicação no PNCP: " + e.getMessage(), e);
        }
    }

    private String cnpjOrgao(UUID tenantId) {
        if (!properties.cnpjOrgao().isBlank()) {
            return properties.cnpjOrgao().trim();
        }
        return empresaJpaRepository.findById(tenantId)
                .map(EmpresaJpaEntity::getCnpj)
                .filter(cnpj -> !cnpj.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(
                        "CNPJ do órgão não encontrado para o tenant " + tenantId
                                + " — cadastre a empresa ou configure app.licitacoes.pncp.cnpj-orgao"));
    }

    private String mensagemErro(String corpo) {
        if (corpo == null || corpo.isBlank()) {
            return "sem detalhes";
        }
        return corpo.length() > 300 ? corpo.substring(0, 300) : corpo;
    }
}