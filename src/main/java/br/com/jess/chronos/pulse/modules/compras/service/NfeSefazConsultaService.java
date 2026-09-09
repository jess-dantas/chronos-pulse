package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.compras.infrastructure.SefazConsultaProperties;
import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Optional;

/**
 * Consulta do documento na SEFAZ via NFeDistDFeInt (Distribuição de DF-e).
 * Habilita com {@code app.compras.sefaz.consulta-enabled=true}; sem a
 * configuração (ambiente padrão), retorna vazio e o recebimento usa a
 * validação estrutural local (chave/DV) ou o XML importado manualmente.
 */
@Service
public class NfeSefazConsultaService {

    private static final Logger log = LoggerFactory.getLogger(NfeSefazConsultaService.class);

    private final SefazConsultaProperties properties;

    public NfeSefazConsultaService(SefazConsultaProperties properties) {
        this.properties = properties;
    }

    public Optional<NfeImportadoDTO> consultarPorChave(String chaveNfe) {
        if (!properties.consultaEnabled()) {
            return Optional.empty();
        }
        String endpoint = properties.endpoint();
        String cnpj = properties.cnpjDestinatario();
        if (endpoint.isBlank() || cnpj.isBlank()) {
            throw new IllegalArgumentException(
                    "Consulta SEFAZ habilitada, porém endpoint e/ou CNPJ destinatário não configurados (app.compras.sefaz)");
        }
        try {
            log.info("Consultando NFe {} na SEFAZ (ambiente {})", chaveNfe,
                    properties.ambienteProducao() ? "produção" : "homologação");
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .header("Content-Type", "application/soap+xml; charset=UTF-8")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            NfeSefazProtocolo.montarPedidoDistDfe(
                                    chaveNfe, cnpj, properties.ambienteProducao()),
                            java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpClient client = properties.certificadoCaminho().isBlank()
                    ? HttpClient.newHttpClient()
                    : HttpClient.newBuilder().sslContext(sslContext()).build();

            HttpResponse<String> resposta = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (resposta.statusCode() != 200) {
                throw new IllegalArgumentException("SEFAZ respondeu HTTP " + resposta.statusCode());
            }

            NfeImportadoDTO importada = NfeXmlParser.parsear(
                    NfeSefazProtocolo.extrairNfeDaResposta(resposta.body()));
            return Optional.of(new NfeImportadoDTO(
                    importada.chaveNfe(), importada.numero(), importada.serie(),
                    importada.dataEmissao(), importada.valorNota(), importada.cnpjEmitente(),
                    importada.razaoEmitente(), "SEFAZ", importada.itens()));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Falha ao consultar a NFe na SEFAZ: " + e.getMessage(), e);
        }
    }

    private SSLContext sslContext() throws Exception {
        char[] senha = properties.certificadoSenha().toCharArray();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = new FileInputStream(properties.certificadoCaminho())) {
            keyStore.load(in, senha);
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, senha);
        SSLContext contexto = SSLContext.getInstance("TLS");
        contexto.init(kmf.getKeyManagers(), null, null);
        return contexto;
    }
}