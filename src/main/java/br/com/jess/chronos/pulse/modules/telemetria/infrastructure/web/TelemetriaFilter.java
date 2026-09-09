package br.com.jess.chronos.pulse.modules.telemetria.infrastructure.web;

import br.com.jess.chronos.pulse.modules.telemetria.infrastructure.config.TelemetriaProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Gera um traceId por requisição, expõe no header X-Trace-Id e mantém o
 * contexto (traceId, tenantId, usuarioId) no MDC durante todo o processamento.
 * O MDC é sempre limpo ao final (evita vazamento entre threads do pool).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TelemetriaFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("REQUEST_LOG");

    private final TelemetriaProperties properties;

    public TelemetriaFilter(TelemetriaProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank() || traceId.length() > 64) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);

        long inicio = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracaoMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
            MDC.put("statusHttp", String.valueOf(response.getStatus()));
            MDC.put("duracaoMs", String.valueOf(duracaoMs));
            MDC.put("modulo", derivarModulo(request.getRequestURI()));

            if (properties.isRequestLog() && !ehEndpointRuidoso(request)) {
                log.info("Requisição {} {} concluída.", request.getMethod(), request.getRequestURI());
            }

            MDC.clear();
        }
    }

    private static boolean ehEndpointRuidoso(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains("/actuator/health")) {
            return true;
        }
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private static String derivarModulo(String uri) {
        if (uri == null) {
            return "GERAL";
        }
        String caminho = uri;
        int queryIndex = caminho.indexOf('?');
        if (queryIndex >= 0) {
            caminho = caminho.substring(0, queryIndex);
        }
        String prefix = "/api/v1/";
        int inicio = caminho.indexOf(prefix);
        if (inicio < 0) {
            return "PLATAFORMA";
        }
        String resto = caminho.substring(inicio + prefix.length());
        int primeiroSegmento = resto.indexOf('/');
        String modulo = primeiroSegmento > 0 ? resto.substring(0, primeiroSegmento) : resto;
        return modulo.isEmpty() ? "GERAL" : modulo.toUpperCase();
    }
}