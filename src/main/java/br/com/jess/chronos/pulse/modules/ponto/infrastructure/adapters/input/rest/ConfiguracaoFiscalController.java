package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.ConfiguracaoFiscal;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.ConfiguracaoFiscalRepositoryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Configuração de exportação fiscal (AFD/AEJ) por tenant: nº de registro no INPI,
 * dados do desenvolvedor (PTRP) e CNO. Quando não informada, o download usa os
 * valores padrão ("CHRONOS PULSE" 1.0.0) ou os parâmetros explícitos da requisição.
 */
@RestController
@RequestMapping("/api/v1/fiscal/configuracao")
public class ConfiguracaoFiscalController {

    private final ConfiguracaoFiscalRepositoryPort repository;

    public ConfiguracaoFiscalController(ConfiguracaoFiscalRepositoryPort repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<ConfiguracaoFiscalDto> obter(@AuthenticationPrincipal CpcUsuario usuarioLogado) {
        ConfiguracaoFiscal config = repository.buscarPorTenant(tenantDe(usuarioLogado))
                .orElseGet(() -> ConfiguracaoFiscal.padrao(tenantDe(usuarioLogado)));
        return ResponseEntity.ok(ConfiguracaoFiscalDto.from(config));
    }

    @PutMapping
    public ResponseEntity<ConfiguracaoFiscalDto> atualizar(@RequestBody ConfiguracaoFiscalDto dto,
                                                           @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        UUID tenantId = tenantDe(usuarioLogado);
        ConfiguracaoFiscal existente = repository.buscarPorTenant(tenantId)
                .orElseGet(() -> ConfiguracaoFiscal.padrao(tenantId));
        ConfiguracaoFiscal salvo = repository.salvar(dto.sobrePor(existente));
        return ResponseEntity.ok(ConfiguracaoFiscalDto.from(salvo));
    }

    private static UUID tenantDe(CpcUsuario usuarioLogado) {
        return usuarioLogado.getTenantId();
    }

    public record ConfiguracaoFiscalDto(
            String numeroRegistroInpi,
            String cnpjDesenvolvedor,
            String prtpNome,
            String prtpVersao,
            String prtpRazaoDesenv,
            String prtpEmail,
            String cno) {

        static ConfiguracaoFiscalDto from(ConfiguracaoFiscal m) {
            return new ConfiguracaoFiscalDto(m.numeroRegistroInpi(), m.cnpjDesenvolvedor(),
                    m.prtpNome(), m.prtpVersao(), m.prtpRazaoDesenv(), m.prtpEmail(), m.cno());
        }

        ConfiguracaoFiscal sobrePor(ConfiguracaoFiscal atual) {
            return new ConfiguracaoFiscal(atual.tenantId(),
                    nv(numeroRegistroInpi, atual.numeroRegistroInpi()),
                    nv(cnpjDesenvolvedor, atual.cnpjDesenvolvedor()),
                    nv(prtpNome, atual.prtpNome()),
                    nv(prtpVersao, atual.prtpVersao()),
                    nv(prtpRazaoDesenv, atual.prtpRazaoDesenv()),
                    nv(prtpEmail, atual.prtpEmail()),
                    nv(cno, atual.cno()));
        }

        private static String nv(String novo, String atual) {
            return novo == null || novo.isBlank() ? atual : novo.trim();
        }
    }
}