package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraStatus;
import br.com.jess.chronos.pulse.modules.compras.repository.FornecedorRepository;
import br.com.jess.chronos.pulse.modules.compras.service.ComprasService;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.*;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoContratoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoEditalRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoLanceRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoPropostaRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LicitacaoServiceTest {

    @Mock
    private LicitacaoRepository licitacaoRepository;

    @Mock
    private LicitacaoPropostaRepository propostaRepository;

    @Mock
    private LicitacaoLanceRepository lanceRepository;

    @Mock
    private LicitacaoContratoRepository contratoRepository;

    @Mock
    private LicitacaoEditalRepository editalRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private ComprasService comprasService;

    @Mock
    private PncpService pncpService;

    @InjectMocks
    private LicitacaoService service;

    private UUID tenantId;
    private Material papel;
    private Material caneta;
    private Fornecedor fornecedor1;
    private Fornecedor fornecedor2;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();

        papel = Material.builder().id(UUID.randomUUID())
                .descricao("Papel A4 - Resma").unidadeMedida("UN").build();
        caneta = Material.builder().id(UUID.randomUUID())
                .descricao("Caneta esferográfica azul").unidadeMedida("UN").build();

        fornecedor1 = Fornecedor.builder().id(UUID.randomUUID())
                .tenantId(tenantId).cnpj("11222333000181").razaoSocial("PapelCenter LTDA").ativo(true).build();
        fornecedor2 = Fornecedor.builder().id(UUID.randomUUID())
                .tenantId(tenantId).cnpj("22334455000190").razaoSocial("Suprimentos Center LTDA").ativo(true).build();
    }

    @Test
    @DisplayName("Deve criar licitação com numeração sequencial, estimativa e status em elaboração")
    void deveCriarLicitacao() {
        CadastrarLicitacaoDTO dto = new CadastrarLicitacaoDTO(
                "PREGAO", "MENOR_PRECO", "Papel A4 e canetas",
                LocalDate.now().plusDays(20), null,
                List.of(
                        new LicitacaoItemDTO(papel.getId(), new BigDecimal("100"), new BigDecimal("17.5000")),
                        new LicitacaoItemDTO(caneta.getId(), new BigDecimal("50"), new BigDecimal("1.2000"))));

        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId)).thenReturn(List.of());
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> {
            Licitacao l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        LicitacaoResponseDTO resposta = service.criarLicitacao(dto, tenantId);

        assertEquals("LIC-" + LocalDate.now().getYear() + "-000001", resposta.numero());
        assertEquals("EM_ELABORACAO", resposta.status());
        assertEquals(LicitacaoModalidade.PREGAO, resposta.modalidade());
        assertEquals(LicitacaoTipoJulgamento.MENOR_PRECO, resposta.tipoJulgamento());
        assertEquals(0, new BigDecimal("1810.00").compareTo(resposta.valorEstimado()));
        assertEquals(2, resposta.itens().size());
    }

    @Test
    @DisplayName("Deve rejeitar material duplicado na criação da licitação")
    void deveRejeitarMaterialDuplicado() {
        CadastrarLicitacaoDTO dto = new CadastrarLicitacaoDTO(
                "PREGAO", "MENOR_PRECO", "Objeto",
                null, null,
                List.of(
                        new LicitacaoItemDTO(papel.getId(), new BigDecimal("10"), new BigDecimal("5.0000")),
                        new LicitacaoItemDTO(papel.getId(), new BigDecimal("10"), new BigDecimal("5.0000"))));

        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.criarLicitacao(dto, tenantId));
        assertTrue(ex.getMessage().contains("duplicado"));
    }

    @Test
    @DisplayName("Deve rejeitar modalidade inválida")
    void deveRejeitarModalidadeInvalida() {
        CadastrarLicitacaoDTO dto = new CadastrarLicitacaoDTO(
                "TOMADA_DE_PRECO", "MENOR_PRECO", "Objeto",
                null, null,
                List.of(new LicitacaoItemDTO(papel.getId(), new BigDecimal("10"), new BigDecimal("5.0000"))));

        assertThrows(IllegalArgumentException.class, () -> service.criarLicitacao(dto, tenantId));
    }

    @Test
    @DisplayName("Deve publicar licitação habilitando fornecedores e transicionar para publicada")
    void devePublicarLicitacao() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.EM_ELABORACAO);
        licitacao.getParticipantes().clear();

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(fornecedorRepository.findByIdAndTenantId(fornecedor1.getId(), tenantId)).thenReturn(Optional.of(fornecedor1));
        when(fornecedorRepository.findByIdAndTenantId(fornecedor2.getId(), tenantId)).thenReturn(Optional.of(fornecedor2));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        PublicarLicitacaoDTO dto = new PublicarLicitacaoDTO(List.of(fornecedor1.getId(), fornecedor2.getId()));
        LicitacaoResponseDTO resposta = service.publicarLicitacao(licitacao.getId(), dto, tenantId);

        assertEquals("PUBLICADA", resposta.status());
        assertEquals(2, resposta.participantes().size());
    }

    @Test
    @DisplayName("Deve recusar publicação com fornecedor inativo")
    void deveRecusarPublicacaoFornecedorInativo() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.EM_ELABORACAO);
        licitacao.getParticipantes().clear();
        fornecedor2.setAtivo(false);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(fornecedorRepository.findByIdAndTenantId(fornecedor1.getId(), tenantId)).thenReturn(Optional.of(fornecedor1));
        when(fornecedorRepository.findByIdAndTenantId(fornecedor2.getId(), tenantId)).thenReturn(Optional.of(fornecedor2));

        PublicarLicitacaoDTO dto = new PublicarLicitacaoDTO(List.of(fornecedor1.getId(), fornecedor2.getId()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publicarLicitacao(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("inativo"));
    }

    @Test
    @DisplayName("Deve registrar propostas para fornecedor habilitado e substituir registros anteriores")
    void deveRegistrarPropostas() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.PUBLICADA);
        LicitacaoProposta antiga = proposta(fornecedor1.getId(), papel.getId(), "20.0000");
        licitacao.getPropostas().add(antiga);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(propostaRepository.findAllByLicitacaoId(licitacao.getId())).thenReturn(List.of(antiga));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrarPropostasLicitacaoDTO dto = new RegistrarPropostasLicitacaoDTO(
                fornecedor1.getId(),
                List.of(new LicitacaoPropostaDTO(papel.getId(), new BigDecimal("16.0000"), null)));

        LicitacaoResponseDTO resposta = service.registrarPropostas(licitacao.getId(), dto, tenantId);

        assertEquals(1, resposta.propostas().size());
        assertEquals("16.0000", resposta.propostas().get(0).valorUnitario().toPlainString());
        assertEquals("16.0000", resposta.propostas().get(0).valorUnitario().toPlainString());
    }

    @Test
    @DisplayName("Deve rejeitar propostas de fornecedor não habilitado")
    void deveRejeitarPropostaFornecedorNaoHabilitado() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.PUBLICADA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        RegistrarPropostasLicitacaoDTO dto = new RegistrarPropostasLicitacaoDTO(
                UUID.randomUUID(),
                List.of(new LicitacaoPropostaDTO(papel.getId(), new BigDecimal("16.0000"), null)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarPropostas(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("não habilitado"));
    }

    @Test
    @DisplayName("Deve adjudicar por menor preço definindo o vencedor de cada item")
    void deveAdjudicarPorMenorPreco() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        licitacao.getPropostas().add(proposta(fornecedor1.getId(), papel.getId(), "17.0000"));
        licitacao.getPropostas().add(proposta(fornecedor1.getId(), caneta.getId(), "125.0000"));
        licitacao.getPropostas().add(proposta(fornecedor2.getId(), papel.getId(), "16.0000"));
        licitacao.getPropostas().add(proposta(fornecedor2.getId(), caneta.getId(), "128.0000"));

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        LicitacaoResponseDTO resposta = service.adjudicarLicitacao(licitacao.getId(), tenantId);

        assertEquals("ADJUDICADA", resposta.status());
        var papelVencedor = resposta.propostas().stream()
                .filter(p -> p.materialId().equals(papel.getId()) && Boolean.TRUE.equals(p.vencedor()))
                .findFirst().orElseThrow();
        var canetaVencedor = resposta.propostas().stream()
                .filter(p -> p.materialId().equals(caneta.getId()) && Boolean.TRUE.equals(p.vencedor()))
                .findFirst().orElseThrow();
        assertEquals(fornecedor2.getId(), papelVencedor.fornecedorId());
        assertEquals(fornecedor1.getId(), canetaVencedor.fornecedorId());
    }

    @Test
    @DisplayName("Deve exigir proposta para todos os itens antes de adjudicar")
    void deveExigirPropostaParaTodosOsItens() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        licitacao.getPropostas().add(proposta(fornecedor1.getId(), papel.getId(), "17.0000"));

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.adjudicarLicitacao(licitacao.getId(), tenantId));
        assertTrue(ex.getMessage().contains("ao menos uma proposta"));
    }

    @Test
    @DisplayName("Deve rejeitar adjudicação automática para julgamento que não seja menor preço")
    void deveRejeitarAdjudicacaoTecnica() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        licitacao.setTipoJulgamento(LicitacaoTipoJulgamento.MELHOR_TECNICA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.adjudicarLicitacao(licitacao.getId(), tenantId));
        assertTrue(ex.getMessage().contains("análise técnica"));
    }

    @Test
    @DisplayName("Deve homologar somente licitação adjudicada")
    void deveHomologar() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ADJUDICADA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        LicitacaoResponseDTO resposta = service.homologarLicitacao(licitacao.getId(), tenantId);
        assertEquals("HOMOLOGADA", resposta.status());

        Licitacao emElaboracao = licitacaoBase(LicitacaoStatus.PUBLICADA);
        when(licitacaoRepository.findByIdAndTenantId(emElaboracao.getId(), tenantId)).thenReturn(Optional.of(emElaboracao));
        assertThrows(IllegalArgumentException.class,
                () -> service.homologarLicitacao(emElaboracao.getId(), tenantId));
    }

    @Test
    @DisplayName("Deve gerar pedido por fornecedor vencedor e marcar pedido como gerado")
    void deveGerarPedidos() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.HOMOLOGADA);
        licitacao.getPropostas().add(propostaVencedora(fornecedor1.getId(), caneta.getId(), "125.0000"));
        licitacao.getPropostas().add(propostaVencedora(fornecedor2.getId(), papel.getId(), "16.0000"));

        PedidoCompra pedido = PedidoCompra.builder()
                .tenantId(tenantId).numero("PC-2026-000003")
                .fornecedor(fornecedor1).objeto("Objeto")
                .dataEmissao(LocalDate.now()).valorTotal(new BigDecimal("1250.00"))
                .status(PedidoCompraStatus.EMITIDO).build();
        PedidoCompraResponseDTO respostaPedido = PedidoCompraResponseDTO.from(
                pedido, fornecedor1.getRazaoSocial(), List.of());

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(comprasService.criarPedidoPorItens(any(UUID.class), anyString(), isNull(), isNull(), isNull(),
                isNull(), anyList(), eq(tenantId))).thenReturn(respostaPedido);
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PedidoCompraResponseDTO> pedidos = service.gerarPedidos(licitacao.getId(), tenantId);

        assertEquals(2, pedidos.size());
        verify(comprasService, times(2)).criarPedidoPorItens(any(UUID.class), anyString(), isNull(), isNull(),
                isNull(), isNull(), anyList(), eq(tenantId));
        assertTrue(licitacao.getPedidoGerado());
    }

    @Test
    @DisplayName("Deve recusar cancelamento após adjudicação")
    void deveRecusarCancelamentoAposAdjudicacao() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ADJUDICADA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.cancelarLicitacao(licitacao.getId(), tenantId));
        assertTrue(ex.getMessage().contains("não pode ser cancelada"));
    }

    @Test
    @DisplayName("Deve publicar o aviso no PNCP registrando protocolo e data")
    void devePublicarAvisoNoPncp() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.PUBLICADA);
        LicitacaoEdital edital = editalBase();

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(editalRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(edital));
        when(pncpService.publicarAviso(any(Licitacao.class), any(LicitacaoEdital.class)))
                .thenReturn(new PncpResultado("PNCP-2026-000123", Instant.parse("2026-09-09T12:00:00Z")));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        LicitacaoResponseDTO resposta = service.publicarPncp(licitacao.getId(), tenantId);

        assertEquals("PUBLICADO", resposta.pncpStatus());
        assertEquals("PNCP-2026-000123", resposta.pncpProtocolo());
        assertNotNull(resposta.pncpPublicadoEm());
        assertNull(resposta.pncpErro());
        verify(pncpService).publicarAviso(any(Licitacao.class), any(LicitacaoEdital.class));
    }

    @Test
    @DisplayName("Deve registrar FALHA sem propagar exceção quando o PNCP não está disponível")
    void deveRegistrarFalhaQuandoPncpIndisponivel() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.PUBLICADA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(editalRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.empty());
        when(pncpService.publicarAviso(any(Licitacao.class), isNull()))
                .thenThrow(new IllegalArgumentException("Publicação no PNCP desabilitada. Configure a integração"));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        LicitacaoResponseDTO resposta = service.publicarPncp(licitacao.getId(), tenantId);

        assertEquals("FALHA", resposta.pncpStatus());
        assertNull(resposta.pncpProtocolo());
        assertNull(resposta.pncpPublicadoEm());
        assertTrue(resposta.pncpErro().contains("PNCP"));
    }

    @Test
    @DisplayName("Deve recusar publicação no PNCP para licitação fora da disputa")
    void deveRecusarPncpForaDaDisputa() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.EM_ELABORACAO);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publicarPncp(licitacao.getId(), tenantId));
        assertTrue(ex.getMessage().contains("publicadas/em disputa"));
        verifyNoInteractions(pncpService);
    }

    @Test
    @DisplayName("Deve recusar publicação no PNCP duplicada")
    void deveRecusarPncpDuplicado() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.PUBLICADA);
        licitacao.setPncpStatus(LicitacaoPncpStatus.PUBLICADO);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publicarPncp(licitacao.getId(), tenantId));
        assertTrue(ex.getMessage().contains("já publicado"));
        verifyNoInteractions(pncpService);
    }

    @Test
    @DisplayName("Deve abrir disputa somente a partir de licitação publicada")
    void deveAbrirDisputa() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.PUBLICADA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        LicitacaoResponseDTO resposta = service.abrirDisputa(licitacao.getId(), tenantId);

        assertEquals("ABERTA", resposta.status());

        Licitacao emElaboracao = licitacaoBase(LicitacaoStatus.EM_ELABORACAO);
        when(licitacaoRepository.findByIdAndTenantId(emElaboracao.getId(), tenantId))
                .thenReturn(Optional.of(emElaboracao));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.abrirDisputa(emElaboracao.getId(), tenantId));
        assertTrue(ex.getMessage().contains("publicadas"));
    }

    @Test
    @DisplayName("Deve registrar lance novo para fornecedor habilitado em item da licitação")
    void deveRegistrarLanceNovo() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        UUID itemId = licitacao.getItens().get(0).getId();

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(lanceRepository.findByLicitacaoIdAndLicitacaoItemIdAndFornecedorId(
                licitacao.getId(), itemId, fornecedor1.getId())).thenReturn(Optional.empty());
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrarLanceDTO dto = new RegistrarLanceDTO(itemId, fornecedor1.getId(),
                new BigDecimal("16.0000"), "Lance inicial");

        LicitacaoResponseDTO resposta = service.registrarLance(licitacao.getId(), dto, tenantId);

        assertEquals(1, resposta.lances().size());
        assertEquals(fornecedor1.getId(), resposta.lances().get(0).fornecedorId());
        assertEquals("16.0000", resposta.lances().get(0).valorUnitario().toPlainString());
    }

    @Test
    @DisplayName("Deve aceitar lance menor em disputa por menor preço, substituindo o atual")
    void deveSubstituirLanceComValorMenor() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        UUID itemId = licitacao.getItens().get(0).getId();
        LicitacaoLance atual = lance(licitacao, itemId, fornecedor1.getId(), "17.0000");
        licitacao.getLances().add(atual);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(lanceRepository.findByLicitacaoIdAndLicitacaoItemIdAndFornecedorId(
                licitacao.getId(), itemId, fornecedor1.getId())).thenReturn(Optional.of(atual));
        when(lanceRepository.save(any(LicitacaoLance.class))).thenAnswer(inv -> inv.getArgument(0));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));

        RegistrarLanceDTO dto = new RegistrarLanceDTO(itemId, fornecedor1.getId(),
                new BigDecimal("16.0000"), "Redução");

        LicitacaoResponseDTO resposta = service.registrarLance(licitacao.getId(), dto, tenantId);

        verify(lanceRepository).save(atual);
        assertEquals("16.0000", atual.getValorUnitario().toPlainString());
    }

    @Test
    @DisplayName("Deve recusar lance não inferior ao atual em disputa por menor preço")
    void deveRecusarLanceNaoInferior() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        UUID itemId = licitacao.getItens().get(0).getId();
        LicitacaoLance atual = lance(licitacao, itemId, fornecedor1.getId(), "16.0000");

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(lanceRepository.findByLicitacaoIdAndLicitacaoItemIdAndFornecedorId(
                licitacao.getId(), itemId, fornecedor1.getId())).thenReturn(Optional.of(atual));

        RegistrarLanceDTO dto = new RegistrarLanceDTO(itemId, fornecedor1.getId(),
                new BigDecimal("16.5000"), null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarLance(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("inferior"));
    }

    @Test
    @DisplayName("Deve recusar lance de fornecedor não habilitado")
    void deveRecusarLanceFornecedorNaoHabilitado() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        RegistrarLanceDTO dto = new RegistrarLanceDTO(licitacao.getItens().get(0).getId(),
                UUID.randomUUID(), new BigDecimal("16.0000"), null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarLance(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("não habilitado"));
    }

    @Test
    @DisplayName("Deve recusar lance em item fora da licitação")
    void deveRecusarLanceEmItemForaDaLicitacao() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        RegistrarLanceDTO dto = new RegistrarLanceDTO(UUID.randomUUID(), fornecedor1.getId(),
                new BigDecimal("16.0000"), null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarLance(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("não pertence"));
    }

    @Test
    @DisplayName("Deve adjudicar pela disputa de lances por menor preço")
    void deveAdjudicarPorLancesMenorPreco() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        UUID itemPapel = licitacao.getItens().get(0).getId();
        UUID itemCaneta = licitacao.getItens().get(1).getId();
        licitacao.getLances().add(lance(licitacao, itemPapel, fornecedor1.getId(), "17.0000"));
        licitacao.getLances().add(lance(licitacao, itemPapel, fornecedor2.getId(), "16.0000"));
        licitacao.getLances().add(lance(licitacao, itemCaneta, fornecedor1.getId(), "125.0000"));
        licitacao.getLances().add(lance(licitacao, itemCaneta, fornecedor2.getId(), "128.0000"));

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(lanceRepository.findAllByLicitacaoIdOrderByAtualizadoEmDesc(licitacao.getId()))
                .thenReturn(List.copyOf(licitacao.getLances()));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        LicitacaoResponseDTO resposta = service.adjudicarLicitacao(licitacao.getId(), tenantId);

        assertEquals("ADJUDICADA", resposta.status());
        var papelVencedor = resposta.propostas().stream()
                .filter(p -> p.materialId().equals(papel.getId()) && Boolean.TRUE.equals(p.vencedor()))
                .findFirst().orElseThrow();
        var canetaVencedor = resposta.propostas().stream()
                .filter(p -> p.materialId().equals(caneta.getId()) && Boolean.TRUE.equals(p.vencedor()))
                .findFirst().orElseThrow();
        assertEquals(fornecedor2.getId(), papelVencedor.fornecedorId());
        assertEquals(fornecedor1.getId(), canetaVencedor.fornecedorId());
    }

    @Test
    @DisplayName("Deve exigir lance para todos os itens antes de adjudicar pela disputa")
    void deveExigirLanceParaTodosOsItens() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        UUID itemPapel = licitacao.getItens().get(0).getId();
        licitacao.getLances().add(lance(licitacao, itemPapel, fornecedor1.getId(), "16.0000"));

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(lanceRepository.findAllByLicitacaoIdOrderByAtualizadoEmDesc(licitacao.getId()))
                .thenReturn(List.copyOf(licitacao.getLances()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.adjudicarLicitacao(licitacao.getId(), tenantId));
        assertTrue(ex.getMessage().contains("ao menos um lance"));
    }

    @Test
    @DisplayName("Deve listar lances com ranking por fornecedor e economia")
    void deveListarLances() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ABERTA);
        UUID itemPapel = licitacao.getItens().get(0).getId();
        UUID itemCaneta = licitacao.getItens().get(1).getId();
        LicitacaoLance lancePapel = lance(licitacao, itemPapel, fornecedor2.getId(), "16.0000");
        LicitacaoLance lanceCaneta = lance(licitacao, itemCaneta, fornecedor1.getId(), "125.0000");

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(papel, caneta));
        when(lanceRepository.findAllByLicitacaoIdOrderByAtualizadoEmDesc(licitacao.getId()))
                .thenReturn(List.of(lancePapel, lanceCaneta));

        List<LanceResponseDTO> lances = service.listarLances(licitacao.getId(), tenantId);

        assertEquals(2, lances.size());
        assertEquals("Suprimentos Center LTDA", lances.get(0).fornecedorNome());
        assertEquals("PapelCenter LTDA", lances.get(1).fornecedorNome());
        assertEquals(0, new BigDecimal("1.5000").compareTo(lances.get(0).economia()));
    }

    // ============================ FORMALIZAÇÃO DO CONTRATO ============================

    @Test
    @DisplayName("Deve formalizar contrato de licitação homologada somando os vencedores")
    void deveFormalizarContrato() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.HOMOLOGADA);
        licitacao.getPropostas().add(propostaVencedora(fornecedor1.getId(), caneta.getId(), "125.0000"));
        licitacao.getPropostas().add(propostaVencedora(fornecedor2.getId(), papel.getId(), "16.0000"));

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));
        when(licitacaoRepository.save(any(Licitacao.class))).thenAnswer(inv -> inv.getArgument(0));
        when(contratoRepository.save(any(ContratoLicitacao.class))).thenAnswer(inv -> inv.getArgument(0));

        FormalizarContratoDTO dto = new FormalizarContratoDTO(
                LocalDate.of(2026, 9, 20), LocalDate.of(2027, 9, 19),
                "Vigência de 12 meses", new BigDecimal("654.17"),
                new BigDecimal("7850.00"), BigDecimal.ZERO, "EMP-2026-0001", 30);

        LicitacaoResponseDTO resposta = service.formalizarContrato(licitacao.getId(), dto, tenantId);

        assertEquals(Boolean.TRUE, resposta.contratoGerado());
        verify(contratoRepository).save(argThat(contrato ->
                "CT-LIC-2026-000001".equals(contrato.getNumero())
                        && 0 == new BigDecimal("7850.00").compareTo(contrato.getValorTotal())
                        && licitacao.getId().equals(contrato.getLicitacaoId())
                        && "ATIVO".equals(contrato.getStatus())));
    }

    @Test
    @DisplayName("Deve rejeitar formalização de licitação não homologada")
    void deveRejeitarFormalizacaoSemHomologacao() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.ADJUDICADA);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        FormalizarContratoDTO dto = new FormalizarContratoDTO(
                LocalDate.of(2026, 9, 20), LocalDate.of(2027, 9, 19), null, null, null, null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.formalizarContrato(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("homologadas"));
    }

    @Test
    @DisplayName("Deve rejeitar segunda formalização de contrato para a mesma licitação")
    void deveRejeitarFormalizacaoDuplicada() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.HOMOLOGADA);
        licitacao.getPropostas().add(propostaVencedora(fornecedor2.getId(), papel.getId(), "16.0000"));
        licitacao.setContratoGerado(Boolean.TRUE);

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        FormalizarContratoDTO dto = new FormalizarContratoDTO(
                LocalDate.of(2026, 9, 20), LocalDate.of(2027, 9, 19), null, null, null, null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.formalizarContrato(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("já formalizado"));
    }

    @Test
    @DisplayName("Deve rejeitar formalização sem vencedores definidos")
    void deveRejeitarFormalizacaoSemVencedores() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.HOMOLOGADA);
        licitacao.getPropostas().add(proposta(fornecedor1.getId(), papel.getId(), "16.0000"));

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        FormalizarContratoDTO dto = new FormalizarContratoDTO(
                LocalDate.of(2026, 9, 20), LocalDate.of(2027, 9, 19), null, null, null, null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.formalizarContrato(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("Nenhum vencedor"));
    }

    @Test
    @DisplayName("Deve rejeitar formalização com data fim anterior à data início")
    void deveRejeitarFormalizacaoDataFimInvalida() {
        Licitacao licitacao = licitacaoBase(LicitacaoStatus.HOMOLOGADA);
        licitacao.getPropostas().add(propostaVencedora(fornecedor2.getId(), papel.getId(), "16.0000"));

        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId)).thenReturn(Optional.of(licitacao));

        FormalizarContratoDTO dto = new FormalizarContratoDTO(
                LocalDate.of(2027, 9, 19), LocalDate.of(2026, 9, 20), null, null, null, null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.formalizarContrato(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("não pode ser anterior"));
    }

    // ============================ FIXTURES ============================

    private LicitacaoEdital editalBase() {
        return LicitacaoEdital.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .numeroProcesso("PA-2026-0001")
                .numeroEdital("ED-2026-0001")
                .formaEntregaPropostas("ELETRONICA")
                .build();
    }

    private Licitacao licitacaoBase(LicitacaoStatus status) {
        Licitacao licitacao = Licitacao.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .numero("LIC-2026-000001")
                .modalidade(LicitacaoModalidade.PREGAO)
                .tipoJulgamento(LicitacaoTipoJulgamento.MENOR_PRECO)
                .objeto("Aquisição de material de escritório")
                .dataAbertura(LocalDate.now().plusDays(10))
                .valorEstimado(new BigDecimal("2350.00"))
                .status(status)
                .build();

        licitacao.adicionarItem(LicitacaoItem.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId).materialId(papel.getId()).descricao(papel.getDescricao())
                .quantidade(new BigDecimal("100")).valorEstimadoUnitario(new BigDecimal("17.5000"))
                .build());
        licitacao.adicionarItem(LicitacaoItem.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId).materialId(caneta.getId()).descricao(caneta.getDescricao())
                .quantidade(new BigDecimal("50")).valorEstimadoUnitario(new BigDecimal("1.2000"))
                .build());

        licitacao.adicionarParticipante(LicitacaoParticipante.builder()
                .tenantId(tenantId).fornecedor(fornecedor1).build());
        licitacao.adicionarParticipante(LicitacaoParticipante.builder()
                .tenantId(tenantId).fornecedor(fornecedor2).build());

        return licitacao;
    }

    private LicitacaoProposta proposta(UUID fornecedorId, UUID materialId, String valor) {
        return LicitacaoProposta.builder()
                .tenantId(tenantId).fornecedorId(fornecedorId).materialId(materialId)
                .valorUnitario(new BigDecimal(valor)).vencedor(Boolean.FALSE).build();
    }

    private LicitacaoProposta propostaVencedora(UUID fornecedorId, UUID materialId, String valor) {
        return LicitacaoProposta.builder()
                .tenantId(tenantId).fornecedorId(fornecedorId).materialId(materialId)
                .valorUnitario(new BigDecimal(valor)).vencedor(Boolean.TRUE).build();
    }

    private LicitacaoLance lance(Licitacao licitacao, UUID itemId, UUID fornecedorId, String valor) {
        return LicitacaoLance.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .licitacao(licitacao)
                .licitacaoItemId(itemId)
                .fornecedorId(fornecedorId)
                .valorUnitario(new BigDecimal(valor))
                .build();
    }
}