package br.com.jess.chronos.pulse.modules.estoque.service;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Almoxarifado;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.EstoqueMovimentacao;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.EstoqueSaldo;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.domain.service.CalculadoraPmpService;
import br.com.jess.chronos.pulse.modules.estoque.repository.AlmoxarifadoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.EstoqueMovimentacaoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.EstoqueSaldoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.EntradaMaterialDTO;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.SaidaMaterialDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueMovimentacaoServiceTest {

    @Mock
    private EstoqueSaldoRepository saldoRepository;

    @Mock
    private EstoqueMovimentacaoRepository movimentacaoRepository;

    @Mock
    private AlmoxarifadoRepository almoxarifadoRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Spy
    private CalculadoraPmpService calculadoraPmpService = new CalculadoraPmpService();

    @InjectMocks
    private EstoqueMovimentacaoService movimentacaoService;

    private UUID tenantId;
    private UUID usuarioCpcId;
    private Almoxarifado almoxarifado;
    private Material material;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        usuarioCpcId = UUID.randomUUID();

        almoxarifado = Almoxarifado.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .nome("Almoxarifado Central")
                .build();

        material = Material.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .descricao("Papel A4")
                .unidadeMedida("RESMA")
                .estoqueMinimo(new BigDecimal("10.000"))
                .build();
    }

    @Test
    @DisplayName("Deve registrar entrada e calcular novo PMP quando já houver saldo prévio")
    void deveRegistrarEntradaComCalculoPmp() {
        when(almoxarifadoRepository.findByIdAndTenantId(almoxarifado.getId(), tenantId))
                .thenReturn(Optional.of(almoxarifado));
        when(materialRepository.findByIdAndTenantId(material.getId(), tenantId))
                .thenReturn(Optional.of(material));

        EstoqueSaldo saldoExistente = EstoqueSaldo.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .almoxarifado(almoxarifado)
                .material(material)
                .lote("LOTE-01")
                .quantidadeAtual(new BigDecimal("100.000"))
                .custoMedioUnitario(new BigDecimal("10.0000"))
                .build();

        when(saldoRepository.findByTenantIdAndAlmoxarifadoIdAndMaterialIdAndLote(
                tenantId, almoxarifado.getId(), material.getId(), "LOTE-01"))
                .thenReturn(Optional.of(saldoExistente));

        EntradaMaterialDTO entradaDTO = new EntradaMaterialDTO(
                almoxarifado.getId(),
                material.getId(),
                new BigDecimal("50.000"),
                new BigDecimal("16.0000"),
                "LOTE-01",
                LocalDate.now().plusMonths(12),
                "NF-12345"
        );

        movimentacaoService.registrarEntrada(entradaDTO, tenantId, usuarioCpcId);

        ArgumentCaptor<EstoqueSaldo> saldoCaptor = ArgumentCaptor.forClass(EstoqueSaldo.class);
        verify(saldoRepository).save(saldoCaptor.capture());
        EstoqueSaldo saldoSalvo = saldoCaptor.getValue();

        assertEquals(new BigDecimal("150.000"), saldoSalvo.getQuantidadeAtual());
        assertEquals(new BigDecimal("12.0000"), saldoSalvo.getCustoMedioUnitario());

        ArgumentCaptor<EstoqueMovimentacao> movCaptor = ArgumentCaptor.forClass(EstoqueMovimentacao.class);
        verify(movimentacaoRepository).save(movCaptor.capture());
        EstoqueMovimentacao movSalva = movCaptor.getValue();

        assertEquals("ENTRADA_NFE", movSalva.getTipoMovimento());
        assertEquals(new BigDecimal("50.000"), movSalva.getQuantidade());
        assertEquals(new BigDecimal("800.0000"), movSalva.getValorTotal());
    }

    @Test
    @DisplayName("Deve registrar saída abatendo a quantidade do saldo sem alterar o custo médio unitário")
    void deveRegistrarSaidaComSucesso() {
        when(almoxarifadoRepository.findByIdAndTenantId(almoxarifado.getId(), tenantId))
                .thenReturn(Optional.of(almoxarifado));
        when(materialRepository.findByIdAndTenantId(material.getId(), tenantId))
                .thenReturn(Optional.of(material));

        EstoqueSaldo saldoExistente = EstoqueSaldo.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .almoxarifado(almoxarifado)
                .material(material)
                .lote("LOTE-01")
                .quantidadeAtual(new BigDecimal("150.000"))
                .custoMedioUnitario(new BigDecimal("12.0000"))
                .build();

        when(saldoRepository.findByTenantIdAndAlmoxarifadoIdAndMaterialIdAndLote(
                tenantId, almoxarifado.getId(), material.getId(), "LOTE-01"))
                .thenReturn(Optional.of(saldoExistente));

        SaidaMaterialDTO saidaDTO = new SaidaMaterialDTO(
                almoxarifado.getId(),
                material.getId(),
                new BigDecimal("30.000"),
                "LOTE-01",
                "REQ-1001",
                "Atendimento Secretaria de Saúde"
        );

        movimentacaoService.registrarSaida(saidaDTO, tenantId, usuarioCpcId);

        ArgumentCaptor<EstoqueSaldo> saldoCaptor = ArgumentCaptor.forClass(EstoqueSaldo.class);
        verify(saldoRepository).save(saldoCaptor.capture());
        EstoqueSaldo saldoSalvo = saldoCaptor.getValue();

        assertEquals(new BigDecimal("120.000"), saldoSalvo.getQuantidadeAtual());
        assertEquals(new BigDecimal("12.0000"), saldoSalvo.getCustoMedioUnitario()); // Inalterado

        ArgumentCaptor<EstoqueMovimentacao> movCaptor = ArgumentCaptor.forClass(EstoqueMovimentacao.class);
        verify(movimentacaoRepository).save(movCaptor.capture());
        EstoqueMovimentacao movSalva = movCaptor.getValue();

        assertEquals("SAIDA_REQUISICAO", movSalva.getTipoMovimento());
        assertEquals(new BigDecimal("30.000"), movSalva.getQuantidade());
        assertEquals(new BigDecimal("360.0000"), movSalva.getValorTotal());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar saída com saldo insuficiente")
    void deveLancarExcecaoAoTentarSaidaComSaldoInsuficiente() {
        when(almoxarifadoRepository.findByIdAndTenantId(almoxarifado.getId(), tenantId))
                .thenReturn(Optional.of(almoxarifado));
        when(materialRepository.findByIdAndTenantId(material.getId(), tenantId))
                .thenReturn(Optional.of(material));

        EstoqueSaldo saldoExistente = EstoqueSaldo.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .almoxarifado(almoxarifado)
                .material(material)
                .lote("LOTE-01")
                .quantidadeAtual(new BigDecimal("10.000"))
                .custoMedioUnitario(new BigDecimal("12.0000"))
                .build();

        when(saldoRepository.findByTenantIdAndAlmoxarifadoIdAndMaterialIdAndLote(
                tenantId, almoxarifado.getId(), material.getId(), "LOTE-01"))
                .thenReturn(Optional.of(saldoExistente));

        SaidaMaterialDTO saidaDTO = new SaidaMaterialDTO(
                almoxarifado.getId(),
                material.getId(),
                new BigDecimal("20.000"), // Maior que saldo (10)
                "LOTE-01",
                "REQ-1002",
                "Tentativa inválida"
        );

        assertThrows(IllegalArgumentException.class, () ->
                movimentacaoService.registrarSaida(saidaDTO, tenantId, usuarioCpcId)
        );

        verify(saldoRepository, never()).save(any());
        verify(movimentacaoRepository, never()).save(any());
    }
}
