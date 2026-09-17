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
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
            @RequestParam(value = "numeroRegistroInpi", required = false) String numeroRegistroInpi,
            @RequestParam(value = "horarioContratual", required = false) String horarioContratual,
            @RequestParam(value = "codHorarioContratual", required = false, defaultValue = "1") String codHorarioContratual,
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

        List<GeradorArquivoAEJAdapter.AejRep> reps = new ArrayList<>();
        if (numeroRegistroInpi != null && !numeroRegistroInpi.isBlank()) {
            reps.add(new GeradorArquivoAEJAdapter.AejRep(1, 3, numeroRegistroInpi.trim()));
        }

        GeradorArquivoAEJAdapter.AejHorarioContratual horario =
                parseHorarioContratual(horarioContratual, codHorarioContratual);
        GeradorArquivoAEJAdapter.AejVinculo vinculo = new GeradorArquivoAEJAdapter.AejVinculo(
                1, colaborador.getCpf(), colaborador.getNome(), pontos, horario, List.of());

        String conteudo = geradorAEJ.gerarConteudoAEJ(new GeradorArquivoAEJAdapter.GerarAEJ(
                cnpj, razaoSocial, null,
                inicioInstant, fimInstant.plusMillis(-1), Instant.now(),
                List.of(vinculo), reps));

        return arquivo(conteudo, "AEJ_" + cnpj + ".txt");
    }

    /**
     * Converte "0800-1200;1300-1800" (ou "0800,1200,1300,1800") em um horário
     * contratual do AEJ (registro "04"), com duração calculada pela soma dos pares.
     */
    private static GeradorArquivoAEJAdapter.AejHorarioContratual parseHorarioContratual(
            String horarioContratual, String codHorario) {
        if (horarioContratual == null || horarioContratual.isBlank()) {
            return null;
        }
        String normalizado = horarioContratual.trim().replace(",", "-").replace(";", "-");
        String[] horas = normalizado.split("-");
        if (horas.length < 2 || horas.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "horarioContratual deve ser um ou mais pares HHmm-HHmm (ex.: 0800-1200;1300-1800).");
        }
        List<GeradorArquivoAEJAdapter.ParJornada> pares = new ArrayList<>();
        int duracaoMinutos = 0;
        for (int i = 0; i < horas.length; i += 2) {
            String entrada = horas[i].trim();
            String saida = horas[i + 1].trim();
            pares.add(new GeradorArquivoAEJAdapter.ParJornada(entrada, saida));
            duracaoMinutos += Math.max(0, minutos(entrada, saida));
        }
        return new GeradorArquivoAEJAdapter.AejHorarioContratual(codHorario, duracaoMinutos, pares);
    }

    private static int minutos(String entrada, String saida) {
        return (int) java.time.Duration.between(
                LocalTime.parse(entrada), LocalTime.parse(saida)).toMinutes();
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