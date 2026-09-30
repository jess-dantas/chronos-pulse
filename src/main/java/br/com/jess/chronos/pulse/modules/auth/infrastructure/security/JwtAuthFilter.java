package br.com.jess.chronos.pulse.modules.auth.infrastructure.security;

import br.com.jess.chronos.pulse.modules.auth.application.service.DeviceTokenService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
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
import java.time.Instant;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /// Header do vínculo de dispositivo ("Modo Ponto"). Autentica SOMENTE a
    /// sincronização de ponto, a leitura do espelho do próprio dono e a
    /// ingestão de telemetria (best-effort) — nunca rotas de sessão.
    public static final String HEADER_DEVICE_TOKEN = "X-Device-Token";
    private static final String CAMINHO_SYNC_PONTO = "/api/v1/pontos/sincronizar";
    private static final String CAMINHO_TELEMETRIA_EVENTOS = "/api/v1/telemetria/eventos";
    /// Espelho do dia/mês: igualdade exata — `/espelho/relatorio` (PDF com
    /// dados da empresa) continua fora do escopo do vínculo.
    private static final String CAMINHO_ESPELHO = "/api/v1/pontos/espelho";

    private final JwtService jwtService;
    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final DeviceTokenService deviceTokenService;

    public JwtAuthFilter(JwtService jwtService, CpcUsuarioRepositoryPort usuarioRepository,
                         DeviceTokenService deviceTokenService) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.deviceTokenService = deviceTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Vinculo de dispositivo (X-Device-Token): escopo restrito à
        // sincronização de ponto, à leitura do espelho do próprio dono (o
        // modo dispositivo equaliza histórico offline → online sem sessão) e
        // à ingestão de telemetria. Fora desses caminhos o header é ignorado;
        // neles, token inválido/expirado/revogado cai no fluxo normal
        // (sem Bearer = não autenticado → 401/403).
        String deviceToken = request.getHeader(HEADER_DEVICE_TOKEN);
        if (deviceToken != null && !deviceToken.isBlank()
                && request.getRequestURI() != null
                && (request.getRequestURI().startsWith(CAMINHO_SYNC_PONTO)
                    || request.getRequestURI().startsWith(CAMINHO_TELEMETRIA_EVENTOS)
                    || CAMINHO_ESPELHO.equals(request.getRequestURI()))) {
            var usuarioDevice = deviceTokenService.autenticar(deviceToken).orElse(null);
            if (usuarioDevice != null && usuarioDevice.isAtivo()) {
                aplicarAutenticacao(usuarioDevice);
                filterChain.doFilter(request, response);
                return;
            }
        }

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

        // Refresh tokens não podem ser usados como Bearer (LOW - security review).
        if (!jwtService.isAccessToken(claims)) {
            filterChain.doFilter(request, response);
            return;
        }

        String cpf = claims.getSubject();

        CpcUsuario usuario;
        if (claims.get("adminId", String.class) != null) {
            // Token AdminPlataforma (login /admin/auth/login): subject é o
            // username, não um CPF de cpc_usuario. Autentica direto com o
            // papel ADMIN_PLATAFORMA sem lookup em cpc_usuario.
            usuario = new CpcUsuario(null, null, cpf, cpf, null, null,
                    Role.ADMIN_PLATAFORMA, null);
        } else {
            var usuarioOpt = usuarioRepository.buscarPorCpf(cpf);
            if (usuarioOpt.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }
            usuario = usuarioOpt.get();

            // Revogação (H2): usuário desativado ou tokens emitidos antes da última
            // troca de senha não autenticam.
            if (!usuario.isAtivo()) {
                filterChain.doFilter(request, response);
                return;
            }
            Instant iat = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null;
            if (usuario.getSenhaAlteradaEm() != null && iat != null
                    && iat.isBefore(usuario.getSenhaAlteradaEm())) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        aplicarAutenticacao(usuario);
        filterChain.doFilter(request, response);
    }

    /// Resolve MDC de observabilidade (R27) e monta a autenticação com
    /// autoridades derivadas do banco — compartilhado entre o fluxo JWT de
    /// sessão e o vínculo de dispositivo.
    private void aplicarAutenticacao(CpcUsuario usuario) {
        // Contexto de observabilidade no MDC (R27) — limpo ao final da
        // requisição pelo TelemetriaFilter, que envolve toda a cadeia.
        if (usuario.getTenantId() != null) {
            MDC.put("tenantId", usuario.getTenantId().toString());
        }
        if (usuario.getCpcId() != null) {
            MDC.put("usuarioId", usuario.getCpcId().toString());
        }
        // Autoridades derivadas do banco, não do claim do token: trocas de papel
        // (ex.: transferência de titularidade) valem imediatamente, sem esperar
        // a expiração/refresh do access token.
        String role = usuario.getRole().name();
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
    }

    private boolean isEndpointPublico(String uri) {
        return uri != null && (uri.startsWith("/api/v1/auth/ping")
                || uri.startsWith("/api/v1/publico/"));
    }
}
