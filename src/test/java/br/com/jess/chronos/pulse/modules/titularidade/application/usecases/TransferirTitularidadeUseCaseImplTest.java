package br.com.jess.chronos.pulse.modules.titularidade.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import br.com.jess.chronos.pulse.modules.notificacao.service.EmailCodigoTitularidadeService;
import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeCodigo;
import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeTransferencia;
import br.com.jess.chronos.pulse.modules.titularidade.domain.ports.input.TransferirTitularidadeUseCase;
import br.com.jess.chronos.pulse.modules.titularidade.domain.ports.output.TitularidadeRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferirTitularidadeUseCaseImplTest {

    @Mock
    private TitularidadeRepositoryPort titularidadeRepository;

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    @Mock
    private ModulosPort modulosPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailCodigoTitularidadeService emailService;

    private TransferirTitularidadeUseCaseImpl useCase;

    private UUID tenantId;
    private UUID solicitanteId;
    private UUID novoTitularId;

    @BeforeEach
    void setUp() {
        useCase = new TransferirTitularidadeUseCaseImpl(
                titularidadeRepository, usuarioRepository, modulosPort,
                passwordEncoder, emailService);
        tenantId = UUID.randomUUID();
        solicitanteId = UUID.randomUUID();
        novoTitularId = UUID.randomUUID();
    }

    private CpcUsuario usuario(Role role) {
        CpcUsuario u = new CpcUsuario(
                role == Role.ADMIN_EMPRESA ? solicitanteId : novoTitularId,
                UUID.randomUUID(), "12345678901", "Fulano",
                "fulano@empresa.com", "hash", role, tenantId,
                true, true, true, true, null);
        return u;
    }

    private TitularidadeTransferencia transferenciaAberta() {
        TitularidadeTransferencia t = new TitularidadeTransferencia();
        t.setId(UUID.randomUUID());
        t.setTenantId(tenantId);
        t.setSolicitanteId(solicitanteId);
        t.setNovoTitularId(novoTitularId);
        t.setStatus(TitularidadeTransferencia.STATUS_EM_ANDAMENTO);
        t.setExpiraEm(Instant.now().plusSeconds(1800));
        return t;
    }

    // ---------- iniciar ----------

    @Test
    void iniciarDeveRejeitarNovoTitularIgualAoSolicitante() {
        assertThatThrownBy(() -> useCase.iniciar(
                new TransferirTitularidadeUseCase.IniciarComando(
                        solicitanteId, tenantId, solicitanteId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("diferente do titular atual");

        verify(titularidadeRepository, never()).salvar(any());
    }

    @Test
    void iniciarDeveRejeitarQuandoSolicitanteNaoEAdminEmpresa() {
        CpcUsuario gestor = new CpcUsuario(
                solicitanteId, UUID.randomUUID(), "11122233344", "Gestor",
                "gestor@empresa.com", "hash", Role.GESTOR_RH, tenantId,
                true, true, true, true, null);
        when(usuarioRepository.buscarPorId(solicitanteId)).thenReturn(Optional.of(gestor));

        assertThatThrownBy(() -> useCase.iniciar(
                new TransferirTitularidadeUseCase.IniciarComando(
                        solicitanteId, tenantId, novoTitularId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titular da empresa");

        verify(titularidadeRepository, never()).salvar(any());
    }

    @Test
    void iniciarDeveCriarTransferenciaECancelarAbertasAnteriores() {
        CpcUsuario admin = usuario(Role.ADMIN_EMPRESA);
        CpcUsuario novo = usuario(Role.COLABORADOR);
        when(usuarioRepository.buscarPorId(solicitanteId)).thenReturn(Optional.of(admin));
        when(usuarioRepository.buscarPorId(novoTitularId)).thenReturn(Optional.of(novo));
        when(titularidadeRepository.salvar(any(TitularidadeTransferencia.class)))
                .thenAnswer(inv -> {
                    TitularidadeTransferencia t = inv.getArgument(0);
                    t.setId(UUID.randomUUID());
                    return t;
                });

        var iniciado = useCase.iniciar(new TransferirTitularidadeUseCase.IniciarComando(
                solicitanteId, tenantId, novoTitularId));

        assertThat(iniciado.transferenciaId()).isNotNull();
        assertThat(iniciado.novoTitularNome()).isEqualTo("Fulano");
        verify(titularidadeRepository).cancelarAbertas(tenantId, solicitanteId);
        verify(titularidadeRepository).salvar(any(TitularidadeTransferencia.class));
    }

    // ---------- etapa biometria / celular ----------

    @Test
    void enviarCodigoCelularDeveExigirBiometriaPrevia() {
        TitularidadeTransferencia t = transferenciaAberta();
        when(titularidadeRepository.buscarPorId(t.getId())).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> useCase.enviarCodigo(
                new TransferirTitularidadeUseCase.EnviarCodigoComando(
                        t.getId(), solicitanteId, tenantId,
                        TransferirTitularidadeUseCase.ETAPA_CELULAR)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("biometria");
    }

    @Test
    void verificarCodigoCelularDeveRejeitarSemConfirmacaoDoCelular() {
        TitularidadeTransferencia t = transferenciaAberta();
        t.marcarBiometria();
        when(titularidadeRepository.buscarPorId(t.getId())).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> useCase.verificarCodigo(
                new TransferirTitularidadeUseCase.VerificarCodigoComando(
                        t.getId(), solicitanteId, tenantId,
                        TransferirTitularidadeUseCase.ETAPA_CELULAR,
                        "123456", false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("celular do novo titular");

        verify(titularidadeRepository, never()).salvarCodigo(any());
    }

    @Test
    void verificarCodigoDeveRejeitarCodigoInvalido() {
        TitularidadeTransferencia t = transferenciaAberta();
        t.marcarBiometria();
        when(titularidadeRepository.buscarPorId(t.getId())).thenReturn(Optional.of(t));

        TitularidadeCodigo codigo = new TitularidadeCodigo();
        codigo.setTransferenciaId(t.getId());
        codigo.setEtapa(TitularidadeCodigo.ETAPA_CELULAR);
        codigo.setCodigoHash("$2a$hash");
        codigo.setExpiraEm(Instant.now().plusSeconds(900));
        when(titularidadeRepository.listarCodigos(t.getId(), "CELULAR"))
                .thenReturn(List.of(codigo));
        when(passwordEncoder.matches(eq("000000"), anyString())).thenReturn(false);

        assertThatThrownBy(() -> useCase.verificarCodigo(
                new TransferirTitularidadeUseCase.VerificarCodigoComando(
                        t.getId(), solicitanteId, tenantId,
                        TransferirTitularidadeUseCase.ETAPA_CELULAR,
                        "000000", true)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void verificarCodigoCelularDeveMarcarEtapaQuandoConfere() {
        TitularidadeTransferencia t = transferenciaAberta();
        t.marcarBiometria();
        when(titularidadeRepository.buscarPorId(t.getId())).thenReturn(Optional.of(t));

        TitularidadeCodigo codigo = new TitularidadeCodigo();
        codigo.setTransferenciaId(t.getId());
        codigo.setEtapa(TitularidadeCodigo.ETAPA_CELULAR);
        codigo.setCodigoHash("$2a$hash");
        codigo.setExpiraEm(Instant.now().plusSeconds(900));
        when(titularidadeRepository.listarCodigos(t.getId(), "CELULAR"))
                .thenReturn(List.of(codigo));
        when(passwordEncoder.matches(eq("123456"), anyString())).thenReturn(true);
        when(titularidadeRepository.salvarCodigo(any())).thenAnswer(inv -> inv.getArgument(0));
        when(titularidadeRepository.salvar(any(TitularidadeTransferencia.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        useCase.verificarCodigo(new TransferirTitularidadeUseCase.VerificarCodigoComando(
                t.getId(), solicitanteId, tenantId,
                TransferirTitularidadeUseCase.ETAPA_CELULAR,
                "123456", true));

        ArgumentCaptor<TitularidadeTransferencia> captor =
                ArgumentCaptor.forClass(TitularidadeTransferencia.class);
        verify(titularidadeRepository).salvar(captor.capture());
        assertThat(captor.getValue().isEtapaCelular()).isTrue();
        assertThat(codigo.isUsado()).isTrue();
    }

    // ---------- concluir ----------

    @Test
    void concluirDeveExigirTodasAsEtapas() {
        TitularidadeTransferencia t = transferenciaAberta();
        t.marcarBiometria();
        when(titularidadeRepository.buscarPorId(t.getId())).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> useCase.concluir(
                new TransferirTitularidadeUseCase.Comando(
                        t.getId(), solicitanteId, tenantId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("todas as etapas");

        verify(usuarioRepository, never()).atualizar(any());
    }

    @Test
    void concluirDeveTrocarPapeisEDefinirModulos() {
        TitularidadeTransferencia t = transferenciaAberta();
        t.marcarBiometria();
        t.marcarCelular();
        t.marcarEmail();
        when(titularidadeRepository.buscarPorId(t.getId())).thenReturn(Optional.of(t));

        CpcUsuario admin = usuario(Role.ADMIN_EMPRESA);
        CpcUsuario novo = usuario(Role.COLABORADOR);
        when(usuarioRepository.buscarPorId(solicitanteId)).thenReturn(Optional.of(admin));
        when(usuarioRepository.buscarPorId(novoTitularId)).thenReturn(Optional.of(novo));
        when(modulosPort.listarCodigosAtivos(tenantId))
                .thenReturn(List.of("PONTO", "RECURSOS_HUMANOS", "ESTOQUE"));
        when(usuarioRepository.atualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(titularidadeRepository.salvar(any(TitularidadeTransferencia.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        useCase.concluir(new TransferirTitularidadeUseCase.Comando(
                t.getId(), solicitanteId, tenantId));

        ArgumentCaptor<CpcUsuario> usuarios = ArgumentCaptor.forClass(CpcUsuario.class);
        verify(usuarioRepository, org.mockito.Mockito.times(2)).atualizar(usuarios.capture());
        List<CpcUsuario> salvos = usuarios.getAllValues();

        CpcUsuario novoAdmin = salvos.stream()
                .filter(u -> u.getId().equals(novoTitularId)).findFirst().orElseThrow();
        CpcUsuario antigoColaborador = salvos.stream()
                .filter(u -> u.getId().equals(solicitanteId)).findFirst().orElseThrow();

        assertThat(novoAdmin.getRole()).isEqualTo(Role.ADMIN_EMPRESA);
        assertThat(antigoColaborador.getRole()).isEqualTo(Role.COLABORADOR);
        assertThat(antigoColaborador.isAcessoEstoque()).isFalse();

        verify(modulosPort).definirModulosDoUsuario(
                novoTitularId, tenantId, List.of("PONTO", "RECURSOS_HUMANOS", "ESTOQUE"));
        verify(modulosPort).definirModulosDoUsuario(
                solicitanteId, tenantId, List.of("PONTO"));

        ArgumentCaptor<TitularidadeTransferencia> tCaptor =
                ArgumentCaptor.forClass(TitularidadeTransferencia.class);
        verify(titularidadeRepository).salvar(tCaptor.capture());
        assertThat(tCaptor.getValue().getStatus())
                .isEqualTo(TitularidadeTransferencia.STATUS_CONCLUIDA);
    }

    // ---------- expiração ----------

    @Test
    void carregarDeveCancelarERejeitarTransferenciaExpirada() {
        TitularidadeTransferencia t = transferenciaAberta();
        t.setExpiraEm(Instant.now().minusSeconds(1));
        when(titularidadeRepository.buscarPorId(t.getId())).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> useCase.cancelar(
                new TransferirTitularidadeUseCase.Comando(
                        t.getId(), solicitanteId, tenantId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expirada");

        verify(titularidadeRepository).salvar(t);
        assertThat(t.getStatus()).isEqualTo(TitularidadeTransferencia.STATUS_CANCELADA);
    }

    @Test
    void cancelarDeveCancelarTransferenciaAberta() {
        TitularidadeTransferencia t = transferenciaAberta();
        when(titularidadeRepository.buscarPorId(t.getId())).thenReturn(Optional.of(t));
        when(titularidadeRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.cancelar(new TransferirTitularidadeUseCase.Comando(
                t.getId(), solicitanteId, tenantId));

        assertThat(t.getStatus()).isEqualTo(TitularidadeTransferencia.STATUS_CANCELADA);
    }

    @Test
    void iniciarDeveRejeitarNovoTitularDeOutroTenant() {
        CpcUsuario admin = usuario(Role.ADMIN_EMPRESA);
        when(usuarioRepository.buscarPorId(solicitanteId)).thenReturn(Optional.of(admin));

        CpcUsuario deOutroTenant = new CpcUsuario(
                novoTitularId, UUID.randomUUID(), "98765432100", "Outro",
                "outro@empresa.com", "hash", Role.COLABORADOR, UUID.randomUUID(),
                false, false, false, false, null);
        when(usuarioRepository.buscarPorId(novoTitularId)).thenReturn(Optional.of(deOutroTenant));

        assertThatThrownBy(() -> useCase.iniciar(
                new TransferirTitularidadeUseCase.IniciarComando(
                        solicitanteId, tenantId, novoTitularId)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não encontrado");

        verify(titularidadeRepository, never()).salvar(any());
    }
}
