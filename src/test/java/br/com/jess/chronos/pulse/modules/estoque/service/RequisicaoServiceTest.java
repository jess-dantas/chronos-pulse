package br.com.jess.chronos.pulse.modules.estoque.service;

import br.com.jess.chronos.pulse.modules.estoque.domain.entity.*;
import br.com.jess.chronos.pulse.modules.estoque.repository.AlmoxarifadoRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.MaterialRepository;
import br.com.jess.chronos.pulse.modules.estoque.repository.RequisicaoRepository;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.CriarRequisicaoDTO;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.ItemRequisicaoDTO;
import br.com.jess.chronos.pulse.modules.estoque.web.dto.RequisicaoResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequisicaoServiceTest {

    @Mock
    private RequisicaoRepository requisicaoRepository;

    @Mock
    private AlmoxarifadoRepository almoxarifadoRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private EstoqueMovimentacaoService estoqueMovimentacaoService;

    @InjectMocks
    private RequisicaoService requisicaoService;

    private UUID tenantId;
    private UUID solicitanteCpcId;
    private Almoxarifado almoxarifado;
    private Material material;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        solicitanteCpcId = UUID.randomUUID();

        almoxarifado = Almoxarifado.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .nome("Almoxarifado Central")
                .build();

        material = Material.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .descricao("Caneta Esferográfica")
                .unidadeMedida("UN")
                .build();
    }

    @Test
    @DisplayName("Deve criar requisição com status inicial PENDENTE")
    void deveCriarRequisicaoComSucesso() {
        when(almoxarifadoRepository.findByIdAndTenantId(almoxarifado.getId(), tenantId))
                .thenReturn(Optional.of(almoxarifado));
        when(materialRepository.findByIdAndTenantId(material.getId(), tenantId))
                .thenReturn(Optional.of(material));

        when(requisicaoRepository.save(any(Requisicao.class))).thenAnswer(invocation -> {
            Requisicao req = invocation.getArgument(0);
            req.setId(UUID.randomUUID());
            return req;
        });

        CriarRequisicaoDTO dto = new CriarRequisicaoDTO(
                almoxarifado.getId(),
                "Secretaria de Educação",
                "Materiais para início das aulas",
                List.of(new ItemRequisicaoDTO(material.getId(), new BigDecimal("50.000")))
        );

        RequisicaoResponseDTO response = requisicaoService.criarRequisicao(dto, tenantId, solicitanteCpcId);

        assertNotNull(response.id());
        assertEquals(RequisicaoStatus.PENDENTE, response.status());
        assertEquals("Secretaria de Educação", response.departamento());
        assertEquals(1, response.itens().size());
        assertEquals(new BigDecimal("50.000"), response.itens().get(0).quantidadeSolicitada());
    }

    @Test
    @DisplayName("Deve aprovar requisição pendente com sucesso")
    void deveAprovarRequisicao() {
        UUID reqId = UUID.randomUUID();
        Requisicao requisicao = Requisicao.builder()
                .id(reqId)
                .tenantId(tenantId)
                .almoxarifado(almoxarifado)
                .solicitanteCpcId(solicitanteCpcId)
                .status(RequisicaoStatus.PENDENTE)
                .build();

        when(requisicaoRepository.findByIdAndTenantId(reqId, tenantId))
                .thenReturn(Optional.of(requisicao));
        when(requisicaoRepository.save(any(Requisicao.class))).thenAnswer(i -> i.getArgument(0));

        RequisicaoResponseDTO response = requisicaoService.aprovarRequisicao(reqId, tenantId);

        assertEquals(RequisicaoStatus.APROVADA, response.status());
    }

    @Test
    @DisplayName("Deve atender requisição disparando a baixa de estoque dos itens")
    void deveAtenderRequisicaoComBaixaEstoque() {
        UUID reqId = UUID.randomUUID();
        UUID atendenteCpcId = UUID.randomUUID();

        Requisicao requisicao = Requisicao.builder()
                .id(reqId)
                .tenantId(tenantId)
                .almoxarifado(almoxarifado)
                .solicitanteCpcId(solicitanteCpcId)
                .status(RequisicaoStatus.APROVADA)
                .build();

        RequisicaoItem item = RequisicaoItem.builder()
                .id(UUID.randomUUID())
                .material(material)
                .quantidadeSolicitada(new BigDecimal("20.000"))
                .build();
        requisicao.adicionarItem(item);

        when(requisicaoRepository.findByIdAndTenantId(reqId, tenantId))
                .thenReturn(Optional.of(requisicao));
        when(requisicaoRepository.save(any(Requisicao.class))).thenAnswer(i -> i.getArgument(0));

        RequisicaoResponseDTO response = requisicaoService.atenderRequisicao(reqId, tenantId, atendenteCpcId);

        assertEquals(RequisicaoStatus.ATENDIDA, response.status());
        assertNotNull(response.dataAtendimento());
        assertEquals(atendenteCpcId, response.atendenteCpcId());

        verify(estoqueMovimentacaoService, times(1)).registrarSaida(any(), eq(tenantId), eq(atendenteCpcId));
    }
}
