package br.com.jess.chronos.pulse.modules.auth.infrastructure.security;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CpcUsuarioRepositoryPort usuarioRepository;

    public JwtAuthFilter(JwtService jwtService, CpcUsuarioRepositoryPort usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtService.isTokenValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Endpoints públicos não precisam resolver o usuário no banco:
        // evita SELECT em cpc_usuario a cada heartbeat (/auth/ping).
        if (isEndpointPublico(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtService.extrairClaims(token);
        String cpf = claims.getSubject();
        String role = claims.get("role", String.class);

        usuarioRepository.buscarPorCpf(cpf).ifPresent(usuario -> {
            // Contexto de observabilidade no MDC (R27) — limpo ao final da
            // requisição pelo TelemetriaFilter, que envolve toda a cadeia.
            if (usuario.getTenantId() != null) {
                MDC.put("tenantId", usuario.getTenantId().toString());
            }
            if (usuario.getCpcId() != null) {
                MDC.put("usuarioId", usuario.getCpcId().toString());
            }
            var authorities = new java.util.ArrayList<SimpleGrantedAuthority>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            if (usuario.isAcessoEstoque() || "ADMIN_PLATAFORMA".equals(role) || "ADMIN_EMPRESA".equals(role) || "GESTOR_RH".equals(role)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ESTOQUE"));
            }
            if (usuario.isAcessoPatrimonio() || "ADMIN_PLATAFORMA".equals(role) || "ADMIN_EMPRESA".equals(role) || "GESTOR_RH".equals(role)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_PATRIMONIO"));
            }
            if (usuario.isAcessoFrota() || "ADMIN_PLATAFORMA".equals(role) || "ADMIN_EMPRESA".equals(role) || "GESTOR_RH".equals(role)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_FROTA"));
            }
            if (usuario.isAcessoProtocolo() || "ADMIN_PLATAFORMA".equals(role) || "ADMIN_EMPRESA".equals(role) || "GESTOR_RH".equals(role)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_PROTOCOLO"));
            }
            var auth = new UsernamePasswordAuthenticationToken(usuario, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        });

        filterChain.doFilter(request, response);
    }

    private boolean isEndpointPublico(String uri) {
        return uri != null && (uri.startsWith("/api/v1/auth/ping")
                || uri.startsWith("/api/v1/publico/"));
    }
}
