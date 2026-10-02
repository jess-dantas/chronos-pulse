package br.com.jess.chronos.pulse.modules.usuario.service;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModulosPort modulosPort;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private UsuarioService service;

    private final UUID tenantId = UUID.randomUUID();

    private CpcUsuario conta(Role papel, String cpf, UUID tenant, boolean ativo) {
        CpcUsuario usuario = new CpcUsuario(null, null, cpf, "Nome " + cpf,
                cpf + "@empresa.com", "hash", papel, tenant);
        return ativo ? usuario : usuario.comAtivo(false);
    }

    private CpcUsuario operadorAdmin() {
        return conta(Role.ADMIN_EMPRESA, "11111111111", tenantId, true);
    }

    // ---- listar ----

    @Test
    void listarFiltraColaboradoresEOrdenaPorNome() {
        CpcUsuario gestor = conta(Role.GESTOR_RH, "22222222222", tenantId, true);
        CpcUsuario colaborador = conta(Role.COLABORADOR, "12345678901", tenantId, true);
        CpcUsuario admin = conta(Role.ADMIN_EMPRESA, "33333333333", tenantId, true);
        when(usuarioRepository.listarPorTenant(tenantId))
                .thenReturn(List.of(gestor, colaborador, admin));

        List<UsuarioService.UsuarioItem> itens = service.listar(tenantId);

        assertThat(itens).extracting(UsuarioService.UsuarioItem::cpf)
                .containsExactly("22222222222", "33333333333");
        assertThat(itens).allMatch(UsuarioService.UsuarioItem::ativo);
    }

    @Test
    void listarComTenantNuloEhInvalido() {
        assertThatThrownBy(() -> service.listar(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- criar ----

    @Test
    void criarGestorRhDefineVinculosFixosERegistraAuditoria() {
        when(usuarioRepository.existePorCpf("22222222222")).thenReturn(false);
        when(passwordEncoder.encode("S3nh@Forte")).thenReturn("hash-encode");
        when(usuarioRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        CpcUsuario operador = operadorAdmin();

        var item = service.criar(
                new UsuarioService.CriarUsuario("22222222222", "Ana Gestora",
                        "ana@empresa.com", "S3nh@Forte", "GESTOR_RH"),
                tenantId, operador);

        assertThat(item.role()).isEqualTo("GESTOR_RH");
        assertThat(item.ativo()).isTrue();
        ArgumentCaptor<CpcUsuario> criado = ArgumentCaptor.forClass(CpcUsuario.class);
        verify(usuarioRepository).salvar(criado.capture());
        assertThat(criado.getValue().getRole()).isEqualTo(Role.GESTOR_RH);
        assertThat(criado.getValue().getTenantId()).isEqualTo(tenantId);
        verify(modulosPort).definirModulosDoUsuario(
                eq(criado.getValue().getId()), eq(tenantId),
                eq(List.of("PONTO", "RECURSOS_HUMANOS")));
        verify(auditoriaService).registrar(eq("CRIACAO"), eq("USUARIO"),
                any(UUID.class), anyString(), eq(tenantId), any(UUID.class),
                anyString(), anyString(), eq(null), anyString(), eq(null));
    }

    @Test
    void criarAdminEmpresaNaoDefineVinculos() {
        when(usuarioRepository.existePorCpf("44444444444")).thenReturn(false);
        when(passwordEncoder.encode("S3nh@Forte")).thenReturn("hash-encode");
        when(usuarioRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        var item = service.criar(
                new UsuarioService.CriarUsuario("44444444444", "Bruno Admin",
                        null, "S3nh@Forte", "ADMIN_EMPRESA"),
                tenantId, operadorAdmin());

        assertThat(item.role()).isEqualTo("ADMIN_EMPRESA");
        verify(modulosPort, never()).definirModulosDoUsuario(any(), any(), any());
        verify(auditoriaService).registrar(eq("CRIACAO"), eq("USUARIO"),
                any(UUID.class), anyString(), eq(tenantId), any(UUID.class),
                anyString(), anyString(), eq(null), anyString(), eq(null));
    }

    @Test
    void criarComCpfDuplicadoEhInvalido() {
        when(usuarioRepository.existePorCpf("22222222222")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(
                new UsuarioService.CriarUsuario("22222222222", "Ana",
                        null, "S3nh@Forte", "GESTOR_RH"),
                tenantId, operadorAdmin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF já cadastrado");
    }

    @Test
    void criarComPapelColaboradorEhInvalido() {
        assertThatThrownBy(() -> service.criar(
                new UsuarioService.CriarUsuario("22222222222", "Ana",
                        null, "S3nh@Forte", "COLABORADOR"),
                tenantId, operadorAdmin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GESTOR_RH ou ADMIN_EMPRESA");
    }

    @Test
    void criarComPapelDesconhecidoEhInvalido() {
        assertThatThrownBy(() -> service.criar(
                new UsuarioService.CriarUsuario("22222222222", "Ana",
                        null, "S3nh@Forte", "ESTOQUE"),
                tenantId, operadorAdmin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Papel inválido");
    }

    @Test
    void criarComSenhaFracaEhInvalida() {
        when(usuarioRepository.existePorCpf("22222222222")).thenReturn(false);

        assertThatThrownBy(() -> service.criar(
                new UsuarioService.CriarUsuario("22222222222", "Ana",
                        null, "123", "GESTOR_RH"),
                tenantId, operadorAdmin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mínimo");
        verify(usuarioRepository, never()).salvar(any());
    }

    // ---- suspender ----

    @Test
    void suspenderMarcaContaComoInativaERegistraAuditoria() {
        CpcUsuario alvo = conta(Role.GESTOR_RH, "22222222222", tenantId, true);
        when(usuarioRepository.buscarPorId(alvo.getId())).thenReturn(Optional.of(alvo));
        when(usuarioRepository.atualizar(any())).thenAnswer(inv -> inv.getArgument(0));
        CpcUsuario operador = operadorAdmin();

        var item = service.suspender(alvo.getId(), tenantId, operador);

        assertThat(item.ativo()).isFalse();
        ArgumentCaptor<CpcUsuario> atualizado = ArgumentCaptor.forClass(CpcUsuario.class);
        verify(usuarioRepository).atualizar(atualizado.capture());
        assertThat(atualizado.getValue().isAtivo()).isFalse();
        assertThat(atualizado.getValue().getCpf()).isEqualTo("22222222222");
        verify(auditoriaService).registrar(eq("SUSPENSAO"), eq("USUARIO"),
                eq(alvo.getId()), anyString(), eq(tenantId), any(UUID.class),
                anyString(), anyString(), eq(null), anyString(), eq(null));
    }

    @Test
    void suspenderProprioUsuarioEhInvalido() {
        CpcUsuario operador = operadorAdmin();
        when(usuarioRepository.buscarPorId(operador.getId()))
                .thenReturn(Optional.of(operador));

        assertThatThrownBy(() -> service.suspender(operador.getId(), tenantId, operador))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("próprio usuário");
        verify(usuarioRepository, never()).atualizar(any());
    }

    @Test
    void suspenderContaColaboradorEhInvalida() {
        CpcUsuario colaborador = conta(Role.COLABORADOR, "12345678901", tenantId, true);
        when(usuarioRepository.buscarPorId(colaborador.getId()))
                .thenReturn(Optional.of(colaborador));

        assertThatThrownBy(() -> service.suspender(colaborador.getId(), tenantId, operadorAdmin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("gestão de colaboradores");
        verify(usuarioRepository, never()).atualizar(any());
    }

    @Test
    void suspenderContaDeOutroTenantEhInvalido() {
        CpcUsuario alvo = conta(Role.GESTOR_RH, "22222222222", UUID.randomUUID(), true);
        when(usuarioRepository.buscarPorId(alvo.getId())).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> service.suspender(alvo.getId(), tenantId, operadorAdmin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tenant");
    }

    @Test
    void suspenderContaJaSuspensaEhInvalido() {
        CpcUsuario alvo = conta(Role.ADMIN_EMPRESA, "44444444444", tenantId, false);
        when(usuarioRepository.buscarPorId(alvo.getId())).thenReturn(Optional.of(alvo));

        assertThatThrownBy(() -> service.suspender(alvo.getId(), tenantId, operadorAdmin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está suspenso");
        verify(usuarioRepository, never()).atualizar(any());
    }

    @Test
    void suspenderIdInexistenteEhInvalido() {
        UUID inexistente = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.suspender(inexistente, tenantId, operadorAdmin()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não encontrado");
    }
}
