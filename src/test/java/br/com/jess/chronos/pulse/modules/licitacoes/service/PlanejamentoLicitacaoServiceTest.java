package br.com.jess.chronos.pulse.modules.licitacoes.service;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.*;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoEditalRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoEtpRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.repository.LicitacaoTrRepository;
import br.com.jess.chronos.pulse.modules.licitacoes.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanejamentoLicitacaoServiceTest {

    @Mock
    private LicitacaoRepository licitacaoRepository;

    @Mock
    private LicitacaoEtpRepository etpRepository;

    @Mock
    private LicitacaoTrRepository trRepository;

    @Mock
    private LicitacaoEditalRepository editalRepository;

    @InjectMocks
    private PlanejamentoLicitacaoService service;

    private UUID tenantId;
    private Licitacao licitacao;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        licitacao = Licitacao.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .numero("LIC-2026-000001")
                .modalidade(LicitacaoModalidade.PREGAO)
                .tipoJulgamento(LicitacaoTipoJulgamento.MENOR_PRECO)
                .objeto("Aquisição de material de escritório")
                .status(LicitacaoStatus.EM_ELABORACAO)
                .build();
    }

    @Test
    @DisplayName("Deve salvar ETP como rascunho com os dados informados")
    void deveSalvarEtpComoRascunho() {
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        guardarEtpSalvo();

        EtpDTO dto = new EtpDTO("Papel A4 e canetas", "Necessidade administrativa", "Produtos nacionais",
                "Locação de impressora", new BigDecimal("3850.00"), "Risco de entrega", "Viável por pregão");

        PlanejamentoLicitacaoResponseDTO resposta = service.salvarEtp(licitacao.getId(), dto, tenantId);

        assertNotNull(resposta.etp());
        assertEquals("RASCUNHO", resposta.etp().status());
        assertEquals("Papel A4 e canetas", resposta.etp().objeto());
        assertEquals(new BigDecimal("3850.00"), resposta.etp().valorEstimado());
    }

    @Test
    @DisplayName("Deve atualizar ETP ainda em rascunho sem duplicar o documento")
    void deveAtualizarEtpEmRascunho() {
        LicitacaoEtp etp = etpBase(EtpStatus.RASCUNHO);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(etpRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(etp));
        guardarEtpSalvo();

        EtpDTO dto = new EtpDTO("Novo objeto", "Nova justificativa", "Novos requisitos",
                null, null, null, "Conclusão revisada");

        PlanejamentoLicitacaoResponseDTO resposta = service.salvarEtp(licitacao.getId(), dto, tenantId);

        assertEquals(etp.getId(), resposta.etp().id());
        assertEquals("Novo objeto", resposta.etp().objeto());
        assertEquals("Conclusão revisada", resposta.etp().conclusao());
        assertEquals("RASCUNHO", resposta.etp().status());
    }

    @Test
    @DisplayName("Deve rejeitar alteração de ETP já aprovado")
    void deveRejeitarEdicaoDeEtpAprovado() {
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(etpRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(etpBase(EtpStatus.APROVADO)));

        EtpDTO dto = new EtpDTO("Novo objeto", "Justificativa", "Requisitos", null, null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvarEtp(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("já aprovado"));
    }

    @Test
    @DisplayName("Deve aprovar ETP registrando responsável e data de aprovação")
    void deveAprovarEtp() {
        LicitacaoEtp etp = etpBase(EtpStatus.RASCUNHO);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(etpRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(etp));
        guardarEtpSalvo();

        PlanejamentoLicitacaoResponseDTO resposta =
                service.aprovarEtp(licitacao.getId(), new AprovacaoDocumentoDTO("Comissão de Contratação"), tenantId);

        assertEquals("APROVADO", resposta.etp().status());
        assertEquals("Comissão de Contratação", resposta.etp().responsavel());
        assertNotNull(resposta.etp().dataAprovacao());
    }

    @Test
    @DisplayName("Deve rejeitar aprovação de ETP inexistente")
    void deveRejeitarAprovacaoSemEtp() {
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.aprovarEtp(licitacao.getId(), new AprovacaoDocumentoDTO("Gestor"), tenantId));
        assertTrue(ex.getMessage().contains("Elabore o ETP"));
    }

    @Test
    @DisplayName("Deve exigir ETP aprovado antes de elaborar o Termo de Referência")
    void deveExigirEtpAprovadoParaTr() {
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(etpRepository.findByLicitacaoId(licitacao.getId()))
                .thenReturn(Optional.of(etpBase(EtpStatus.RASCUNHO)));

        TrDTO dto = trValido();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvarTr(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("Aprove o ETP"));
    }

    @Test
    @DisplayName("Deve salvar e aprovar Termo de Referência vinculado ao ETP aprovado")
    void deveSalvarEAprovarTr() {
        LicitacaoEtp etp = etpBase(EtpStatus.APROVADO);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(etpRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(etp));
        guardarTrSalvo();

        PlanejamentoLicitacaoResponseDTO salvo =
                service.salvarTr(licitacao.getId(), trValido(), tenantId);

        assertEquals("RASCUNHO", salvo.tr().status());
        assertEquals("Especificações técnicas", salvo.tr().especificacoes());

        PlanejamentoLicitacaoResponseDTO aprovado =
                service.aprovarTr(licitacao.getId(), new AprovacaoDocumentoDTO("Comissão"), tenantId);
        assertEquals("APROVADO", aprovado.tr().status());
        assertNotNull(aprovado.tr().dataAprovacao());
    }

    @Test
    @DisplayName("Deve rejeitar alteração de Termo de Referência já aprovado")
    void deveRejeitarEdicaoDeTrAprovado() {
        LicitacaoEtp etp = etpBase(EtpStatus.APROVADO);
        LicitacaoTr tr = trBase(TrStatus.APROVADO, etp);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(etpRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(etp));
        when(trRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(tr));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvarTr(licitacao.getId(), trValido(), tenantId));
        assertTrue(ex.getMessage().contains("já aprovado não pode ser alterado"));
    }

    @Test
    @DisplayName("Deve exigir Termo de Referência aprovado antes de elaborar o edital")
    void deveExigirTrAprovadoParaEdital() {
        LicitacaoEtp etp = etpBase(EtpStatus.APROVADO);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(trRepository.findByLicitacaoId(licitacao.getId()))
                .thenReturn(Optional.of(trBase(TrStatus.RASCUNHO, etp)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvarEdital(licitacao.getId(), editalValido(), tenantId));
        assertTrue(ex.getMessage().contains("Aprove o Termo de Referência"));
    }

    @Test
    @DisplayName("Deve elaborar e publicar edital vinculado ao TR aprovado")
    void deveSalvarEPublicarEdital() {
        LicitacaoEtp etp = etpBase(EtpStatus.APROVADO);
        LicitacaoTr tr = trBase(TrStatus.APROVADO, etp);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(etpRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(etp));
        when(trRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(tr));
        guardarEditalSalvo();

        PlanejamentoLicitacaoResponseDTO salvo =
                service.salvarEdital(licitacao.getId(), editalValido(), tenantId);

        assertEquals("EM_ELABORACAO", salvo.edital().status());
        assertEquals("PA-2026-0001", salvo.edital().numeroProcesso());
        assertEquals("ELETRONICA", salvo.edital().formaEntregaPropostas());

        PlanejamentoLicitacaoResponseDTO publicado = service.publicarEdital(licitacao.getId(), tenantId);
        assertEquals("PUBLICADO", publicado.edital().status());
        assertNotNull(publicado.edital().dataPublicacao());
    }

    @Test
    @DisplayName("Deve rejeitar segunda publicação do edital")
    void deveRejeitarSegundaPublicacaoDeEdital() {
        LicitacaoEtp etp = etpBase(EtpStatus.APROVADO);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(editalRepository.findByLicitacaoId(licitacao.getId()))
                .thenReturn(Optional.of(editalBase(EditalStatus.PUBLICADO, trBase(TrStatus.APROVADO, etp))));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publicarEdital(licitacao.getId(), tenantId));
        assertTrue(ex.getMessage().contains("já publicado"));
    }

    @Test
    @DisplayName("Deve rejeitar planejamento para licitação fora de elaboração")
    void deveRejeitarPlanejamentoForaDeElaboracao() {
        licitacao.setStatus(LicitacaoStatus.PUBLICADA);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));

        EtpDTO dto = new EtpDTO("Objeto", "Justificativa", "Requisitos", null, null, null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvarEtp(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("em elaboração"));
    }

    @Test
    @DisplayName("Deve retornar planejamento completo ao buscar licitação com documentos")
    void deveBuscarPlanejamentoCompleto() {
        LicitacaoEtp etp = etpBase(EtpStatus.APROVADO);
        LicitacaoTr tr = trBase(TrStatus.APROVADO, etp);
        LicitacaoEdital edital = editalBase(EditalStatus.PUBLICADO, tr);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(etpRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(etp));
        when(trRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(tr));
        when(editalRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(edital));

        PlanejamentoLicitacaoResponseDTO resposta = service.buscarPlanejamento(licitacao.getId(), tenantId);

        assertEquals(licitacao.getId(), resposta.licitacaoId());
        assertEquals("LIC-2026-000001", resposta.numero());
        assertNotNull(resposta.etp());
        assertNotNull(resposta.tr());
        assertNotNull(resposta.edital());
        assertEquals("APROVADO", resposta.etp().status());
        assertEquals("APROVADO", resposta.tr().status());
        assertEquals("PUBLICADO", resposta.edital().status());
    }

    @Test
    @DisplayName("Deve retornar documentos nulos quando o planejamento não existe")
    void deveRetornarVazioSemDocumentos() {
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));

        PlanejamentoLicitacaoResponseDTO resposta = service.buscarPlanejamento(licitacao.getId(), tenantId);

        assertNull(resposta.etp());
        assertNull(resposta.tr());
        assertNull(resposta.edital());
    }

    @Test
    @DisplayName("Deve rejeitar forma de entrega de propostas inválida no edital")
    void deveRejeitarFormaEntregaInvalida() {
        LicitacaoEtp etp = etpBase(EtpStatus.APROVADO);
        when(licitacaoRepository.findByIdAndTenantId(licitacao.getId(), tenantId))
                .thenReturn(Optional.of(licitacao));
        when(trRepository.findByLicitacaoId(licitacao.getId()))
                .thenReturn(Optional.of(trBase(TrStatus.APROVADO, etp)));

        EditalDTO dto = new EditalDTO("PA-1", "ED-1", "Local", LocalDate.now(), null, "CORREIO", null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.salvarEdital(licitacao.getId(), dto, tenantId));
        assertTrue(ex.getMessage().contains("Forma de entrega"));
    }

    // ============================ FIXTURES ============================

    private LicitacaoEtp etpBase(EtpStatus status) {
        return LicitacaoEtp.builder()
                .id(UUID.randomUUID())
                .licitacao(licitacao)
                .tenantId(tenantId)
                .objeto("Objeto do ETP")
                .justificativa("Justificativa do ETP")
                .requisitos("Requisitos do ETP")
                .status(status)
                .build();
    }

    private LicitacaoTr trBase(TrStatus status, LicitacaoEtp etp) {
        return LicitacaoTr.builder()
                .id(UUID.randomUUID())
                .licitacao(licitacao)
                .etp(etp)
                .tenantId(tenantId)
                .especificacoes("Especificações técnicas")
                .condicoesFornecimento("Entrega em até 10 dias úteis")
                .obrigacoes("Substituir produtos com defeito")
                .criteriosAceitacao("Conferência qualitativa")
                .prazosEntrega("Lotes parcelados")
                .status(status)
                .build();
    }

    private LicitacaoEdital editalBase(EditalStatus status, LicitacaoTr tr) {
        return LicitacaoEdital.builder()
                .id(UUID.randomUUID())
                .licitacao(licitacao)
                .tr(tr)
                .tenantId(tenantId)
                .numeroProcesso("PA-2026-0001")
                .numeroEdital("ED-2026-0001")
                .localSessao("Câmara Municipal")
                .dataAberturaSessao(LocalDate.now().plusDays(10))
                .horarioAbertura(LocalTime.of(9, 0))
                .formaEntregaPropostas("ELETRONICA")
                .status(status)
                .build();
    }

    private TrDTO trValido() {
        return new TrDTO("Especificações técnicas", "Entrega em até 10 dias úteis",
                "Substituir produtos com defeito", "Conferência qualitativa",
                "Lotes parcelados", "Garantia de 90 dias", "Empenho + liquidação");
    }

    private EditalDTO editalValido() {
        return new EditalDTO("PA-2026-0001", "ED-2026-0001", "Câmara Municipal",
                LocalDate.now().plusDays(10), LocalTime.of(9, 0), "ELETRONICA",
                "Anexo I - TR", null);
    }

    private void guardarEtpSalvo() {
        when(etpRepository.save(any(LicitacaoEtp.class))).thenAnswer(inv -> {
            LicitacaoEtp salvo = inv.getArgument(0);
            when(etpRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(salvo));
            return salvo;
        });
    }

    private void guardarTrSalvo() {
        when(trRepository.save(any(LicitacaoTr.class))).thenAnswer(inv -> {
            LicitacaoTr salvo = inv.getArgument(0);
            when(trRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(salvo));
            return salvo;
        });
    }

    private void guardarEditalSalvo() {
        when(editalRepository.save(any(LicitacaoEdital.class))).thenAnswer(inv -> {
            LicitacaoEdital salvo = inv.getArgument(0);
            when(editalRepository.findByLicitacaoId(licitacao.getId())).thenReturn(Optional.of(salvo));
            return salvo;
        });
    }
}