package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.EntradaNfe;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraItem;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraStatus;
import br.com.jess.chronos.pulse.modules.compras.repository.EntradaNfeRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.FornecedorRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.PedidoCompraItemRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.PedidoCompraRepository;
import br.com.jess.chronos.pulse.modules.compras.web.dto.AtualizarFornecedorDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarFornecedorDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.CadastrarPedidoCompraDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.ItemNfeDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraItemDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.PedidoCompraResponseDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.ReceberNfeDTO;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.estoque.service.EstoqueMovimentacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComprasServiceTest {

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private PedidoCompraRepository pedidoRepository;

    @Mock
    private PedidoCompraItemRepository pedidoItemRepository;

    @Mock
    private EntradaNfeRepository entradaNfeRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private EstoqueMovimentacaoService estoqueMovimentacaoService;

    @Mock
    private ContratoRepositoryPort contratoRepositoryPort;

    @InjectMocks
    private ComprasService comprasService;

    private UUID tenantId;
    private UUID almoxarifadoId;
    private Material material;
    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        almoxarifadoId = UUID.randomUUID();

        material = Material.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .descricao("Papel A4 Sulfite 75g")
                .unidadeMedida("RESMA")
                .build();

        fornecedor = Fornecedor.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .cnpj("11222333000181")
                .razaoSocial("PAPELARIA E INFORMATICA LTDA")
                .ativo(true)
                .build();
    }

    private String gerarChaveNfeValida() {
        String base = "352409" + "0".repeat(37);
        int soma = 0;
        int peso = 2;
        for (int i = base.length() - 1; i >= 0; i--) {
            soma += (base.charAt(i) - '0') * peso;
            peso++;
            if (peso > 9) peso = 2;
        }
        int resto = soma % 11;
        int dv = resto < 2 ? 0 : 11 - resto;
        return base + dv;
    }

    private PedidoCompra pedidoComItem(BigDecimal quantidade, BigDecimal recebido, BigDecimal valorUnitario) {
        PedidoCompraItem item = PedidoCompraItem.builder()
                .tenantId(tenantId)
                .materialId(material.getId())
                .quantidade(quantidade)
                .valorUnitario(valorUnitario)
                .quantidadeRecebida(recebido)
                .build();

        PedidoCompra pedido = PedidoCompra.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .numero("PC-2026-000001")
                .fornecedor(fornecedor)
                .dataEmissao(LocalDate.now())
                .valorTotal(quantidade.multiply(valorUnitario))
                .status(PedidoCompraStatus.EMITIDO)
                .build();
        pedido.adicionarItem(item);
        return pedido;
    }

    // ============================ CHAVE NFE ============================

    @Test
    @DisplayName("Deve aceitar chave NFe válida (dígito verificador confere)")
    void deveValidarChaveNfeCorreta() {
        assertTrue(ComprasService.validarChaveNfe(gerarChaveNfeValida()));
    }

    @Test
    @DisplayName("Deve rejeitar chave NFe com dígito verificador incorreto ou tamanho inválido")
    void deveRejeitarChaveNfeInvalida() {
        String valida = gerarChaveNfeValida();
        char dvTroca = valida.charAt(43) == '0' ? '1' : '0';
        String corrompida = valida.substring(0, 43) + dvTroca;

        assertFalse(ComprasService.validarChaveNfe(corrompida));
        assertFalse(ComprasService.validarChaveNfe(valida.substring(0, 20)));
        assertFalse(ComprasService.validarChaveNfe(null));
    }

    // ============================ FORNECEDORES ============================

    @Test
    @DisplayName("Deve cadastrar fornecedor com CNPJ válido normalizado")
    void deveCadastrarFornecedorComCnpjValido() {
        CadastrarFornecedorDTO dto = new CadastrarFornecedorDTO(
                "11.222.333/0001-81", "PAPELARIA E INFORMATICA LTDA", "PapelCenter",
                null, "contato@papelcenter.com.br", "(11) 3456-7890",
                "Av. Central", "1200", "Centro", "São Paulo", "SP", "01310100", null);

        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(inv -> inv.getArgument(0));

        var resposta = comprasService.criarFornecedor(dto, tenantId);

        assertEquals("11222333000181", resposta.cnpj());
        assertEquals("PAPELARIA E INFORMATICA LTDA", resposta.razaoSocial());
        verify(fornecedorRepository).findByTenantIdAndCnpj(tenantId, "11222333000181");
    }

    @Test
    @DisplayName("Deve rejeitar cadastro de fornecedor com CNPJ inválido")
    void deveRejeitarCnpjInvalido() {
        CadastrarFornecedorDTO dto = new CadastrarFornecedorDTO(
                "11222333000182", "FORNECEDOR INVALIDO LTDA", null, null, null, null,
                null, null, null, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> comprasService.criarFornecedor(dto, tenantId));
        verify(fornecedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar CNPJ duplicado no mesmo tenant")
    void deveRejeitarCnpjDuplicado() {
        CadastrarFornecedorDTO dto = new CadastrarFornecedorDTO(
                "11222333000181", "PAPELARIA E INFORMATICA LTDA", null, null, null, null,
                null, null, null, null, null, null, null);

        when(fornecedorRepository.findByTenantIdAndCnpj(tenantId, "11222333000181"))
                .thenReturn(Optional.of(fornecedor));

        assertThrows(IllegalArgumentException.class, () -> comprasService.criarFornecedor(dto, tenantId));
        verify(fornecedorRepository, never()).save(any());
    }

    // ============================ PEDIDOS ============================

    @Test
    @DisplayName("Deve criar pedido de compra com numeração sequencial e itens")
    void deveCriarPedidoDeCompra() {
        CadastrarPedidoCompraDTO dto = new CadastrarPedidoCompraDTO(
                fornecedor.getId(), "Papel A4 e material de escritório",
                LocalDate.now().plusDays(30), null, null, null,
                List.of(new PedidoCompraItemDTO(
                        material.getId(), new BigDecimal("100.000"), new BigDecimal("17.5000"))));

        when(fornecedorRepository.findByIdAndTenantId(fornecedor.getId(), tenantId))
                .thenReturn(Optional.of(fornecedor));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(material));
        when(pedidoRepository.save(any(PedidoCompra.class))).thenAnswer(inv -> {
            PedidoCompra p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(pedidoRepository.findAllByTenantIdOrderByDataEmissaoDesc(tenantId)).thenReturn(List.of());

        PedidoCompraResponseDTO resposta = comprasService.criarPedido(dto, tenantId);

        assertEquals("PC-2026-000001", resposta.numero());
        assertEquals("EMITIDO", resposta.status());
        assertEquals(1, resposta.itens().size());
        assertEquals(new BigDecimal("100.000"), resposta.itens().get(0).quantidade());
        assertEquals(new BigDecimal("1750.00"), resposta.valorTotal());
    }

    @Test
    @DisplayName("Deve rejeitar pedido com material inexistente")
    void deveRejeitarPedidoComMaterialInexistente() {
        CadastrarPedidoCompraDTO dto = new CadastrarPedidoCompraDTO(
                fornecedor.getId(), "Item inválido", null, null, null, null,
                List.of(new PedidoCompraItemDTO(
                        UUID.randomUUID(), new BigDecimal("5.000"), new BigDecimal("10.0000"))));

        when(fornecedorRepository.findByIdAndTenantId(fornecedor.getId(), tenantId))
                .thenReturn(Optional.of(fornecedor));
        when(materialRepository.findAllByTenantId(tenantId)).thenReturn(List.of(material));

        assertThrows(IllegalArgumentException.class, () -> comprasService.criarPedido(dto, tenantId));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve cancelar pedido somente enquanto não recebido")
    void deveCancelarPedidoNaoRecebido() {
        PedidoCompra pedido = pedidoComItem(new BigDecimal("100.000"), BigDecimal.ZERO,
                new BigDecimal("17.5000"));
        pedido.setId(UUID.randomUUID());
        pedido.setStatus(PedidoCompraStatus.RECEBIDO);

        when(pedidoRepository.findByIdAndTenantId(pedido.getId(), tenantId))
                .thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> comprasService.cancelarPedido(pedido.getId(), tenantId));

        pedido.setStatus(PedidoCompraStatus.EMITIDO);
        comprasService.cancelarPedido(pedido.getId(), tenantId);
        assertEquals(PedidoCompraStatus.CANCELADO, pedido.getStatus());
    }

    // ============================ RECEBIMENTO NFE ============================

    @Test
    @DisplayName("Deve receber NFe registrando entrada no estoque e marcando pedido como recebido")
    void deveReceberNfeComSucesso() {
        PedidoCompra pedido = pedidoComItem(new BigDecimal("100.000"), BigDecimal.ZERO,
                new BigDecimal("17.5000"));
        when(pedidoRepository.findByIdAndTenantId(pedido.getId(), tenantId))
                .thenReturn(Optional.of(pedido));
        when(entradaNfeRepository.save(any(EntradaNfe.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceberNfeDTO dto = new ReceberNfeDTO(
                gerarChaveNfeValida(), "1001", "1", LocalDate.now(),
                new BigDecimal("1750.00"), fornecedor.getId(), pedido.getId(),
                almoxarifadoId, "DEFINITIVO", null, null, null, null, null,
                List.of(new ItemNfeDTO(material.getId(), new BigDecimal("100.000"))));

        var resposta = comprasService.receberNfe(dto, tenantId, UUID.randomUUID());

        assertEquals(PedidoCompraStatus.RECEBIDO, pedido.getStatus());
        assertEquals(gerarChaveNfeValida(), resposta.chaveNfe());
        verify(estoqueMovimentacaoService).registrarEntrada(any(), eq(tenantId), any());
        verify(pedidoRepository).save(pedido);
        ArgumentCaptor<EntradaNfe> captor = ArgumentCaptor.forClass(EntradaNfe.class);
        verify(entradaNfeRepository).save(captor.capture());
        assertEquals("PC-2026-000001", resposta.pedidoNumero());
    }

    @Test
    @DisplayName("Deve rejeitar NFe com chave inválida")
    void deveRejeitarNfeComChaveInvalida() {
        ReceberNfeDTO dto = new ReceberNfeDTO(
                "123", "1001", "1", LocalDate.now(), null, fornecedor.getId(),
                UUID.randomUUID(), almoxarifadoId, "DEFINITIVO", null, null, null, null, null,
                List.of(new ItemNfeDTO(material.getId(), new BigDecimal("10.000"))));

        assertThrows(IllegalArgumentException.class, () -> comprasService.receberNfe(dto, tenantId, UUID.randomUUID()));
        verify(estoqueMovimentacaoService, never()).registrarEntrada(any(), any(), any());
    }

    @Test
    @DisplayName("Deve rejeitar NFe de chave já registrada no tenant")
    void deveRejeitarChaveNfeDuplicada() {
        when(entradaNfeRepository.findByTenantIdAndChaveNfe(tenantId, gerarChaveNfeValida()))
                .thenReturn(Optional.of(EntradaNfe.builder().build()));

        ReceberNfeDTO dto = new ReceberNfeDTO(
                gerarChaveNfeValida(), "1001", "1", LocalDate.now(), null, fornecedor.getId(),
                UUID.randomUUID(), almoxarifadoId, "DEFINITIVO", null, null, null, null, null,
                List.of(new ItemNfeDTO(material.getId(), new BigDecimal("10.000"))));

        assertThrows(IllegalArgumentException.class, () -> comprasService.receberNfe(dto, tenantId, UUID.randomUUID()));
        verify(estoqueMovimentacaoService, never()).registrarEntrada(any(), any(), any());
    }

    @Test
    @DisplayName("Deve rejeitar recebimento que excede o saldo do item do pedido")
    void deveRejeitarQuantidadeAcimaDoPedido() {
        PedidoCompra pedido = pedidoComItem(new BigDecimal("100.000"), new BigDecimal("90.000"),
                new BigDecimal("17.5000"));
        when(pedidoRepository.findByIdAndTenantId(pedido.getId(), tenantId))
                .thenReturn(Optional.of(pedido));

        ReceberNfeDTO dto = new ReceberNfeDTO(
                gerarChaveNfeValida(), "1001", "1", LocalDate.now(), null, fornecedor.getId(),
                pedido.getId(), almoxarifadoId, "DEFINITIVO", null, null, null, null, null,
                List.of(new ItemNfeDTO(material.getId(), new BigDecimal("20.000"))));

        assertThrows(IllegalArgumentException.class, () -> comprasService.receberNfe(dto, tenantId, UUID.randomUUID()));
        verify(estoqueMovimentacaoService, never()).registrarEntrada(any(), any(), any());
    }

    @Test
    @DisplayName("Deve liquidar contrato até o limite do empenhado ao receber NFe vinculada")
    void deveLiquidarContratoAoReceberNfeVinculada() {
        PedidoCompra pedido1 = pedidoComItem(new BigDecimal("100.000"), BigDecimal.ZERO,
                new BigDecimal("17.5000"));
        Contrato contrato = new Contrato(
                UUID.randomUUID(), tenantId, "CT-2026-001", "Fornecimento de papel", null, null,
                BigDecimal.ZERO, new BigDecimal("2000.00"), "ATIVO", null,
                new BigDecimal("2000.00"), BigDecimal.ZERO, "EMP-2026-001", 30);
        pedido1.setContratoId(contrato.getId());
        pedido1.setEmpenhoNumero("EMP-2026-001");

        when(pedidoRepository.findByIdAndTenantId(pedido1.getId(), tenantId))
                .thenReturn(Optional.of(pedido1));
        when(contratoRepositoryPort.buscarPorId(contrato.getId())).thenReturn(Optional.of(contrato));
        when(entradaNfeRepository.save(any(EntradaNfe.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceberNfeDTO dto = new ReceberNfeDTO(
                gerarChaveNfeValida(), "1001", "1", LocalDate.now(),
                new BigDecimal("1750.00"), fornecedor.getId(), pedido1.getId(),
                almoxarifadoId, "DEFINITIVO", null, null, null, null, null,
                List.of(new ItemNfeDTO(material.getId(), new BigDecimal("100.000"))));

        comprasService.receberNfe(dto, tenantId, UUID.randomUUID());

        assertEquals(new BigDecimal("1750.00"), contrato.getValorLiquidado());
        verify(contratoRepositoryPort).salvar(contrato);
    }

    @Test
    @DisplayName("Deve bloquear liquidação que exceda o valor empenhado do contrato")
    void deveBloquearLiquidacaoAcimaDoEmpenhado() {
        PedidoCompra pedido1 = pedidoComItem(new BigDecimal("100.000"), BigDecimal.ZERO,
                new BigDecimal("17.5000"));
        Contrato contrato = new Contrato(
                UUID.randomUUID(), tenantId, "CT-2026-001", "Fornecimento de papel", null, null,
                BigDecimal.ZERO, new BigDecimal("1500.00"), "ATIVO", null,
                new BigDecimal("1500.00"), BigDecimal.ZERO, "EMP-2026-001", 30);
        pedido1.setContratoId(contrato.getId());

        when(pedidoRepository.findByIdAndTenantId(pedido1.getId(), tenantId))
                .thenReturn(Optional.of(pedido1));
        when(contratoRepositoryPort.buscarPorId(contrato.getId())).thenReturn(Optional.of(contrato));

        ReceberNfeDTO dto = new ReceberNfeDTO(
                gerarChaveNfeValida(), "1001", "1", LocalDate.now(),
                new BigDecimal("1750.00"), fornecedor.getId(), pedido1.getId(),
                almoxarifadoId, "DEFINITIVO", null, null, null, null, null,
                List.of(new ItemNfeDTO(material.getId(), new BigDecimal("100.000"))));

        assertThrows(IllegalArgumentException.class, () ->
                comprasService.receberNfe(dto, tenantId, UUID.randomUUID()));
        verify(contratoRepositoryPort, never()).salvar(any());
    }

    // ============================ ATUALIZAR FORNECEDOR ============================

    @Test
    @DisplayName("Deve atualizar fornecedor preservando dados não informados")
    void deveAtualizarFornecedorParcialmente() {
        when(fornecedorRepository.findByIdAndTenantId(fornecedor.getId(), tenantId))
                .thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarFornecedorDTO dto = new AtualizarFornecedorDTO(
                null, "PAPELARIA NOVA LTDA", null, null, null, null,
                null, null, null, null, null, null, null, null);

        var resposta = comprasService.atualizarFornecedor(fornecedor.getId(), dto, tenantId);

        assertEquals("PAPELARIA NOVA LTDA", resposta.razaoSocial());
        assertEquals("11222333000181", resposta.cnpj());
    }
}