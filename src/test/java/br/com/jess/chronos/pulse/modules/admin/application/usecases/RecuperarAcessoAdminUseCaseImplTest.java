package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminRecoveryCode;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RecuperarAcessoAdminUseCase.Comando;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminRecoveryCodeRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.AdminRecoveryCodeService;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperarAcessoAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private AdminRecoveryCodeRepositoryPort recoveryCodeRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private final AdminRecoveryCodeService recoveryCodeService = new AdminRecoveryCodeService();

    private RecuperarAcessoAdminUseCaseImpl useCase;

    private AdminPlataforma admin;
    private AdminRecoveryCode codigo;

    @BeforeEach
    void setUp() {
        useCase = new RecuperarAcessoAdminUseCaseImpl(
                repositoryPort, recoveryCodeRepositoryPort, passwordEncoder,
                jwtService, recoveryCodeService);
        admin = AdminPlataforma.builder()
                .id(UUID.randomUUID())
                .username("Administrator")
                .senhaHash("hash")
                .ativo(true)
                .twoFactorEnabled(true)
                .build();
        codigo = new AdminRecoveryCode();
        codigo.setAdminId(admin.getId());
        codigo.setCodeHash(recoveryCodeService.hash("ABCDE-FGHIJ"));
    }

    @Test
    void deveRecusarCodigoInvalido() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin1234", "hash")).thenReturn(true);
        when(recoveryCodeRepositoryPort.listarPorAdmin(admin.getId())).thenReturn(List.of(codigo));

        assertThatThrownBy(() -> useCase.executar(
                new Comando("Administrator", "admin1234", "ZZZZZ-ZZZZZ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código de recuperação inválido");
    }

    @Test
    void deveAceitarCodigoValidoERegenerar() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin1234", "hash")).thenReturn(true);
        when(recoveryCodeRepositoryPort.listarPorAdmin(admin.getId())).thenReturn(List.of(codigo));
        when(jwtService.gerarAccessTokenAdmin("Administrator", admin.getId().toString())).thenReturn("access");
        when(jwtService.gerarRefreshTokenAdmin("Administrator", admin.getId().toString())).thenReturn("refresh");

        var resultado = useCase.executar(
                new Comando("Administrator", "admin1234", "abcde-fghij"));

        assertThat(resultado.accessToken()).isEqualTo("access");
        assertThat(resultado.novosRecoveryCodes()).hasSize(AdminRecoveryCodeService.QUANTIDADE);
        assertThat(codigo.isUsado()).isTrue();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AdminRecoveryCode>> captor =
                ArgumentCaptor.forClass((Class) List.class);
        verify(recoveryCodeRepositoryPort).salvarTodos(captor.capture());
        assertThat(captor.getValue()).hasSize(AdminRecoveryCodeService.QUANTIDADE);

        verify(recoveryCodeRepositoryPort).removerPorAdmin(admin.getId());
        verify(recoveryCodeRepositoryPort).salvar(any(AdminRecoveryCode.class));
    }

    @Test
    void deveContarFalhaDeSenha() {
        when(repositoryPort.buscarPorUsername("Administrator")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(anyString(), eq("hash"))).thenReturn(false);

        assertThatThrownBy(() -> useCase.executar(
                new Comando("Administrator", "senha-errada", "ABCDE-FGHIJ")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repositoryPort).salvar(any(AdminPlataforma.class));
        verifyNoInteractions(recoveryCodeRepositoryPort);
    }
}
