package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal.GeradorArquivoAEJAdapter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@RestController
@RequestMapping("/api/v1/fiscal/aej")
public class ExportacaoFiscalController {

    private final GeradorArquivoAEJAdapter geradorAEJ;

    public ExportacaoFiscalController(GeradorArquivoAEJAdapter geradorAEJ) {
        this.geradorAEJ = geradorAEJ;
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> baixarAEJ(@RequestParam("cnpj") String cnpj,
                                            @RequestParam("razaoSocial") String razaoSocial) {

        // Busca registros (exemplo mockado para demonstração do download)
        String conteudo = geradorAEJ.gerarConteudoAEJ(cnpj, razaoSocial, Collections.emptyList(), "00000000000");
        byte[] bytes = conteudo.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"AEJ_" + cnpj + ".txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }
}
