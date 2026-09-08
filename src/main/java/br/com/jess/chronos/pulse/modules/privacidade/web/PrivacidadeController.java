package br.com.jess.chronos.pulse.modules.privacidade.web;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.privacidade.service.PrivacidadeService;
import br.com.jess.chronos.pulse.modules.privacidade.web.dto.ConsentimentoRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/privacidade")
public class PrivacidadeController {

    private final PrivacidadeService privacidadeService;

    public PrivacidadeController(PrivacidadeService privacidadeService) {
        this.privacidadeService = privacidadeService;
    }

    @GetMapping("/politica")
    public ResponseEntity<Map<String, Object>> politica() {
        return ResponseEntity.ok(privacidadeService.politicaAtual());
    }

    @GetMapping("/meus-dados")
    public ResponseEntity<Map<String, Object>> meusDados(
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        return ResponseEntity.ok(privacidadeService.exportarMeusDados(usuarioLogado));
    }

    @PostMapping("/consentimento")
    public ResponseEntity<Void> registrarConsentimento(
            @RequestBody @Valid ConsentimentoRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp,
            HttpServletRequest httpRequest) {
        privacidadeService.registrarConsentimento(
                usuarioLogado,
                request.versaoPolitica(),
                Boolean.TRUE.equals(request.aceito()),
                resolverIp(httpRequest, forwardedIp));
        return ResponseEntity.created(URI.create("/api/v1/privacidade/consentimento")).build();
    }

    @DeleteMapping("/meus-dados")
    public ResponseEntity<Void> apagarMeusDados(
            @AuthenticationPrincipal CpcUsuario usuarioLogado,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedIp,
            HttpServletRequest httpRequest) {
        privacidadeService.anonimizarMeusDados(usuarioLogado, resolverIp(httpRequest, forwardedIp));
        return ResponseEntity.noContent().build();
    }

    private String resolverIp(HttpServletRequest request, String forwardedIp) {
        if (forwardedIp != null && !forwardedIp.isBlank()) {
            return forwardedIp.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}