package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.application.service.DeviceTokenService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarStatusDeviceUseCaseImplTest {

    @Mock
    private DeviceTokenService deviceTokenService;

    private ConsultarStatusDeviceUseCaseImpl useCase;

    private CpcUsuario usuario;
    private Instant expiraEm;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarStatusDeviceUseCaseImpl(deviceTokenService);
        usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", "colab@empresa.com", "hash", Role.COLABORADOR, null);
        expiraEm = Instant.now().plusSeconds(3600);
    }

    @Test
    void statusDeveDevolverDadosDoVinculoAtivo() {
        usuario.setTwoFactorEnabled(true);
        when(deviceTokenService.autenticarComVinculo("token"))
                .thenReturn(Optional.of(new DeviceTokenService.VinculoAtivo(usuario, expiraEm, "Samsung A32")));

        var status = useCase.executar("token").orElseThrow();

        assertThat(status.cpcId()).isEqualTo(usuario.getCpcId());
        assertThat(status.nome()).isEqualTo("Colaborador");
        assertThat(status.twoFactorEnabled()).isTrue();
        assertThat(status.vinculoAtivo()).isTrue();
        assertThat(status.expiraEm()).isEqualTo(expiraEm);
    }

    @Test
    void semVinculoDeveRetornarVazio() {
        when(deviceTokenService.autenticarComVinculo("token")).thenReturn(Optional.empty());

        assertThat(useCase.executar("token")).isEmpty();
    }
}
