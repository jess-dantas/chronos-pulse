package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.ports.output.RegistroPontoRepositoryPort;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal.GeradorArquivoAEJAdapter;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal.GeradorArquivoAFDAdapter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Exportação de arquivos fiscais do controle eletrônico de ponto (REP-P),
 * conforme Portaria MTP n. 671/2021: AFD (Anexo V, art. 81) e AEJ (Anexo VI,
 * art. 83). Todos os registros são restritos ao tenant do usuário autenticado.
 */
@RestController
@RequestMapping("/api/v1/fiscal")
public class ExportacaoFiscalController {

    private static final ZoneId FUSO_BRASIL = ZoneId.of("America/Sao_Paulo");

    private final GeradorArquivoAEJAdapter geradorAEJ;
    private final GeradorArquivoAFDAdapter geradorAFD;
    private final RegistroPontoRepositoryPort registroPontoRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;

    public ExportacaoFiscalController(GeradorArquivoAEJAdapter geradorAEJ,
                                      GeradorArquivoAFDAdapter geradorAFD,
                                      RegistroPontoRepositoryPort registroPontoRepository,
                                      CpcUsuarioRepositoryPort usuarioRepository) {
        this.geradorAEJ = geradorAEJ;
        this.geradorAFD = geradorAFD;
        this.registroPontoRepository = registroPontoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/aej/download")
    public ResponseEntity<byte[]> baixarAEJ(
            @RequestParam("cnpj") String cnpj,
            @RequestParam("razaoSocial") String razaoSocial,
            @RequestParam("inicio") String inicio,
            @RequestParam("fim") String fim,
            @RequestParam(value = "colaboradorId", required = false) UUID colaboradorId,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        UUID tenantId = usuarioLogado.getTenantId();
        UUID alvo = colaboradorId == null ? usuarioLogado.getCpcId() : colaboradorId;

        LocalDate dataInicio = LocalDate.parse(inicio);
        LocalDate dataFim = LocalDate.parse(fim);
        Instant inicioInstant = dataInicio.atStartOfDay(FUSO_BRASIL).toInstant();
        Instant fimInstant = dataFim.plusDays(1).atStartOfDay(FUSO_BRASIL).toInstant();

        CpcUsuario colaborador = buscarColaborador(alvo, tenantId);
        List<RegistroPonto> pontos = registroPontoRepository
                .listarPorColaboradorEPeriodo(alvo, tenantId, inicioInstant, fimInstant);

        GeradorArquivoAEJAdapter.AejVinculo vinculo = new GeradorArquivoAEJAdapter.AejVinculo(
                1, colaborador.getCpf(), colaborador.getNome(), pontos);

        String conteudo = geradorAEJ.gerarConteudoAEJ(new GeradorArquivoAEJAdapter.GerarAEJ(
                cnpj, razaoSocial, null,
                inicioInstant, fimInstant.plusMillis(-1), Instant.now(),
                List.of(vinculo)));

        return arquivo(conteudo, "AEJ_" + cnpj + ".txt");
    }

    @GetMapping("/afd/download")
    public ResponseEntity<byte[]> baixarAFD(
            @RequestParam("cnpj") String cnpj,
            @RequestParam("razaoSocial") String razaoSocial,
            @RequestParam("inicio") String inicio,
            @RequestParam("fim") String fim,
            @RequestParam(value = "colaboradorId", required = false) UUID colaboradorId,
            @RequestParam(value = "numeroRegistroInpi", required = false) String numeroRegistroInpi,
            @RequestParam(value = "cno", required = false) String cno,
            @RequestParam("cnpjDesenvolvedor") String cnpjDesenvolvedor,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        UUID tenantId = usuarioLogado.getTenantId();
        UUID alvo = colaboradorId == null ? usuarioLogado.getCpcId() : colaboradorId;

        LocalDate dataInicio = LocalDate.parse(inicio);
        LocalDate dataFim = LocalDate.parse(fim);
        Instant inicioInstant = dataInicio.atStartOfDay(FUSO_BRASIL).toInstant();
        Instant fimInstant = dataFim.plusDays(1).atStartOfDay(FUSO_BRASIL).toInstant();

        CpcUsuario colaborador = buscarColaborador(alvo, tenantId);
        List<RegistroPonto> pontos = registroPontoRepository
                .listarPorColaboradorEPeriodo(alvo, tenantId, inicioInstant, fimInstant);

        GeradorArquivoAFDAdapter.GerarAFD dados = new GeradorArquivoAFDAdapter.GerarAFD(
                cnpj, razaoSocial, cno, numeroRegistroInpi, colaborador.getCpf(),
                cnpjDesenvolvedor, inicioInstant, fimInstant.plusMillis(-1), Instant.now(),
                pontos);

        String conteudo = geradorAFD.gerarConteudoAFD(dados);

        return arquivo(conteudo, "AFD_" + cnpj + "_REP_P.txt");
    }

    private CpcUsuario buscarColaborador(UUID colaboradorId, UUID tenantId) {
        return usuarioRepository.buscarPorId(colaboradorId)
                .filter(u -> tenantId.equals(u.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException("Colaborador não encontrado no tenant."));
    }

    private ResponseEntity<byte[]> arquivo(String conteudo, String nomeArquivo) {
        byte[] bytes = conteudo.getBytes(StandardCharsets.ISO_8859_1);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(MediaType.parseMediaType("text/plain; charset=ISO-8859-1"))
                .body(bytes);
    }
}