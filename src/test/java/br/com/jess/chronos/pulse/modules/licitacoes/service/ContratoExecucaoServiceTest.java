package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.*;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.*;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContratoExecucaoServiceTest {

    @Mock
    private LicitacaoContratoRepository contratoRepository;

    @Mock
    private ContratoAditivoRepository aditivoRepository;

    @Mock
    private ContratoApontamentoRepository apontamentoRepository;

    @Mock
    private ContratoMedicaoRepository medicaoRepository;

    @Mock
    private ContratoSancaoRepository sancaoRepository;

    @Mock
    private ContratoRescisaoRepository rescisaoRepository;

    @InjectMocks
    private ContratoExecucaoService service;

    private UUID tenantId;
    private UUID contratoId;
    private UUID criadoPor;

    @BeforeEach
    void setup() {
        tenantId = UUID.randomUUID();
        contratoId = UUID.randomUUID();
        criadoPor = UUID.randomUUID();
    }

    private ContratoLicitacao contratoPadrao(LocalDate dataFim, String status) {
        return ContratoLicitacao.builder()
                .id(contratoId)
                .tenantId(tenantId)
                .numero("CT-LIC-000001")
                .objeto("Fornecimento de materiais")
                .dataInicio(LocalDate.now().minusMonths(2))
                .dataFim(dataFim)
                .valorMensal(new BigDecimal("1000.00"))
                .valorTotal(new BigDecimal("12000.00"))
                .valorEmpenhado(new BigDecimal("12000.00"))
                .valorLiquidado(new BigDecimal("0.00"))
                .empenhoNumero("EMP-2026-0001")
                .status(status)
                .vencimentoAvisoDias(30)
                .build();
    }

    @Test
    @DisplayName("buscarExecucao monta situação VIGENTE quando faltam mais dias que o aviso")
    void buscarExecucao_vigente() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(90), "ATIVO")));
        when(aditivoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(apontamentoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(medicaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(sancaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(rescisaoRepository.findByContratoIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.empty());

        ContratoExecucaoResponseDTO resposta = service.buscarExecucao(contratoId, tenantId);

        assertEquals("VIGENTE", resposta.situacao());
        assertEquals(90, resposta.diasParaVencimento());
        assertFalse(resposta.atrasado());
    }

    @Test
    @DisplayName("buscarExecucao sinaliza EXPIRANDO dentro da janela de aviso")
    void buscarExecucao_expirando() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(10), "ATIVO")));
        when(aditivoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(apontamentoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(medicaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(sancaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(rescisaoRepository.findByContratoIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.empty());

        ContratoExecucaoResponseDTO resposta = service.buscarExecucao(contratoId, tenantId);

        assertEquals("EXPIRANDO", resposta.situacao());
    }

    @Test
    @DisplayName("buscarExecucao marca VENCIDO/atrasado quando a data fim já passou")
    void buscarExecucao_vencido() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().minusDays(5), "ATIVO")));
        when(aditivoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(apontamentoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(medicaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(sancaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(rescisaoRepository.findByContratoIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.empty());

        ContratoExecucaoResponseDTO resposta = service.buscarExecucao(contratoId, tenantId);

        assertEquals("VENCIDO", resposta.situacao());
        assertTrue(resposta.atrasado());
        assertTrue(resposta.diasParaVencimento() < 0);
    }

    @Test
    @DisplayName("buscarExecucao lança exceção quando o contrato não existe no tenant")
    void buscarExecucao_naoEncontrada() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.buscarExecucao(contratoId, tenantId));

        assertEquals("Contrato não encontrado", ex.getMessage());
    }

    @Test
    @DisplayName("registrarAditivo de prazo estende a data fim e salva")
    void registrarAditivo_prazo() {
        ContratoLicitacao contrato = contratoPadrao(LocalDate.now().plusDays(90), "ATIVO");
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contrato));

        ContratoAditivoResponseDTO resposta = service.registrarAditivo(contratoId,
                new AdicionarAditivoDTO("PRAZO", "Prorrogação de 60 dias", "Necessidade de entrega",
                        60, null),
                tenantId, criadoPor);

        assertEquals("PRAZO", resposta.tipo());
        assertTrue(resposta.aprovado());
        verify(aditivoRepository).save(any(ContratoAditivo.class));
        verify(contratoRepository).save(contrato);
        assertEquals(LocalDate.now().plusDays(90).plusDays(60), contrato.getDataFim());
    }

    @Test
    @DisplayName("registrarAditivo de valor atualiza o valor total do contrato")
    void registrarAditivo_valor() {
        ContratoLicitacao contrato = contratoPadrao(LocalDate.now().plusDays(90), "ATIVO");
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contrato));

        service.registrarAditivo(contratoId,
                new AdicionarAditivoDTO("VALOR", "Acréscimo de 10%", null, null,
                        new BigDecimal("13200.00")),
                tenantId, criadoPor);

        assertEquals(0, new BigDecimal("13200.00").compareTo(contrato.getValorTotal()));
    }

    @Test
    @DisplayName("registrarAditivo de valor sem novoValorTotal rejeita")
    void registrarAditivo_valorSemValor() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(90), "ATIVO")));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarAditivo(contratoId,
                        new AdicionarAditivoDTO("VALOR", "Sem valor", null, null, null),
                        tenantId, criadoPor));

        assertEquals("Aditivo de valor exige novoValorTotal positivo", ex.getMessage());
    }

    @Test
    @DisplayName("registrarAditivo rejeita tipo inválido")
    void registrarAditivo_tipoInvalido() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(90), "ATIVO")));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarAditivo(contratoId,
                        new AdicionarAditivoDTO("PARCIAL", "Tipo errado", null, 10, null),
                        tenantId, criadoPor));

        assertEquals("Tipo de aditivo inválido: PARCIAL", ex.getMessage());
    }

    @Test
    @DisplayName("registrarApontamento grava apontamento aberto")
    void registrarApontamento_aberto() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(90), "ATIVO")));

        ContratoApontamentoResponseDTO resposta = service.registrarApontamento(contratoId,
                new AdicionarApontamentoDTO("Maria", "Prazo de entrega não cumprido", "GRAVE"),
                tenantId, criadoPor);

        assertEquals("GRAVE", resposta.gravidade());
        assertFalse(resposta.resolvido());
        verify(apontamentoRepository).save(any(ContratoApontamento.class));
    }

    @Test
    @DisplayName("registrarApontamento rejeita gravidade desconhecida")
    void registrarApontamento_gravidadeInvalida() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(90), "ATIVO")));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarApontamento(contratoId,
                        new AdicionarApontamentoDTO("Maria", "Falha", "CRITICA"),
                        tenantId, criadoPor));

        assertEquals("Gravidade do apontamento inválida: CRITICA", ex.getMessage());
    }

    @Test
    @DisplayName("resolverApontamento marca como resolvido com hora atual")
    void resolverApontamento() {
        ContratoApontamento apontamento = ContratoApontamento.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).contratoId(contratoId)
                .fiscal("Maria").descricao("Prazo não cumprido").gravidade("GRAVE")
                .resolvido(Boolean.FALSE).criadoPor(criadoPor).build();
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(90), "ATIVO")));
        when(apontamentoRepository.findByIdAndContratoIdAndTenantId(
                apontamento.getId(), contratoId, tenantId))
                .thenReturn(Optional.of(apontamento));

        ContratoApontamentoResponseDTO resposta =
                service.resolverApontamento(contratoId, apontamento.getId(), tenantId);

        assertTrue(resposta.resolvido());
        assertNotNull(resposta.resolvidoEm());
        verify(apontamentoRepository).save(apontamento);
    }

    @Test
    @DisplayName("registrarMedicao credita o valor pago no valor liquidado")
    void registrarMedicao_creditaLiquidado() {
        ContratoLicitacao contrato = contratoPadrao(LocalDate.now().plusDays(90), "ATIVO");
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contrato));

        service.registrarMedicao(contratoId,
                new RegistrarMedicaoDTO("2026-07", new BigDecimal("1000.00"),
                        new BigDecimal("800.00"), LocalDate.now(), "Medição mensal"),
                tenantId, criadoPor);

        assertEquals(0, new BigDecimal("800.00").compareTo(contrato.getValorLiquidado()));
        verify(contratoRepository).save(contrato);
    }

    @Test
    @DisplayName("registrarSancao grava multa")
    void registrarSancao_multa() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(90), "ATIVO")));

        ContratoSancaoResponseDTO resposta = service.registrarSancao(contratoId,
                new AdicionarSancaoDTO("MULTA", "Art. 10 da Lei 14.133/2021",
                        "Aplicação de multa por atraso", new BigDecimal("2.000"),
                        null, LocalDate.now()),
                tenantId, criadoPor);

        assertEquals("MULTA", resposta.tipo());
        verify(sancaoRepository).save(any(ContratoSancao.class));
    }

    @Test
    @DisplayName("registrarSancao de multa sem valores rejeita")
    void registrarSancao_multaSemValores() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now().plusDays(90), "ATIVO")));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarSancao(contratoId,
                        new AdicionarSancaoDTO("MULTA", null, "Sem valor", null, null, LocalDate.now()),
                        tenantId, criadoPor));

        assertEquals("Sanção de multa exige percentualMulta ou valorMulta", ex.getMessage());
    }

    @Test
    @DisplayName("rescindirContrato marca o contrato como rescindido")
    void rescindirContrato() {
        ContratoLicitacao contrato = contratoPadrao(LocalDate.now().plusDays(90), "ATIVO");
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contrato));

        ContratoRescisaoResponseDTO resposta = service.rescindirContrato(contratoId,
                new RescindirContratoDTO("UNILATERAL", "Inadimplemento do fornecedor", LocalDate.now()),
                tenantId, criadoPor);

        assertEquals("UNILATERAL", resposta.tipo());
        assertEquals("RESCINDIDO", contrato.getStatus());
        assertEquals(LocalDate.now(), contrato.getDataFim());
        verify(rescisaoRepository).save(any(ContratoRescisao.class));
        verify(contratoRepository).save(contrato);
    }

    @Test
    @DisplayName("rescindirContrato rejeita rescisão em contrato já rescindido")
    void rescindirContrato_jaRescindido() {
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contratoPadrao(LocalDate.now(), "RESCINDIDO")));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.rescindirContrato(contratoId,
                        new RescindirContratoDTO("AMIGAVEL", "Acordo", LocalDate.now()),
                        tenantId, criadoPor));

        assertEquals("Contrato já rescindido", ex.getMessage());
    }

    @Test
    @DisplayName("listarExecucoes consolida os contratos do tenant")
    void listarExecucoes() {
        ContratoLicitacao contrato = contratoPadrao(LocalDate.now().plusDays(90), "ATIVO");
        when(contratoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId))
                .thenReturn(List.of(contrato));
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contrato));
        when(aditivoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(apontamentoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(medicaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(sancaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(rescisaoRepository.findByContratoIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.empty());

        List<ContratoExecucaoResponseDTO> resposta = service.listarExecucoes(tenantId);

        assertEquals(1, resposta.size());
        assertEquals("CT-LIC-000001", resposta.get(0).numero());
        assertEquals("VIGENTE", resposta.get(0).situacao());
    }

    @Test
    @DisplayName("montarExecucao expõe rescisão quando existente")
    void montarExecucao_comRescisao() {
        ContratoLicitacao contrato = contratoPadrao(LocalDate.now().minusDays(5), "RESCINDIDO");
        ContratoRescisao rescisao = ContratoRescisao.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).contratoId(contratoId)
                .tipo("JUDICIAL").motivo("Decisão judicial").dataRescisao(LocalDate.now().minusMonths(1))
                .criadoPor(criadoPor).criadoEm(Instant.now()).build();
        when(contratoRepository.findByIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(contrato));
        when(aditivoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(apontamentoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(medicaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(sancaoRepository.findAllByContratoIdAndTenantIdOrderByCriadoEmDesc(contratoId, tenantId))
                .thenReturn(List.of());
        when(rescisaoRepository.findByContratoIdAndTenantId(contratoId, tenantId))
                .thenReturn(Optional.of(rescisao));

        ContratoExecucaoResponseDTO resposta = service.buscarExecucao(contratoId, tenantId);

        assertEquals("RESCINDIDO", resposta.status());
        assertEquals("RESCINDIDO", resposta.situacao());
        assertNotNull(resposta.rescisao());
        assertEquals("JUDICIAL", resposta.rescisao().tipo());
    }
}