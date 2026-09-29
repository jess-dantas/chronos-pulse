package br.com.jess.chronos.pulse.modules.admin.application.usecases;

import br.com.jess.chronos.pulse.modules.admin.domain.model.AdminPlataforma;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminPlataformaRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.output.AdminRecoveryCodeRepositoryPort;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.AdminRecoveryCodeService;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.security.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class GerenciarTwoFactorAdminUseCaseImplTest {

    @Mock
    private AdminPlataformaRepositoryPort repositoryPort;

    @Mock
    private AdminRecoveryCodeRepositoryPort recoveryCodeRepositoryPort;

    @Mock
    private TotpService totpService;

    @Mock
    private AdminRecoveryCodeService recoveryCodeService;

    private AdminPlataforma admin;
    private GerenciarTwoFactorAdminUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        admin = AdminPlataforma.builder()
                .id(UUID.randomUUID())
                .username("Administrator")
                .senhaHash("hash")
                .email("admin@example.com")
                .twoFactorEnabled(true)
                .twoFactorSecret("SEGREDO")
                .ativo(true)
                .build();
        useCase = new GerenciarTwoFactorAdminUseCaseImpl(
                repositoryPort, recoveryCodeRepositoryPort, totpService, recoveryCodeService);
        when(repositoryPort.buscarPorId(admin.getId())).thenReturn(Optional.of(admin));
    }

    @Test
    void deveDesabilitarDoisFatoresComCodigoTotpValido() {
        when(totpService.validar("123456", "SEGREDO")).thenReturn(true);

        useCase.desabilitar(admin.getId().toString(), "123456");

        assertThat(admin.isTwoFactorEnabled()).isFalse();
        assertThat(admin.getTwoFactorSecret()).isNull();
        verify(repositoryPort).salvar(admin);
    }

    @Test
    void deveRejeitarCodigoTotpInvalidoAoDesabilitar() {
        when(totpService.validar("000000", "SEGREDO")).thenReturn(false);

        assertThatThrownBy(() -> useCase.desabilitar(admin.getId().toString(), "000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código inválido");

        assertThat(admin.isTwoFactorEnabled()).isTrue();
        assertThat(admin.getTwoFactorSecret()).isEqualTo("SEGREDO");
        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveRejeitarDesabilitarQuandoDoisFatoresJaEstaDesligado() {
        admin.setTwoFactorEnabled(false);
        admin.setTwoFactorSecret(null);

        assertThatThrownBy(() -> useCase.desabilitar(admin.getId().toString(), "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("2FA não está habilitado");

        verify(repositoryPort, never()).salvar(any());
    }

    @Test
    void deveRejeitarDesabilitarComSecretAusente() {
        admin.setTwoFactorSecret(null);

        when(totpService.validar("123456", null)).thenReturn(false);

        assertThatThrownBy(() -> useCase.desabilitar(admin.getId().toString(), "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Código inválido");

        verify(repositoryPort, never()).salvar(any());
    }
}
