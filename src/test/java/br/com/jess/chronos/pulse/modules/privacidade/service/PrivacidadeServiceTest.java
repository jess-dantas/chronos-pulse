package br.com.jess.chronos.pulse.modules.privacidade.service;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
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
                .containsKey("texto");
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
        privacidadeService.registrarConsentimento(usuario, "1.0", true, "127.0.0.1");

        verify(consentimentoRepository).save(any());
        verify(auditoriaService).registrar(
                org.mockito.ArgumentMatchers.eq("CONSENTIMENTO_PRIVACIDADE"),
                org.mockito.ArgumentMatchers.eq("cpc_usuario"),
                org.mockito.ArgumentMatchers.eq(usuario.getId()),
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void deveRejeitarConsentimentoNaoAfirmativo() {
        assertThatThrownBy(() -> privacidadeService.registrarConsentimento(usuario, "1.0", false, "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("afirmativo");

        verify(consentimentoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarVersaoDePoliticaInvalida() {
        assertThatThrownBy(() -> privacidadeService.registrarConsentimento(usuario, "9.9", true, "127.0.0.1"))
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