package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence.EmpresaJpaEntity;
import br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.output.persistence.EmpresaJpaRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoModalidade;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus;
import br.com.jess.chronos.pulse.modules.licitacoes.infrastructure.PncpProperties;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PncpServiceTest {

    @Mock
    private EmpresaJpaRepository empresaJpaRepository;

    private Licitacao licitacao(UUID tenantId) {
        Licitacao licitacao = Licitacao.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .numero("LIC-2026-000001")
                .modalidade(LicitacaoModalidade.PREGAO)
                .objeto("Aquisição de material de escritório")
                .dataAbertura(LocalDate.of(2026, 10, 5))
                .valorEstimado(new BigDecimal("2350.00"))
                .status(LicitacaoStatus.PUBLICADA)
                .build();
        return licitacao;
    }

    private EmpresaJpaEntity empresa(String cnpj) {
        EmpresaJpaEntity empresa = new EmpresaJpaEntity();
        empresa.setId(UUID.randomUUID());
        empresa.setCnpj(cnpj);
        empresa.setNome("Câmara Municipal de Demonstração");
        return empresa;
    }

    @Test
    @DisplayName("Deve recusar com mensagem amigável quando a integração está desabilitada")
    void deveRecusarQuandoDesabilitada() {
        PncpProperties props = new PncpProperties(false, "", "", "", false);
        PncpService service = new PncpService(props, empresaJpaRepository);
        UUID tenantId = UUID.randomUUID();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publicarAviso(licitacao(tenantId), null));
        assertTrue(ex.getMessage().contains("desabilitada"));
    }

    @Test
    @DisplayName("Deve exigir token e baseUrl quando habilitada")
    void deveExigirConfiguracao() {
        PncpProperties props = new PncpProperties(true, "https://pncp.gov.br/api", "", "", false);
        PncpService service = new PncpService(props, empresaJpaRepository);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publicarAviso(licitacao(UUID.randomUUID()), null));
        assertTrue(ex.getMessage().contains("não configurados"));
    }

    @Test
    @DisplayName("Deve recusar quando o tenant não possui CNPJ cadastrado")
    void deveExigirCnpjDoTenant() {
        PncpProperties props = new PncpProperties(true, "https://pncp.gov.br/api", "token", "", false);
        PncpService service = new PncpService(props, empresaJpaRepository);
        UUID tenantId = UUID.randomUUID();

        when(empresaJpaRepository.findById(tenantId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publicarAviso(licitacao(tenantId), null));
        assertTrue(ex.getMessage().contains("não encontrado"));
    }

    @Test
    @DisplayName("Deve publicar o aviso no PNCP e retornar o protocolo")
    void devePublicarAviso() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/public/v1/orgaos/11222333000181/licitacoes", exchange -> {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (!"Bearer tok-teste".equals(auth)) {
                enviar(exchange, 401, "{\"message\":\"unauthorized\"}");
                return;
            }
            String corpo = new String(exchange.getRequestBody().readAllBytes());
            if (!corpo.contains("\"objeto\"") || !corpo.contains("\"cnpj\"")) {
                enviar(exchange, 400, "{\"message\":\"payload inválido\"}");
                return;
            }
            enviar(exchange, 201, "{\"protocolo\":\"PNCP-2026-000123\"}");
        });
        server.start();
        try {
            PncpProperties props = new PncpProperties(
                    true, "http://localhost:" + server.getAddress().getPort(), "tok-teste", "", false);
            PncpService service = new PncpService(props, empresaJpaRepository, HttpClient.newHttpClient());

            UUID tenantId = UUID.randomUUID();
            when(empresaJpaRepository.findById(tenantId))
                    .thenReturn(Optional.of(empresa("11222333000181")));

            PncpResultado resultado = service.publicarAviso(licitacao(tenantId), null);

            assertEquals("PNCP-2026-000123", resultado.protocolo());
            assertNotNull(resultado.publicadoEm());
            verify(empresaJpaRepository).findById(any(UUID.class));
        } finally {
            server.stop(0);
        }
    }

    private void enviar(HttpExchange exchange, int status, String corpo) throws IOException {
        byte[] body = corpo.getBytes();
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}