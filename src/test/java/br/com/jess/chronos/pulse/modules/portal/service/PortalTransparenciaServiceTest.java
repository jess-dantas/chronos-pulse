package br.com.jess.chronos.pulse.modules.portal.service;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoAditivo;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoLicitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoSancao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoItem;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoModalidade;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoTipoJulgamento;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.ContratoAditivoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.ContratoSancaoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoContratoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoRepository;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.portal.domain.exception.PortalIndisponivelException;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalContratoDetalheDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalLicitacaoDetalheDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalLicitacaoListaDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalPublicacaoDTO;
import br.com.jess.chronos.pulse.modules.portal.web.dto.PortalResumoDTO;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.StatusPublicacaoTransparencia;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.TipoPublicacaoTransparencia;
import br.com.jess.chronos.pulse.modules.transparencia.service.TransparenciaService;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.DespesasMensaisDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.PublicacaoResponseDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.TransparenciaResumoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortalTransparenciaServiceTest {

    @Mock EmpresaRepositoryPort empresaRepository;
    @Mock ModulosPort modulosPort;
    @Mock TransparenciaService transparenciaService;
    @Mock LicitacaoRepository licitacaoRepository;
    @Mock LicitacaoContratoRepository contratoRepository;
    @Mock ContratoAditivoRepository aditivoRepository;
    @Mock ContratoSancaoRepository sancaoRepository;

    @InjectMocks
    PortalTransparenciaService service;

    private final UUID tenantId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");

    private Empresa empresa() {
        return new Empresa(tenantId, "12345678000100", "chronos-pulse-demo",
                "Chronos Pulse Tech LTDA", null, null, null, null, null, null,
                null, null, null, null, null, null);
    }

    private TransparenciaResumoDTO resumo() {
        return new TransparenciaResumoDTO(
                new TransparenciaResumoDTO.ContratosResumoDTO(
                        2, new BigDecimal("30000.00"), new BigDecimal("21000.00"),
                        new BigDecimal("9000.00"), 1, 0, 0, 1),
                new TransparenciaResumoDTO.ComprasResumoDTO(5, 10, new BigDecimal("12000.00"), 4, new BigDecimal("9000.00")),
                new TransparenciaResumoDTO.LicitacoesResumoDTO(
                        4, 1, 2, 1, 0, 1, 0, new BigDecimal("54000.00")),
                new TransparenciaResumoDTO.EstoqueResumoDTO(20, new BigDecimal("5000.00"), 15, 5),
                new TransparenciaResumoDTO.PatrimonioResumoDTO(8, 7, new BigDecimal("80000.00"), new BigDecimal("70000.00")),
                new TransparenciaResumoDTO.FrotaResumoDTO(4, 3, 2, new BigDecimal("400.00")),
                new TransparenciaResumoDTO.ColaboradoresResumoDTO(30, 28),
                new TransparenciaResumoDTO.PontoResumoDTO(1200),
                new TransparenciaResumoDTO.PublicacoesResumoDTO(3, "2026-08"));
    }

    private TransparenciaResumoDTO resumoSemLicitacoes() {
        return new TransparenciaResumoDTO(
                new TransparenciaResumoDTO.ContratosResumoDTO(
                        2, new BigDecimal("30000.00"), new BigDecimal("21000.00"),
                        new BigDecimal("9000.00"), 1, 0, 0, 1),
                new TransparenciaResumoDTO.ComprasResumoDTO(5, 10, new BigDecimal("12000.00"), 4, new BigDecimal("9000.00")),
                new TransparenciaResumoDTO.LicitacoesResumoDTO(0, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO),
                new TransparenciaResumoDTO.EstoqueResumoDTO(20, new BigDecimal("5000.00"), 15, 5),
                new TransparenciaResumoDTO.PatrimonioResumoDTO(8, 7, new BigDecimal("80000.00"), new BigDecimal("70000.00")),
                new TransparenciaResumoDTO.FrotaResumoDTO(4, 3, 2, new BigDecimal("400.00")),
                new TransparenciaResumoDTO.ColaboradoresResumoDTO(30, 28),
                new TransparenciaResumoDTO.PontoResumoDTO(1200),
                new TransparenciaResumoDTO.PublicacoesResumoDTO(3, "2026-08"));
    }

    private DespesasMensaisDTO despesas() {
        List<DespesasMensaisDTO.DespesaMensalDTO> meses = new java.util.ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            meses.add(new DespesasMensaisDTO.DespesaMensalDTO(
                    mes, new BigDecimal("1000.00"), 2, new BigDecimal("100.00"), 1,
                    new BigDecimal("500.00"), 1));
        }
        return new DespesasMensaisDTO(2026, meses);
    }

    private Licitacao licitacao(LicitacaoStatus status, String numero) {
        return Licitacao.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .numero(numero)
                .modalidade(LicitacaoModalidade.PREGAO)
                .tipoJulgamento(LicitacaoTipoJulgamento.MENOR_PRECO)
                .objeto("Aquisição de material de expediente")
                .status(status)
                .valorEstimado(new BigDecimal("15000.00"))
                .pncpPublicadoEm(Instant.now())
                .build();
    }

    private TransparenciaResumoDTO resumoComLicitacoesEmAndamento() {
        return new TransparenciaResumoDTO(
                new TransparenciaResumoDTO.ContratosResumoDTO(
                        2, new BigDecimal("30000.00"), new BigDecimal("21000.00"), new BigDecimal("9000.00"), 1, 0, 0, 1),
                new TransparenciaResumoDTO.ComprasResumoDTO(5, 10, new BigDecimal("12000.00"), 4, new BigDecimal("9000.00")),
                new TransparenciaResumoDTO.LicitacoesResumoDTO(5, 1, 2, 1, 1, 1, 0, new BigDecimal("69000.00")),
                new TransparenciaResumoDTO.EstoqueResumoDTO(20, new BigDecimal("5000.00"), 15, 5),
                new TransparenciaResumoDTO.PatrimonioResumoDTO(8, 7, new BigDecimal("80000.00"), new BigDecimal("70000.00")),
                new TransparenciaResumoDTO.FrotaResumoDTO(4, 3, 2, new BigDecimal("400.00")),
                new TransparenciaResumoDTO.ColaboradoresResumoDTO(30, 28),
                new TransparenciaResumoDTO.PontoResumoDTO(1200),
                new TransparenciaResumoDTO.PublicacoesResumoDTO(3, "2026-08"));
    }

    @Test
    @DisplayName("Resumo público agrega licitações, contratos, despesas e publicações do órgão")
    void obterResumo_agregaIndicadoresPublicos() {
        when(empresaRepository.buscarPorSlug("chronos-pulse-demo")).thenReturn(Optional.of(empresa()));
        when(modulosPort.isAtivo(tenantId, "TRANSPARENCIA")).thenReturn(true);
        when(transparenciaService.obterResumo(tenantId)).thenReturn(resumoSemLicitacoes());
        when(transparenciaService.obterDespesasMensais(tenantId, 2026)).thenReturn(despesas());
        when(licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId)).thenReturn(List.of(
                licitacao(LicitacaoStatus.PUBLICADA, "001"),
                licitacao(LicitacaoStatus.ABERTA, "002")));

        PortalResumoDTO dto = service.obterResumo("chronos-pulse-demo");

        assertEquals("Chronos Pulse Tech LTDA", dto.orgao().nome());
        assertEquals(2, dto.licitacoesPublicadas());
        assertEquals(0, dto.licitacoesEmAndamento());
        assertEquals(3, dto.publicacoesDivulgadas());
        assertEquals(2, dto.contratosAtivos());
        assertEquals(0, new BigDecimal("30000.00").compareTo(dto.valorEmpenhado()));
        assertEquals(0, new BigDecimal("13200.00").compareTo(dto.valorDespesasAno()));
    }

    @Test
    @DisplayName("Resumo considera licitações em andamento (abertas + adjudicadas) e homologadas")
    void obterResumo_licitacoesEmAndamento() {
        when(empresaRepository.buscarPorSlug("chronos-pulse-demo")).thenReturn(Optional.of(empresa()));
        when(modulosPort.isAtivo(tenantId, "TRANSPARENCIA")).thenReturn(true);
        when(transparenciaService.obterResumo(tenantId)).thenReturn(resumoComLicitacoesEmAndamento());
        when(transparenciaService.obterDespesasMensais(tenantId, 2026)).thenReturn(despesas());
        when(licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId)).thenReturn(List.of(
                licitacao(LicitacaoStatus.PUBLICADA, "001"),
                licitacao(LicitacaoStatus.ABERTA, "002"),
                licitacao(LicitacaoStatus.ADJUDICADA, "003"),
                licitacao(LicitacaoStatus.HOMOLOGADA, "004")));

        PortalResumoDTO dto = service.obterResumo("chronos-pulse-demo");

        assertEquals(3, dto.licitacoesPublicadas());
        assertEquals(2, dto.licitacoesEmAndamento());
        assertEquals(1, dto.licitacoesHomologadas());
    }

    @Test
    @DisplayName("Lista apenas licitações com situação pública")
    void listarLicitacoes_filtraStatusPublicos() {
        when(empresaRepository.buscarPorSlug("chronos-pulse-demo")).thenReturn(Optional.of(empresa()));
        when(modulosPort.isAtivo(tenantId, "TRANSPARENCIA")).thenReturn(true);
        when(licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId)).thenReturn(List.of(
                licitacao(LicitacaoStatus.EM_ELABORACAO, "001"),
                licitacao(LicitacaoStatus.PUBLICADA, "002"),
                licitacao(LicitacaoStatus.CANCELADA, "003")));

        List<PortalLicitacaoListaDTO> lista = service.listarLicitacoes("chronos-pulse-demo");

        assertEquals(1, lista.size());
        assertEquals("002", lista.get(0).numero());
        assertEquals("PUBLICADA", lista.get(0).status());
    }

    @Test
    @DisplayName("Detalhe da licitação traz itens; situação não pública é bloqueada")
    void obterLicitacao_trazItens_eBloqueiaNaoPublica() {
        when(empresaRepository.buscarPorSlug("chronos-pulse-demo")).thenReturn(Optional.of(empresa()));
        when(modulosPort.isAtivo(tenantId, "TRANSPARENCIA")).thenReturn(true);

        Licitacao publicada = licitacao(LicitacaoStatus.PUBLICADA, "002");
        UUID licitacaoId = publicada.getId();
        LicitacaoItem item = LicitacaoItem.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .descricao("Papel A4")
                .quantidade(new BigDecimal("100"))
                .valorEstimadoUnitario(new BigDecimal("15.00"))
                .build();
        publicada.setItens(List.of(item));

        UUID naoDivulgadaId = UUID.randomUUID();
        when(licitacaoRepository.findByIdAndTenantId(licitacaoId, tenantId))
                .thenReturn(Optional.of(publicada));
        when(licitacaoRepository.findByIdAndTenantId(naoDivulgadaId, tenantId))
                .thenReturn(Optional.of(licitacao(LicitacaoStatus.EM_ELABORACAO, "001")));

        PortalLicitacaoDetalheDTO detalhe = service.obterLicitacao("chronos-pulse-demo", licitacaoId);

        assertEquals(1, detalhe.itens().size());
        assertEquals("Papel A4", detalhe.itens().get(0).descricao());
        assertEquals(0, new BigDecimal("1500.0000").compareTo(detalhe.itens().get(0).valorEstimadoTotal()));

        assertThrows(PortalIndisponivelException.class,
                () -> service.obterLicitacao("chronos-pulse-demo", naoDivulgadaId));
    }

    @Test
    @DisplayName("Contrato detalhe traz aditivos e sanções")
    void obterContrato_trazAditivosESancoes() {
        when(empresaRepository.buscarPorSlug("chronos-pulse-demo")).thenReturn(Optional.of(empresa()));
        when(modulosPort.isAtivo(tenantId, "TRANSPARENCIA")).thenReturn(true);

        UUID contratoId = UUID.randomUUID();
        ContratoLicitacao contrato = ContratoLicitacao.builder()
                .id(contratoId)
                .tenantId(tenantId)
                .numero("CT-2026-001")
                .objeto("Manutenção predial")
                .dataInicio(LocalDate.of(2026, 1, 1))
                .dataFim(LocalDate.of(2026, 12, 31))
                .status("ATIVO")
                .valorTotal(new BigDecimal("120000.00"))
                .valorEmpenhado(new BigDecimal("60000.00"))
                .valorLiquidado(new BigDecimal("40000.00"))
                .build();

        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId)).thenReturn(Optional.of(contrato));
        when(aditivoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of(ContratoAditivo.builder()
                        .tipo("PRORROGACAO")
                        .descricao("Aditivo de prazo")
                        .prazoAdicionadoDias(90)
                        .aprovado(true)
                        .criadoEm(Instant.now())
                        .build()));
        when(sancaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of(ContratoSancao.builder()
                        .tipo("MULTA")
                        .descricao("Atraso na entrega")
                        .valorMulta(new BigDecimal("2000.00"))
                        .aplicadaEm(LocalDate.of(2026, 5, 10))
                        .build()));

        PortalContratoDetalheDTO detalhe = service.obterContrato("chronos-pulse-demo", contratoId);

        assertEquals("PRORROGACAO", detalhe.aditivos().get(0).tipo());
        assertEquals("MULTA", detalhe.sancoes().get(0).tipo());
        assertEquals(0, new BigDecimal("2000.00").compareTo(detalhe.sancoes().get(0).valorMulta()));
    }

    @Test
    @DisplayName("Publicações retornam apenas as divulgadas")
    void listarPublicacoes_filtraDivulgadas() {
        when(empresaRepository.buscarPorSlug("chronos-pulse-demo")).thenReturn(Optional.of(empresa()));
        when(modulosPort.isAtivo(tenantId, "TRANSPARENCIA")).thenReturn(true);
        when(transparenciaService.listarPublicacoes(tenantId)).thenReturn(List.of(
                publicacao(StatusPublicacaoTransparencia.PUBLICADO, "2026-08"),
                publicacao(StatusPublicacaoTransparencia.EM_ELABORACAO, "2026-09")));

        List<PortalPublicacaoDTO> lista = service.listarPublicacoes("chronos-pulse-demo");

        assertEquals(1, lista.size());
        assertEquals("2026-08", lista.get(0).competencia());
    }

    @Test
    @DisplayName("Slug inexistente ou sem módulo TRANSPARENCIA torna o portal indisponível")
    void orgaoPublico_validacoes() {
        when(empresaRepository.buscarPorSlug("nao-existe")).thenReturn(Optional.empty());
        assertThrows(PortalIndisponivelException.class,
                () -> service.obterResumo("nao-existe"));

        when(empresaRepository.buscarPorSlug("sem-modulo")).thenReturn(Optional.of(empresa()));
        when(modulosPort.isAtivo(tenantId, "TRANSPARENCIA")).thenReturn(false);
        assertThrows(PortalIndisponivelException.class,
                () -> service.obterResumo("sem-modulo"));
    }

    private PublicacaoResponseDTO publicacao(StatusPublicacaoTransparencia status, String competencia) {
        return new PublicacaoResponseDTO(
                UUID.randomUUID(), tenantId, competencia, TipoPublicacaoTransparencia.DESPESAS,
                new BigDecimal("50000.00"), 12, status,
                status == StatusPublicacaoTransparencia.PUBLICADO ? LocalDate.of(2026, 9, 5) : null,
                "Fechamento fiscal", Instant.now(), Instant.now());
    }
}