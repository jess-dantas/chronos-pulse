package br.com.jess.chronos.pulse.modules.privacidade.service;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.privacidade.repository.ConsentimentoPrivacidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrivacidadeServiceTest {

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    @Mock
    private ColaboradorRepositoryPort colaboradorRepository;

    @Mock
    private ConsentimentoPrivacidadeRepository consentimentoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ModulosPort modulosPort;

    @InjectMocks
    private PrivacidadeService privacidadeService;

    private CpcUsuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new CpcUsuario(
                UUID.randomUUID(), UUID.randomUUID(), "12345678901", "Fulano de Tal",
                "fulano@empresa.com", "hash", Role.COLABORADOR, UUID.randomUUID(),
                true, false, false, false, null);
    }

    @Test
    void deveRetornarPoliticaAtual() {
        assertThat(privacidadeService.politicaAtual())
                .containsEntry("versao", PrivacidadeService.VERSAO_POLITICA_ATUAL)
                .containsKey("texto")
                .containsKey("hashTermo");
    }

    @Test
    void statusDeveIndicarPendenteQuandoNuncaAceitou() {
        when(consentimentoRepository.findTopByCpcIdOrderByDataConsentimentoDesc(
                usuario.getCpcId())).thenReturn(Optional.empty());

        var status = privacidadeService.statusConsentimento(usuario);

        assertThat(status)
                .containsEntry("versaoAtual", PrivacidadeService.VERSAO_POLITICA_ATUAL)
                .containsEntry("versaoAceita", null)
                .containsEntry("aceitePendente", true);
    }

    @Test
    void statusDeveIndicarAceitoQuandoVersaoAtualJaFoiAceita() {
        var registro = br.com.jess.chronos.pulse.modules.privacidade.domain.ConsentimentoPrivacidade
                .builder()
                .id(UUID.randomUUID())
                .cpcId(usuario.getCpcId())
                .versaoPolitica(PrivacidadeService.VERSAO_POLITICA_ATUAL)
                .dataConsentimento(java.time.OffsetDateTime.now())
                .aceito(true)
                .build();
        when(consentimentoRepository.findTopByCpcIdOrderByDataConsentimentoDesc(
                usuario.getCpcId())).thenReturn(Optional.of(registro));

        var status = privacidadeService.statusConsentimento(usuario);

        assertThat(status)
                .containsEntry("versaoAceita", PrivacidadeService.VERSAO_POLITICA_ATUAL)
                .containsEntry("aceitePendente", false);
    }

    @Test
    void deveExportarDadosSemExporHashDeSenha() {
        when(colaboradorRepository.buscarPorCpcUsuarioId(usuario.getCpcId()))
                .thenReturn(Optional.empty());
        when(consentimentoRepository.findByCpcIdOrderByDataConsentimentoDesc(usuario.getCpcId()))
                .thenReturn(java.util.List.of());

        var dados = privacidadeService.exportarMeusDados(usuario);

        assertThat(dados).containsKey("usuario").containsKey("geradoEm");
        assertThat(dados.get("usuario").toString()).doesNotContain("senhaHash");
    }

    @Test
    void deveRegistrarConsentimento() {
        privacidadeService.registrarConsentimento(
                usuario, "1.0", true, "127.0.0.1", "Mozilla/5.0 (Flutter Test)");

        verify(consentimentoRepository).save(any());
        verify(auditoriaService).registrar(
                org.mockito.ArgumentMatchers.eq("CONSENTIMENTO_PRIVACIDADE"),
                org.mockito.ArgumentMatchers.eq("cpc_usuario"),
                org.mockito.ArgumentMatchers.eq(usuario.getId()),
                any(), any(), any(), any(), any(), any(), any(), any());
        verify(modulosPort, never()).definirModulosDoUsuario(any(), any(), any());
    }

    @Test
    void deveGravarAuditoriaReforcadaTenantUserAgentEHashDoTermo() {
        var captor = org.mockito.ArgumentCaptor.forClass(
                br.com.jess.chronos.pulse.modules.privacidade.domain.ConsentimentoPrivacidade.class);

        privacidadeService.registrarConsentimento(
                usuario, "1.0", true, "189.23.45.12", "Mozilla/5.0 (Android 14; Flutter App v1.2)");

        verify(consentimentoRepository).save(captor.capture());
        var salvo = captor.getValue();
        assertThat(salvo.getTenantId()).isEqualTo(usuario.getTenantId());
        assertThat(salvo.getUserAgent()).isEqualTo("Mozilla/5.0 (Android 14; Flutter App v1.2)");
        assertThat(salvo.getIpOrigem()).isEqualTo("189.23.45.12");
        assertThat(salvo.getHashTermo())
                .isNotNull()
                .hasSize(64)
                .isEqualTo(sha256(PrivacidadeService.TEXTO_POLITICA));
    }

    @Test
    void deveSerIdempotenteQuandoVersaoJaAceita() {
        when(consentimentoRepository.existsByCpcIdAndVersaoPoliticaAndAceitoTrue(
                usuario.getCpcId(), "1.0")).thenReturn(true);

        privacidadeService.registrarConsentimento(
                usuario, "1.0", true, "127.0.0.1", "Agent");

        verify(consentimentoRepository, never()).save(any());
        verify(auditoriaService, never()).registrar(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private static String sha256(String texto) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(texto.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void deveAssociarTodosModulosContratadosParaAdminEmpresaNoConsentimento() {
        CpcUsuario adminEmpresa = new CpcUsuario(
                UUID.randomUUID(), UUID.randomUUID(), "99988877766", "Admin Empresa",
                "admin@empresa.com", "hash", Role.ADMIN_EMPRESA, usuario.getTenantId());

        when(modulosPort.listarCodigosAtivos(adminEmpresa.getTenantId()))
                .thenReturn(java.util.List.of("PONTO", "ESTOQUE"));

        privacidadeService.registrarConsentimento(
                adminEmpresa, "1.0", true, "127.0.0.1", "Agent");

        verify(consentimentoRepository).save(any());
        verify(modulosPort).definirModulosDoUsuario(
                adminEmpresa.getId(), adminEmpresa.getTenantId(),
                java.util.List.of("PONTO", "ESTOQUE"));
    }

    @Test
    void deveRejeitarConsentimentoNaoAfirmativo() {
        assertThatThrownBy(() -> privacidadeService.registrarConsentimento(
                usuario, "1.0", false, "127.0.0.1", "Agent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("afirmativo");

        verify(consentimentoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarVersaoDePoliticaInvalida() {
        assertThatThrownBy(() -> privacidadeService.registrarConsentimento(
                usuario, "9.9", true, "127.0.0.1", "Agent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválida");
    }

    @Test
    void deveAnonimizarDadosPessoais() {
        privacidadeService.anonimizarMeusDados(usuario, "127.0.0.1");

        verify(usuarioRepository).atualizar(any(CpcUsuario.class));
        verify(auditoriaService).registrar(
                org.mockito.ArgumentMatchers.eq("EXCLUSAO_DADOS_PESSOAIS"),
                org.mockito.ArgumentMatchers.eq("cpc_usuario"),
                org.mockito.ArgumentMatchers.eq(usuario.getId()),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void anonimizacaoDeveRemoverNomeEEmailsEDesativar() {
        var anonimizado = usuario.anonimizar();

        assertThat(anonimizado.getNome()).doesNotContain("Fulano");
        assertThat(anonimizado.getEmailCorporativo()).isNull();
        assertThat(anonimizado.getEmailPessoal()).isNull();
        assertThat(anonimizado.getCelular()).isNull();
        assertThat(anonimizado.getFoto()).isNull();
        assertThat(anonimizado.isAtivo()).isFalse();
        assertThat(anonimizado.getId()).isEqualTo(usuario.getId());
        assertThat(anonimizado.getCpf()).isEqualTo(usuario.getCpf());
    }
}