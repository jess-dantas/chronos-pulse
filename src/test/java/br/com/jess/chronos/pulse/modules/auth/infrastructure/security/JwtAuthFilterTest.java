package br.com.jess.chronos.pulse.modules.auth.infrastructure.security;

import br.com.jess.chronos.pulse.modules.auth.application.service.DeviceTokenService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/// Escopo do vínculo de dispositivo: o header X-Device-Token só autentica a
/// sincronização de ponto, a leitura do espelho do próprio dono e a ingestão
/// de telemetria; em qualquer outra rota ele é ignorado.
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String SYNC = "/api/v1/pontos/sincronizar";
    private static final String TELEMETRIA = "/api/v1/telemetria/eventos";
    private static final String ESPELHO = "/api/v1/pontos/espelho";
    private static final String ESPELHO_RELATORIO = "/api/v1/pontos/espelho/relatorio";

    @Mock private JwtService jwtService;
    @Mock private CpcUsuarioRepositoryPort usuarioRepository;
    @Mock private DeviceTokenService deviceTokenService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtService, usuarioRepository, deviceTokenService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CpcUsuario colaborador() {
        return new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", "colab@empresa.com", "hash", Role.COLABORADOR, UUID.randomUUID());
    }

    private void stubHeaderDevice(String valor) {
        when(request.getHeader(JwtAuthFilter.HEADER_DEVICE_TOKEN)).thenReturn(valor);
    }

    @Test
    void deviceTokenValidoAutenticaNoSyncDePonto() throws Exception {
        var dono = colaborador();
        stubHeaderDevice("dt-valor");
        when(request.getRequestURI()).thenReturn(SYNC);
        when(deviceTokenService.autenticar("dt-valor")).thenReturn(Optional.of(dono));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(dono);
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .contains("ROLE_COLABORADOR");
        verify(filterChain).doFilter(request, response);
        // fluxo de sessão não é tocado quando o device token vale
        verifyNoInteractions(jwtService, usuarioRepository);
    }

    @Test
    void deviceTokenValidoAutenticaNaIngestaoDeTelemetria() throws Exception {
        var dono = colaborador();
        stubHeaderDevice("dt-valor");
        when(request.getRequestURI()).thenReturn(TELEMETRIA);
        when(deviceTokenService.autenticar("dt-valor")).thenReturn(Optional.of(dono));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(dono);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, usuarioRepository);
    }

    @Test
    void deviceTokenValidoAutenticaNaLeituraDoEspelho() throws Exception {
        var dono = colaborador();
        stubHeaderDevice("dt-valor");
        when(request.getRequestURI()).thenReturn(ESPELHO);
        when(deviceTokenService.autenticar("dt-valor")).thenReturn(Optional.of(dono));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(dono);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, usuarioRepository);
    }

    @Test
    void deviceTokenNaoCobreORelatorioDoEspelho() throws Exception {
        // Igualdade exata: o PDF (dados da empresa) não faz parte do escopo.
        stubHeaderDevice("dt-valor");
        when(request.getRequestURI()).thenReturn(ESPELHO_RELATORIO);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(deviceTokenService, jwtService);
    }

    @Test
    void deviceTokenIgnoraroForaDoSync() throws Exception {
        stubHeaderDevice("dt-valor");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/me");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        // fora do caminho permitido o serviço nem é consultado
        verifyNoInteractions(deviceTokenService, jwtService);
    }

    @Test
    void deviceTokenInvalidoNaoAutenticaNoSync() throws Exception {
        stubHeaderDevice("dt-invalido");
        when(request.getRequestURI()).thenReturn(SYNC);
        when(deviceTokenService.autenticar("dt-invalido")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    void deviceTokenDeUsuarioInativoNaoAutentica() throws Exception {
        var inativo = new CpcUsuario(colaborador().getId(), colaborador().getCpcId(),
                "12345678901", "Inativo", null, null, null, null,
                null, "hash", Role.COLABORADOR, UUID.randomUUID(),
                false, false, false, false, false, Instant.now());
        stubHeaderDevice("dt-valor");
        when(request.getRequestURI()).thenReturn(SYNC);
        when(deviceTokenService.autenticar("dt-valor")).thenReturn(Optional.of(inativo));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void semHeadersNaoAutenticaNemTocaNosServicos() throws Exception {
        stubHeaderDevice(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(deviceTokenService, jwtService, usuarioRepository);
    }
}
