package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal.GeradorArquivoAEJAdapter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fiscal/aej")
public class ExportacaoFiscalController {

    private final GeradorArquivoAEJAdapter geradorAEJ;
    private final RegistroPontoRepositoryPort registroPontoRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;

    public ExportacaoFiscalController(GeradorArquivoAEJAdapter geradorAEJ,
                                      RegistroPontoRepositoryPort registroPontoRepository,
                                      CpcUsuarioRepositoryPort usuarioRepository) {
        this.geradorAEJ = geradorAEJ;
        this.registroPontoRepository = registroPontoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> baixarAEJ(@RequestParam("cnpj") String cnpj,
                                            @RequestParam("razaoSocial") String razaoSocial,
                                            @RequestParam(value = "colaboradorId", required = false) UUID colaboradorId,
                                            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        UUID tenantId = usuarioLogado.getTenantId();
        if (colaboradorId == null) {
            colaboradorId = usuarioLogado.getCpcId();
        }

        // Somente registros do tenant do usuário autenticado (evita IDOR/isolamento entre empresas)
        List<RegistroPonto> pontos = registroPontoRepository.listarPorColaborador(colaboradorId, tenantId);

        String cpfColaborador = usuarioRepository.buscarPorId(colaboradorId)
                .filter(u -> tenantId.equals(u.getTenantId()))
                .map(CpcUsuario::getCpf)
                .orElse(usuarioLogado.getCpf());

        String conteudo = geradorAEJ.gerarConteudoAEJ(cnpj, razaoSocial, pontos, cpfColaborador);
        byte[] bytes = conteudo.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"AEJ_" + cnpj + ".txt\"")
                .contentType(MediaType.parseMediaType("text/plain; charset=UTF-8"))
                .body(bytes);
    }
}
