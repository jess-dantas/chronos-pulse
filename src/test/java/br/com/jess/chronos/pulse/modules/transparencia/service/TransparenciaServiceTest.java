package br.com.jess.chronos.pulse.modules.transparencia.service;

import br.com.jess.chronos.pulse.modules.admin.domain.model.Contrato;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.ContratoRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.output.persistence.ColaboradorJpaEntity;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.output.persistence.ColaboradorJpaRepository;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.EntradaNfe;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompra;
import br.com.jess.chronos.pulse.modules.compras.domain.entity.PedidoCompraStatus;
import br.com.jess.chronos.pulse.modules.compras.repository.EntradaNfeRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.FornecedorRepository;
import br.com.jess.chronos.pulse.modules.compras.repository.PedidoCompraRepository;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.EstoqueSaldo;
import br.com.jess.chronos.pulse.modules.estoque.domain.entity.Material;
import br.com.jess.chronos.pulse.modules.estoque.repository.EstoqueSaldoRepository;
import br.com.jess.chronos.pulse.modules.frota.domain.entity.FrotaAbastecimento;
import br.com.jess.chronos.pulse.modules.frota.domain.entity.FrotaVeiculo;
import br.com.jess.chronos.pulse.modules.frota.repository.FrotaAbastecimentoRepository;
import br.com.jess.chronos.pulse.modules.frota.repository.FrotaVeiculoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.Licitacao;
import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoStatus;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence.RegistroPontoJpaEntity;
import br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence.RegistroPontoJpaRepository;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.StatusPublicacaoTransparencia;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.TipoPublicacaoTransparencia;
import br.com.jess.chronos.pulse.modules.transparencia.domain.entity.TransparenciaPublicacao;
import br.com.jess.chronos.pulse.modules.transparencia.repository.TransparenciaPublicacaoRepository;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.CriarPublicacaoDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.DespesasMensaisDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.PublicacaoResponseDTO;
import br.com.jess.chronos.pulse.modules.transparencia.web.dto.TransparenciaResumoDTO;
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
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransparenciaServiceTest {

    @Mock TransparenciaPublicacaoRepository publicacaoRepository;
    @Mock ContratoRepositoryPort contratoRepositoryPort;
    @Mock PedidoCompraRepository pedidoRepository;
    @Mock EntradaNfeRepository entradaNfeRepository;
    @Mock FornecedorRepository fornecedorRepository;
    @Mock LicitacaoRepository licitacaoRepository;
    @Mock EstoqueSaldoRepository estoqueSaldoRepository;
    @Mock PatrimonioRepository patrimonioRepository;
    @Mock FrotaVeiculoRepository frotaVeiculoRepository;
    @Mock FrotaAbastecimentoRepository abastecimentoRepository;
    @Mock ColaboradorJpaRepository colaboradorRepository;
    @Mock RegistroPontoJpaRepository registroPontoRepository;

    @InjectMocks TransparenciaService transparenciaService;

    private final UUID tenantId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    private final UUID id = UUID.randomUUID();

    private TransparenciaPublicacao publicacaoEntity(StatusPublicacaoTransparencia status) {
        return TransparenciaPublicacao.builder()
                .id(id)
                .tenantId(tenantId)
                .competencia("2026-08")
                .tipoPublicacao(TipoPublicacaoTransparencia.DESPESAS)
                .valorTotal(new BigDecimal("50000.00"))
                .itensCount(12)
                .status(status)
                .dataPublicacao(status == StatusPublicacaoTransparencia.PUBLICADO ? LocalDate.of(2026, 9, 5) : null)
                .observacoes("Fechamento fiscal")
                .criadoEm(Instant.now())
                .atualizadoEm(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Resumo agrega contratos, compras, licitações, estoque, patrimônio, frota, RH e publicações")
    void obterResumo_agregaIndicadores() {
        when(contratoRepositoryPort.listarPorTenant(tenantId)).thenReturn(List.of(
                new Contrato(UUID.randomUUID(), tenantId, "CT-001", "Aquisição papel", LocalDate.of(2026, 1, 1),
                        LocalDate.now().plusDays(10), new BigDecimal("1000.00"), new BigDecimal("12000.00"),
                        "ATIVO", null, new BigDecimal("12000.00"), new BigDecimal("6000.00"), "EMP-001", 30),
                new Contrato(UUID.randomUUID(), tenantId, "CT-002", "Serviço de TI", LocalDate.of(2026, 3, 1),
                        LocalDate.now().minusDays(2), new BigDecimal("2000.00"), new BigDecimal("18000.00"),
                        "ATIVO", null, new BigDecimal("18000.00"), new BigDecimal("15000.00"), "EMP-002", 30)
        ));
        when(fornecedorRepository.findAllByTenantIdOrderByRazaoSocial(tenantId)).thenReturn(List.of(
                Fornecedor.builder().tenantId(tenantId).cnpj("11111111000111").razaoSocial("Forne A").ativo(true).build(),
                Fornecedor.builder().tenantId(tenantId).cnpj("22222222000122").razaoSocial("Forne B").ativo(false).build()
        ));
        when(pedidoRepository.findAllByTenantIdOrderByDataEmissaoDesc(tenantId)).thenReturn(List.of(
                pedido(new BigDecimal("3000.00"), LocalDate.of(2026, 8, 10))
        ));
        when(entradaNfeRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId)).thenReturn(List.of(
                nfe(new BigDecimal("5000.00"), LocalDate.of(2026, 8, 15))
        ));
        when(licitacaoRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId)).thenReturn(List.of(
                licitacao(LicitacaoStatus.PUBLICADA, new BigDecimal("3850.00")),
                licitacao(LicitacaoStatus.ADJUDICADA, new BigDecimal("50000.00")),
                licitacao(LicitacaoStatus.CANCELADA, new BigDecimal("1000.00"))
        ));
        when(estoqueSaldoRepository.findAllByTenantId(tenantId)).thenReturn(List.of(
                saldo(Material.builder().estoqueMinimo(new BigDecimal("5")).build(), new BigDecimal("10"), new BigDecimal("17.5")),
                saldo(Material.builder().estoqueMinimo(new BigDecimal("5")).build(), new BigDecimal("2"), new BigDecimal("129.0"))
        ));
        when(patrimonioRepository.findAllByTenantId(tenantId)).thenReturn(List.of(
                patrimonio(true, new BigDecimal("10000.00"), new BigDecimal("2000.00")),
                patrimonio(true, new BigDecimal("5000.00"), BigDecimal.ZERO)
        ));
        when(frotaVeiculoRepository.findAllByTenantId(tenantId)).thenReturn(List.of(
                FrotaVeiculo.builder().ativo(true).build(),
                FrotaVeiculo.builder().ativo(false).build()
        ));
        when(abastecimentoRepository.findAllByTenantIdOrderByDataHoraDesc(tenantId))
                .thenReturn(List.of(abastecimento(OffsetDateTime.now(), new BigDecimal("300.00"))));
        when(colaboradorRepository.findByTenantId(tenantId)).thenReturn(List.of(
                colaborador(true), colaborador(true), colaborador(false)
        ));
        when(registroPontoRepository.findByTenantIdOrderByDataHoraDispositivoAsc(tenantId))
                .thenReturn(List.of(registroPonto(Instant.now()), registroPonto(Instant.now())));
        when(publicacaoRepository.countByTenantIdAndStatus(tenantId, StatusPublicacaoTransparencia.PUBLICADO))
                .thenReturn(3L);
        when(publicacaoRepository.findAllByTenantIdOrderByCompetenciaDesc(tenantId))
                .thenReturn(List.of(publicacaoEntity(StatusPublicacaoTransparencia.PUBLICADO)));

        TransparenciaResumoDTO resumo = transparenciaService.obterResumo(tenantId);

        assertEquals(2, resumo.contratos().ativos());
        assertEquals(0, new BigDecimal("30000.00").compareTo(resumo.contratos().valorEmpenhado()));
        assertEquals(0, new BigDecimal("21000.00").compareTo(resumo.contratos().valorLiquidado()));
        assertEquals(0, new BigDecimal("9000.00").compareTo(resumo.contratos().saldoTotal()));
        assertEquals(1, resumo.contratos().vencendo30Dias());
        assertEquals(1, resumo.contratos().vencidos());

        assertEquals(1, resumo.compras().fornecedoresAtivos());
        assertEquals(1, resumo.compras().pedidosEmitidos());
        assertEquals(0, new BigDecimal("3000.00").compareTo(resumo.compras().valorPedidos()));
        assertEquals(1, resumo.compras().notasFiscaisRecebidas());
        assertEquals(0, new BigDecimal("5000.00").compareTo(resumo.compras().valorNotasFiscais()));

        assertEquals(3, resumo.licitacoes().total());
        assertEquals(1, resumo.licitacoes().publicadas());
        assertEquals(1, resumo.licitacoes().adjudicadas());
        assertEquals(1, resumo.licitacoes().canceladas());
        assertEquals(0, new BigDecimal("54850.00").compareTo(resumo.licitacoes().valorEstimadoTotal()));

        assertEquals(2, resumo.estoque().itensEstoque());
        assertEquals(0, new BigDecimal("433.00").compareTo(resumo.estoque().valorTotalEstoque()));
        assertEquals(1, resumo.estoque().acimaDoMinimo());
        assertEquals(1, resumo.estoque().abaixoDoMinimo());

        assertEquals(2, resumo.patrimonio().totalBens());
        assertEquals(0, new BigDecimal("15000.00").compareTo(resumo.patrimonio().valorAquisicao()));
        assertEquals(0, new BigDecimal("13000.00").compareTo(resumo.patrimonio().valorAtual()));

        assertEquals(2, resumo.frota().veiculos());
        assertEquals(1, resumo.frota().veiculosAtivos());
        assertEquals(1, resumo.frota().abastecimentosMes());
        assertEquals(0, new BigDecimal("300.00").compareTo(resumo.frota().valorAbastecimentosMes()));

        assertEquals(3, resumo.colaboradores().total());
        assertEquals(2, resumo.colaboradores().ativos());
        assertEquals(2, resumo.ponto().registrosMes());

        assertEquals(3, resumo.publicacoes().publicadas());
        assertEquals("2026-08", resumo.publicacoes().ultimaCompetencia());
    }

    @Test
    @DisplayName("Despesas mensais consolida NFe, combustível e pedidos por mês/ano")
    void obterDespesasMensais_consolidaPorMes() {
        LocalDate marco1 = LocalDate.of(2026, 3, 5);
        LocalDate marco2 = LocalDate.of(2026, 3, 20);
        LocalDate julho = LocalDate.of(2026, 7, 10);
        when(entradaNfeRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId)).thenReturn(List.of(
                nfe(new BigDecimal("1000.00"), marco1),
                nfe(new BigDecimal("2000.00"), marco2),
                nfe(new BigDecimal("5000.00"), julho),
                nfe(new BigDecimal("999.00"), LocalDate.of(2025, 3, 1))
        ));
        when(abastecimentoRepository.findAllByTenantIdOrderByDataHoraDesc(tenantId)).thenReturn(List.of(
                abastecimento(OffsetDateTime.of(2026, 3, 8, 9, 0, 0, 0, OffsetDateTime.now().getOffset()), new BigDecimal("150.00")),
                abastecimento(OffsetDateTime.of(2026, 8, 2, 9, 0, 0, 0, OffsetDateTime.now().getOffset()), new BigDecimal("200.00"))
        ));
        when(pedidoRepository.findAllByTenantIdOrderByDataEmissaoDesc(tenantId)).thenReturn(List.of(
                pedido(new BigDecimal("3000.00"), marco1),
                pedido(new BigDecimal("1200.00"), LocalDate.of(2026, 8, 12))
        ));

        DespesasMensaisDTO dto = transparenciaService.obterDespesasMensais(tenantId, 2026);

        assertEquals(12, dto.meses().size());
        DespesasMensaisDTO.DespesaMensalDTO marco = dto.meses().get(2);
        assertEquals(2, marco.notasFiscais());
        assertEquals(0, new BigDecimal("3000.00").compareTo(marco.despesasNfe()));
        assertEquals(1, marco.abastecimentos());
        assertEquals(0, new BigDecimal("150.00").compareTo(marco.combustivel()));
        assertEquals(1, marco.quantidadePedidos());
        assertEquals(0, new BigDecimal("3000.00").compareTo(marco.pedidosEmitidos()));
        DespesasMensaisDTO.DespesaMensalDTO agosto = dto.meses().get(7);
        assertEquals(0, new BigDecimal("200.00").compareTo(agosto.combustivel()));
        assertEquals(1, agosto.quantidadePedidos());
    }

    @Test
    @DisplayName("Cria publicação em elaboração")
    void criarPublicacao_criaNova() {
        when(publicacaoRepository.findAllByTenantIdOrderByCompetenciaDesc(tenantId)).thenReturn(List.of());
        when(publicacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PublicacaoResponseDTO criada = transparenciaService.criarPublicacao(
                new CriarPublicacaoDTO("2026-09", TipoPublicacaoTransparencia.DESPESAS,
                        new BigDecimal("42000.00"), 14, "Fechamento mensal"),
                tenantId);

        assertEquals("2026-09", criada.competencia());
        assertEquals(TipoPublicacaoTransparencia.DESPESAS, criada.tipoPublicacao());
        assertEquals(StatusPublicacaoTransparencia.EM_ELABORACAO, criada.status());
        verify(publicacaoRepository).save(any());
    }

    @Test
    @DisplayName("Rejeita publicação duplicada para mesma competência/tipo")
    void criarPublicacao_duplicada_lancaErro() {
        when(publicacaoRepository.findAllByTenantIdOrderByCompetenciaDesc(tenantId))
                .thenReturn(List.of(publicacaoEntity(StatusPublicacaoTransparencia.EM_ELABORACAO)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transparenciaService.criarPublicacao(
                        new CriarPublicacaoDTO("2026-08", TipoPublicacaoTransparencia.DESPESAS,
                                new BigDecimal("50000.00"), 12, null),
                        tenantId));

        assertTrue(ex.getMessage().contains("Já existe publicação"));
        verify(publicacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Publicar divulga publicação e define data de publicação")
    void publicar_divulga() {
        when(publicacaoRepository.findByIdAndTenantId(id, tenantId))
                .thenReturn(Optional.of(publicacaoEntity(StatusPublicacaoTransparencia.EM_ELABORACAO)));
        when(publicacaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PublicacaoResponseDTO publicada = transparenciaService.publicar(id, tenantId);

        assertEquals(StatusPublicacaoTransparencia.PUBLICADO, publicada.status());
        assertEquals(LocalDate.now(), publicada.dataPublicacao());
    }

    @Test
    @DisplayName("Publicação já divulgada não pode republicar")
    void publicar_jaPublicada_lancaErro() {
        when(publicacaoRepository.findByIdAndTenantId(id, tenantId))
                .thenReturn(Optional.of(publicacaoEntity(StatusPublicacaoTransparencia.PUBLICADO)));

        assertThrows(IllegalArgumentException.class, () -> transparenciaService.publicar(id, tenantId));
    }

    @Test
    @DisplayName("Remoção permitida apenas para publicações em elaboração")
    void remover_publicado_lancaErro() {
        when(publicacaoRepository.findByIdAndTenantId(id, tenantId))
                .thenReturn(Optional.of(publicacaoEntity(StatusPublicacaoTransparencia.PUBLICADO)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transparenciaService.remover(id, tenantId));

        assertTrue(ex.getMessage().contains("não pode ser removida"));
    }

    // ---- helpers ----

    private PedidoCompra pedido(BigDecimal valor, LocalDate dataEmissao) {
        return PedidoCompra.builder()
                .tenantId(tenantId)
                .numero("PC-001")
                .dataEmissao(dataEmissao)
                .valorTotal(valor)
                .status(PedidoCompraStatus.EMITIDO)
                .build();
    }

    private EntradaNfe nfe(BigDecimal valor, LocalDate dataEmissao) {
        return EntradaNfe.builder()
                .tenantId(tenantId)
                .chaveNfe(UUID.randomUUID().toString().replace("-", ""))
                .pedidoId(UUID.randomUUID())
                .almoxarifadoId(UUID.randomUUID())
                .tipoTermo("ENTRADA")
                .dataEmissao(dataEmissao)
                .valorNota(valor)
                .build();
    }

    private Licitacao licitacao(LicitacaoStatus status, BigDecimal valorEstimado) {
        return Licitacao.builder()
                .tenantId(tenantId)
                .numero("LIC-" + UUID.randomUUID().toString().substring(0, 6))
                .modalidade(br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoModalidade.PREGAO)
                .tipoJulgamento(br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.LicitacaoTipoJulgamento.MENOR_PRECO)
                .objeto("Objeto de teste")
                .status(status)
                .valorEstimado(valorEstimado)
                .build();
    }

    private EstoqueSaldo saldo(Material material, BigDecimal qtd, BigDecimal custo) {
        return EstoqueSaldo.builder()
                .tenantId(tenantId)
                .material(material)
                .quantidadeAtual(qtd)
                .custoMedioUnitario(custo)
                .build();
    }

    private Patrimonio patrimonio(boolean ativo, BigDecimal valorAquisicao, BigDecimal valorDepreciado) {
        return Patrimonio.builder()
                .tenantId(tenantId)
                .descricao("Bem de teste")
                .ativo(ativo)
                .valorAquisicao(valorAquisicao)
                .valorDepreciado(valorDepreciado)
                .build();
    }

    private FrotaAbastecimento abastecimento(OffsetDateTime dataHora, BigDecimal valorTotal) {
        return FrotaAbastecimento.builder()
                .tenantId(tenantId)
                .dataHora(dataHora)
                .litros(new BigDecimal("10"))
                .valorLitro(new BigDecimal("5.5"))
                .valorTotal(valorTotal)
                .build();
    }

    private ColaboradorJpaEntity colaborador(boolean ativo) {
        ColaboradorJpaEntity c = new ColaboradorJpaEntity();
        c.setId(UUID.randomUUID());
        c.setTenantId(tenantId);
        c.setCpcUsuarioId(UUID.randomUUID());
        c.setDataAdmissao(LocalDate.of(2025, 1, 1));
        c.setAtivo(ativo);
        return c;
    }

    private RegistroPontoJpaEntity registroPonto(Instant dataHora) {
        RegistroPontoJpaEntity r = new RegistroPontoJpaEntity();
        r.setId(UUID.randomUUID());
        r.setTenantId(tenantId);
        r.setColaboradorId(UUID.randomUUID());
        r.setDataHoraDispositivo(dataHora);
        r.setSincronizadoOffline(true);
        return r;
    }
}