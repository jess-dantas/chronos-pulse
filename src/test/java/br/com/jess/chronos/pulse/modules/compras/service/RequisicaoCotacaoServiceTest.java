package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoFornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoProposta;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.CotacaoStatus;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraStatus;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoCompraItem;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.RequisicaoStatus;
import br.com.jess.chronos.pulse.modules.compras.repository.CotacaoPropostaRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.CotacaoRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.FornecedorRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.RequisicaoCompraRepository;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarCotacaoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarRequisicaoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CotacaoResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PropostaItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RegistrarPropostasDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RequisicaoItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.RequisicaoResponseDTO;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequisicaoCotacaoServiceTest {

    @Mock
    private RequisicaoCompraRepository requisicaoRepository;

    @Mock
    private CotacaoRepository cotacaoRepository;

    @Mock
    private CotacaoPropostaRepository propostaRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private CpcUsuarioRepositoryPort cpcUsuarioRepositoryPort;

    @Mock
    private ComprasService comprasService;

    @InjectMocks
    private RequisicaoCotacaoService service;

    private UUID tenantId;
    private CpcUsuario solicitante;
    private Material papel;
    private Material caneta;
    private Fornecedor fornecedor1;
    private Fornecedor fornecedor2;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();

        solicitante = new CpcUsuario(
                null, null, "00000000191", "Solicitante Teste", "s@empresa.com.br",
                "senha", Role.GESTOR_RH, tenantId);

        papel = Material.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .descricao("Papel A4 Sulfite 75g").unidadeMedida("RESMA").build();
        caneta = Material.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .descricao("Caneta Esferográfica Azul").unidadeMedida("CX").build();

        fornecedor1 = Fornecedor.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .cnpj("11222333000181").razaoSocial("FORNECEDOR A LTDA").ativo(true).build();
        fornecedor2 = Fornecedor.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .cnpj("22334455000190").razaoSocial("FORNECEDOR B LTDA").ativo(true).build();

        lenient().when(materialRepository.findAllByTenantId(tenantId))
                .thenReturn(List.of(papel, caneta));
        lenient().when(requisicaoRepository.findAllByTenantIdOrderByDataRequisicaoDesc(tenantId))
                .thenReturn(List.of());
        lenient().when(cotacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId))
                .thenReturn(List.of());
    }

    // ============================ REQUISIÇÕES ============================

    @Test
    @DisplayName("criarRequisicao cria requisição EM_ABERTO com itens e numeração RC")
    void criarRequisicao_comSucesso() {
        when(cpcUsuarioRepositoryPort.buscarPorId(solicitante.getCpcId()))
                .thenReturn(Optional.of(solicitante));
        when(requisicaoRepository.save(any(RequisicaoCompra.class)))
                .thenAnswer(inv -> {
                    RequisicaoCompra salva = inv.getArgument(0);
                    if (salva.getId() == null) {
                        salva.setId(UUID.randomUUID());
                    }
                    return salva;
                });

        RequisicaoResponseDTO resposta = service.criarRequisicao(
                new CadastrarRequisicaoDTO(
                        solicitante.getCpcId(),
                        "Reposição de papel",
                        "Observação",
                        List.of(
                                new RequisicaoItemDTO(papel.getId(), new BigDecimal("50.000"), "Resma"),
                                new RequisicaoItemDTO(caneta.getId(), new BigDecimal("20.000"), null))),
                tenantId);

        assertNotNull(resposta.id());
        assertEquals("RC-2026-000001", resposta.numero());
        assertEquals("EM_ABERTO", resposta.status());
        assertEquals("Solicitante Teste", resposta.solicitanteNome());
        assertEquals(2, resposta.itens().size());
        assertEquals("Papel A4 Sulfite 75g", resposta.itens().get(0).materialDescricao());

        RequisicaoCompra capturada = argumentCaptorRequisicao();
        assertEquals(2, capturada.getItens().size());
        assertEquals(new BigDecimal("50.000"), capturada.getItens().get(0).getQuantidade());
    }

    @Test
    @DisplayName("criarRequisicao rejeita solicitante de outro tenant")
    void criarRequisicao_solicitanteForaDoTenant() {
        CpcUsuario outro = new CpcUsuario(
                null, null, "00000000272", "Outro", "o@empresa.com.br", "senha", Role.GESTOR_RH,
                UUID.randomUUID());
        when(cpcUsuarioRepositoryPort.buscarPorId(outro.getCpcId())).thenReturn(Optional.of(outro));

        assertThrows(IllegalArgumentException.class, () -> service.criarRequisicao(
                new CadastrarRequisicaoDTO(outro.getCpcId(), "Just", "Ob", List.of(
                        new RequisicaoItemDTO(papel.getId(), new BigDecimal("1"), null))),
                tenantId));
    }

    @Test
    @DisplayName("criarRequisicao rejeita material inexistente")
    void criarRequisicao_materialInexistente() {
        when(cpcUsuarioRepositoryPort.buscarPorId(solicitante.getCpcId()))
                .thenReturn(Optional.of(solicitante));

        UUID materialFantasma = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> service.criarRequisicao(
                new CadastrarRequisicaoDTO(solicitante.getCpcId(), "Just", "Ob", List.of(
                        new RequisicaoItemDTO(materialFantasma, new BigDecimal("1"), null))),
                tenantId));
    }

    @Test
    @DisplayName("criarRequisicao rejeita material duplicado")
    void criarRequisicao_materialDuplicado() {
        when(cpcUsuarioRepositoryPort.buscarPorId(solicitante.getCpcId()))
                .thenReturn(Optional.of(solicitante));

        assertThrows(IllegalArgumentException.class, () -> service.criarRequisicao(
                new CadastrarRequisicaoDTO(solicitante.getCpcId(), "Just", "Ob", List.of(
                        new RequisicaoItemDTO(papel.getId(), new BigDecimal("10"), null),
                        new RequisicaoItemDTO(papel.getId(), new BigDecimal("5"), null))),
                tenantId));
    }

    @Test
    @DisplayName("cancelarRequisicao cancela requisição sem cotação ativa")
    void cancelarRequisicao_semCotacaoAtiva() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.EM_ABERTO);
        when(requisicaoRepository.findByIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.of(requisicao));
        when(cotacaoRepository.findByRequisicaoIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.empty());

        service.cancelarRequisicao(requisicao.getId(), tenantId);

        assertEquals(RequisicaoStatus.CANCELADA, requisicao.getStatus());
        verify(requisicaoRepository).save(requisicao);
    }

    @Test
    @DisplayName("cancelarRequisicao bloqueia quando existe cotação em andamento")
    void cancelarRequisicao_bloqueiaComCotacaoAtiva() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.EM_ABERTO);
        CotacaoCompra cotacao = cotacaoBase(requisicao, CotacaoStatus.EM_ANDAMENTO);
        when(requisicaoRepository.findByIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.of(requisicao));
        when(cotacaoRepository.findByRequisicaoIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));

        assertThrows(IllegalArgumentException.class,
                () -> service.cancelarRequisicao(requisicao.getId(), tenantId));
    }

    @Test
    @DisplayName("cancelarRequisicao bloqueia requisição já cotada")
    void cancelarRequisicao_jaCotada() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.COTADA);
        when(requisicaoRepository.findByIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.of(requisicao));

        assertThrows(IllegalArgumentException.class,
                () -> service.cancelarRequisicao(requisicao.getId(), tenantId));
    }

    // ============================ COTAÇÕES ============================

    @Test
    @DisplayName("criarCotacao cria cotação EM_ANDAMENTO com fornecedores convidados")
    void criarCotacao_comSucesso() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.EM_ABERTO);
        when(requisicaoRepository.findByIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.of(requisicao));
        when(cotacaoRepository.findByRequisicaoIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.empty());
        when(fornecedorRepository.findByIdAndTenantId(fornecedor1.getId(), tenantId))
                .thenReturn(Optional.of(fornecedor1));
        when(fornecedorRepository.findByIdAndTenantId(fornecedor2.getId(), tenantId))
                .thenReturn(Optional.of(fornecedor2));
        when(cotacaoRepository.save(any(CotacaoCompra.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CotacaoResponseDTO resposta = service.criarCotacao(
                new CadastrarCotacaoDTO(requisicao.getId(), List.of(fornecedor1.getId(), fornecedor2.getId()),
                        LocalDate.now().plusDays(7), "Convite"),
                tenantId);

        assertEquals("COT-2026-000001", resposta.numero());
        assertEquals("EM_ANDAMENTO", resposta.status());
        assertEquals(2, resposta.fornecedores().size());
        assertEquals("FORNECEDOR A LTDA", resposta.fornecedores().get(0).razaoSocial());
        assertEquals(requisicao.getNumero(), resposta.requisicaoNumero());
    }

    @Test
    @DisplayName("criarCotacao rejeita requisição fora do status EM_ABERTO")
    void criarCotacao_requisicaoNaoAberta() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.COTADA);
        when(requisicaoRepository.findByIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.of(requisicao));

        assertThrows(IllegalArgumentException.class, () -> service.criarCotacao(
                new CadastrarCotacaoDTO(requisicao.getId(), List.of(fornecedor1.getId()), null, null),
                tenantId));
    }

    @Test
    @DisplayName("criarCotacao rejeita fornecedor inativo")
    void criarCotacao_fornecedorInativo() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.EM_ABERTO);
        when(requisicaoRepository.findByIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.of(requisicao));
        when(cotacaoRepository.findByRequisicaoIdAndTenantId(requisicao.getId(), tenantId))
                .thenReturn(Optional.empty());
        Fornecedor inativo = Fornecedor.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .cnpj("33445566000161").razaoSocial("INATIVO LTDA").ativo(false).build();
        when(fornecedorRepository.findByIdAndTenantId(inativo.getId(), tenantId))
                .thenReturn(Optional.of(inativo));

        assertThrows(IllegalArgumentException.class, () -> service.criarCotacao(
                new CadastrarCotacaoDTO(requisicao.getId(), List.of(inativo.getId()), null, null),
                tenantId));
    }

    @Test
    @DisplayName("registrarPropostas substitui propostas anteriores do fornecedor")
    void registrarPropostas_substituiAnteriores() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.EM_ABERTO);
        CotacaoCompra cotacao = cotacaoComConvidados(requisicao, List.of(fornecedor1, fornecedor2));
        cotacao.adicionarProposta(proposta(cotacao, fornecedor1.getId(), papel.getId(), "10.00"));

        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));
        when(propostaRepository.findAllByCotacaoId(cotacao.getId()))
                .thenReturn(cotacao.getPropostas());
        when(cotacaoRepository.save(any(CotacaoCompra.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CotacaoResponseDTO resposta = service.registrarPropostas(
                cotacao.getId(),
                new RegistrarPropostasDTO(fornecedor1.getId(), List.of(
                        new PropostaItemDTO(papel.getId(), new BigDecimal("16.50")),
                        new PropostaItemDTO(caneta.getId(), new BigDecimal("125.00")))),
                tenantId);

        assertEquals(2, resposta.propostas().size());
        assertEquals(2, cotacao.getPropostas().size());
        boolean todasDoFornecedor1 = cotacao.getPropostas().stream()
                .allMatch(p -> p.getFornecedorId().equals(fornecedor1.getId()));
        assertTrue(todasDoFornecedor1);
        verify(cotacaoRepository).save(cotacao);
    }

    @Test
    @DisplayName("registrarPropostas rejeita fornecedor não convidado")
    void registrarPropostas_fornecedorNaoConvidado() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.EM_ABERTO);
        CotacaoCompra cotacao = cotacaoComConvidados(requisicao, List.of(fornecedor1));
        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));

        assertThrows(IllegalArgumentException.class, () -> service.registrarPropostas(
                cotacao.getId(),
                new RegistrarPropostasDTO(fornecedor2.getId(),
                        List.of(new PropostaItemDTO(papel.getId(), new BigDecimal("15.00")))),
                tenantId));
    }

    @Test
    @DisplayName("registrarPropostas rejeita material fora da requisição")
    void registrarPropostas_materialForaDaRequisicao() {
        RequisicaoCompra requisicao = requisicaoComItens(RequisicaoStatus.EM_ABERTO, List.of(papel));
        CotacaoCompra cotacao = cotacaoComConvidados(requisicao, List.of(fornecedor1));
        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));

        assertThrows(IllegalArgumentException.class, () -> service.registrarPropostas(
                cotacao.getId(),
                new RegistrarPropostasDTO(fornecedor1.getId(),
                        List.of(new PropostaItemDTO(caneta.getId(), new BigDecimal("15.00")))),
                tenantId));
    }

    @Test
    @DisplayName("concluirCotacao marca vencedor por item (menor valor) e requisição vira COTADA")
    void concluirCotacao_defineVencedores() {
        RequisicaoCompra requisicao = requisicaoComItens(RequisicaoStatus.EM_ABERTO, List.of(papel, caneta));
        CotacaoCompra cotacao = cotacaoComConvidados(requisicao, List.of(fornecedor1, fornecedor2));
        cotacao.adicionarProposta(proposta(cotacao, fornecedor1.getId(), papel.getId(), "17.00"));
        cotacao.adicionarProposta(proposta(cotacao, fornecedor2.getId(), papel.getId(), "16.50"));
        cotacao.adicionarProposta(proposta(cotacao, fornecedor1.getId(), caneta.getId(), "125.00"));
        cotacao.adicionarProposta(proposta(cotacao, fornecedor2.getId(), caneta.getId(), "128.00"));

        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));
        when(cotacaoRepository.save(any(CotacaoCompra.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CotacaoResponseDTO resposta = service.concluirCotacao(cotacao.getId(), tenantId);

        assertEquals("CONCLUIDA", resposta.status());
        assertEquals(RequisicaoStatus.COTADA, requisicao.getStatus());
        assertEquals(2, resposta.propostas().stream().filter(p -> p.vencedor()).count());
        assertTrue(cotacao.getPropostas().stream()
                .anyMatch(p -> p.getVencedor() && p.getFornecedorId().equals(fornecedor2.getId())
                        && p.getMaterialId().equals(papel.getId())));
        assertTrue(cotacao.getPropostas().stream()
                .anyMatch(p -> p.getVencedor() && p.getFornecedorId().equals(fornecedor1.getId())
                        && p.getMaterialId().equals(caneta.getId())));
    }

    @Test
    @DisplayName("concluirCotacao falha quando algum material não tem proposta")
    void concluirCotacao_faltaPropostaDeMaterial() {
        RequisicaoCompra requisicao = requisicaoComItens(RequisicaoStatus.EM_ABERTO, List.of(papel, caneta));
        CotacaoCompra cotacao = cotacaoComConvidados(requisicao, List.of(fornecedor1));
        cotacao.adicionarProposta(proposta(cotacao, fornecedor1.getId(), papel.getId(), "17.00"));

        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));

        assertThrows(IllegalArgumentException.class, () -> service.concluirCotacao(cotacao.getId(), tenantId));
    }

    @Test
    @DisplayName("cancelarCotacao rejeita cotação concluída")
    void cancelarCotacao_concluida() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.EM_ABERTO);
        CotacaoCompra cotacao = cotacaoBase(requisicao, CotacaoStatus.CONCLUIDA);
        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));

        assertThrows(IllegalArgumentException.class, () -> service.cancelarCotacao(cotacao.getId(), tenantId));
    }

    // ============================ GERAR PEDIDOS ============================

    @Test
    @DisplayName("gerarPedidos cria um pedido por fornecedor vencedor e marca pedido_gerado")
    void gerarPedidos_criaPedidosPorFornecedor() {
        RequisicaoCompra requisicao = requisicaoComItens(RequisicaoStatus.COTADA, List.of(papel, caneta));
        CotacaoCompra cotacao = cotacaoComConvidados(requisicao, List.of(fornecedor1, fornecedor2));
        cotacao.adicionarProposta(propostaVencedora(cotacao, fornecedor2.getId(), papel.getId(), "16.50"));
        cotacao.adicionarProposta(propostaVencedora(cotacao, fornecedor1.getId(), caneta.getId(), "125.00"));
        cotacao.setStatus(CotacaoStatus.CONCLUIDA);

        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));
        when(cotacaoRepository.save(any(CotacaoCompra.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(comprasService.criarPedidoPorItens(
                any(UUID.class), any(), any(), any(), any(), any(), anyList(), eq(tenantId)))
                .thenAnswer(inv -> pedidoResponse(UUID.randomUUID(), "PC-2026-000001" + inv.getArgument(0)));

        List<PedidoCompraResponseDTO> pedidos = service.gerarPedidos(cotacao.getId(), tenantId);

        assertEquals(2, pedidos.size());
        assertTrue(Boolean.TRUE.equals(cotacao.getPedidoGerado()));
        verify(cotacaoRepository).save(cotacao);

        PedidoCompraItemDTO itemPedido = argumentCaptorPedidoItem();
        assertEquals(caneta.getId(), itemPedido.materialId());
        assertEquals(new BigDecimal("20.000"), itemPedido.quantidade());
        assertEquals(0, itemPedido.valorUnitario().compareTo(new BigDecimal("125.00")));
    }

    @Test
    @DisplayName("gerarPedidos rejeita cotação em andamento")
    void gerarPedidos_emAndamento() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.EM_ABERTO);
        CotacaoCompra cotacao = cotacaoBase(requisicao, CotacaoStatus.EM_ANDAMENTO);
        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));

        assertThrows(IllegalArgumentException.class, () -> service.gerarPedidos(cotacao.getId(), tenantId));
    }

    @Test
    @DisplayName("gerarPedidos rejeita cotação com pedido já gerado")
    void gerarPedidos_jaGerado() {
        RequisicaoCompra requisicao = requisicao(RequisicaoStatus.COTADA);
        CotacaoCompra cotacao = cotacaoBase(requisicao, CotacaoStatus.CONCLUIDA);
        cotacao.setPedidoGerado(Boolean.TRUE);
        when(cotacaoRepository.findByIdAndTenantId(cotacao.getId(), tenantId))
                .thenReturn(Optional.of(cotacao));

        assertThrows(IllegalArgumentException.class, () -> service.gerarPedidos(cotacao.getId(), tenantId));
    }

    // ============================ AUXILIARES ============================

    private RequisicaoCompra requisicao(RequisicaoStatus status) {
        return requisicaoComItens(status, List.of(papel, caneta));
    }

    private RequisicaoCompra requisicaoComItens(RequisicaoStatus status, List<Material> materiais) {
        RequisicaoCompra requisicao = RequisicaoCompra.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .numero("RC-2026-000001")
                .solicitanteCpcId(solicitante.getCpcId())
                .justificativa("Reposição de material de escritório")
                .dataRequisicao(LocalDate.now())
                .status(status)
                .build();
        BigDecimal[] quantidades = {new BigDecimal("50.000"), new BigDecimal("20.000")};
        for (int i = 0; i < materiais.size(); i++) {
            requisicao.adicionarItem(RequisicaoCompraItem.builder()
                    .tenantId(tenantId)
                    .materialId(materiais.get(i).getId())
                    .quantidade(i < quantidades.length ? quantidades[i] : BigDecimal.ONE)
                    .build());
        }
        return requisicao;
    }

    private CotacaoCompra cotacaoBase(RequisicaoCompra requisicao, CotacaoStatus status) {
        return CotacaoCompra.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .numero("COT-2026-000001")
                .requisicao(requisicao)
                .dataLimite(LocalDate.now().plusDays(7))
                .status(status)
                .pedidoGerado(Boolean.FALSE)
                .build();
    }

    private CotacaoCompra cotacaoComConvidados(RequisicaoCompra requisicao, List<Fornecedor> convidados) {
        CotacaoCompra cotacao = cotacaoBase(requisicao, CotacaoStatus.EM_ANDAMENTO);
        convidados.forEach(f -> cotacao.adicionarFornecedor(
                CotacaoFornecedor.builder().tenantId(tenantId).fornecedor(f).build()));
        return cotacao;
    }

    private CotacaoProposta proposta(CotacaoCompra cotacao, UUID fornecedorId, UUID materialId, String valor) {
        return CotacaoProposta.builder()
                .tenantId(tenantId)
                .fornecedorId(fornecedorId)
                .materialId(materialId)
                .valorUnitario(new BigDecimal(valor))
                .vencedor(Boolean.FALSE)
                .build();
    }

    private CotacaoProposta propostaVencedora(CotacaoCompra cotacao, UUID fornecedorId, UUID materialId, String valor) {
        return CotacaoProposta.builder()
                .tenantId(tenantId)
                .fornecedorId(fornecedorId)
                .materialId(materialId)
                .valorUnitario(new BigDecimal(valor))
                .vencedor(Boolean.TRUE)
                .build();
    }

    private PedidoCompraResponseDTO pedidoResponse(UUID id, String numero) {
        return new PedidoCompraResponseDTO(
                id, tenantId, numero, fornecedor1.getId(), "FORNECEDOR", "objeto",
                LocalDate.now(), null, null, null, BigDecimal.ONE, PedidoCompraStatus.EMITIDO.name(),
                null, List.of(), null);
    }

    private RequisicaoCompra argumentCaptorRequisicao() {
        var captor = org.mockito.ArgumentCaptor.forClass(RequisicaoCompra.class);
        verify(requisicaoRepository).save(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private PedidoCompraItemDTO argumentCaptorPedidoItem() {
        var captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(comprasService, times(2)).criarPedidoPorItens(
                any(UUID.class), any(), any(), any(), any(), any(), captor.capture(), eq(tenantId));
        List<?> todos = captor.getAllValues();
        return ((List<PedidoCompraItemDTO>) todos.get(todos.size() - 1)).get(0);
    }
}