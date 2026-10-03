package br.com.jess.chronos.pulse.modules.admin.application.service;

import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence.AdminDeviceTokenJpaEntity;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence.AdminDeviceTokenJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDeviceTokenServiceTest {

    private static final long TRINTA_DIAS_MS = Duration.ofDays(30).toMillis();

    @Mock
    private AdminDeviceTokenJpaRepository deviceTokenRepository;

    private AdminDeviceTokenService service;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        service = new AdminDeviceTokenService(deviceTokenRepository, TRINTA_DIAS_MS);
        adminId = UUID.randomUUID();
    }

    @Test
    void vincularDeveGerarTokenCruEArmazenarSomenteHash() {
        var vinculo = service.vincular(adminId, "MacBook do Administrator");

        assertThat(vinculo.deviceToken()).isNotBlank().hasSizeGreaterThanOrEqualTo(43);
        assertThat(vinculo.expiraEm()).isAfter(Instant.now().plus(Duration.ofDays(29)));

        var captor = ArgumentCaptor.forClass(AdminDeviceTokenJpaEntity.class);
        verify(deviceTokenRepository).save(captor.capture());
        var salvo = captor.getValue();
        assertThat(salvo.getTokenHash()).hasSize(64).isNotEqualTo(vinculo.deviceToken());
        assertThat(salvo.getAdminId()).isEqualTo(adminId);
        assertThat(salvo.getDeviceName()).isEqualTo("MacBook do Administrator");
        assertThat(salvo.getRevogadoEm()).isNull();
    }

    @Test
    void vincularDeveNormalizarDeviceNameNulo() {
        service.vincular(adminId, "   ");

        var captor = ArgumentCaptor.forClass(AdminDeviceTokenJpaEntity.class);
        verify(deviceTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getDeviceName()).isNull();
    }

    @Test
    void vincularDeveMorrerComAdminNulo() {
        assertThatThrownBy(() -> service.vincular(null, null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(deviceTokenRepository, never()).save(any());
    }

    @Test
    void validarDeveAceitarTokenValidoEAtualizarUltimoUso() {
        var vinculo = vincularPadrao();
        var entidade = entidadeSalva();
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));
        clearInvocations(deviceTokenRepository);

        assertThat(service.validar(adminId, vinculo.deviceToken())).isTrue();
        assertThat(entidade.getUltimoUsoEm()).isNotNull();
        verify(deviceTokenRepository).save(entidade);
    }

    @Test
    void validarDeveRecusarHashDesconhecido() {
        when(deviceTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThat(service.validar(adminId, "valor-falso")).isFalse();
        verify(deviceTokenRepository, never()).save(any());
    }

    @Test
    void validarDeveRecusarTokenExpirado() {
        // service com expiração negativa: o próprio vínculo nasce vencido
        var serviceExpirado = new AdminDeviceTokenService(deviceTokenRepository, -1000L);
        var vinculo = serviceExpirado.vincular(adminId, null);
        var entidade = entidadeSalva();
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));
        clearInvocations(deviceTokenRepository);

        assertThat(serviceExpirado.validar(adminId, vinculo.deviceToken())).isFalse();
        verify(deviceTokenRepository, never()).save(any());
    }

    @Test
    void validarDeveRecusarTokenRevogado() {
        var vinculo = vincularPadrao();
        var entidade = entidadeSalva();
        entidade.setRevogadoEm(Instant.now().minus(Duration.ofHours(1)));
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));
        clearInvocations(deviceTokenRepository);

        assertThat(service.validar(adminId, vinculo.deviceToken())).isFalse();
        verify(deviceTokenRepository, never()).save(any());
    }

    @Test
    void validarDeveRecusarTokenDeOutroAdmin() {
        var vinculo = vincularPadrao();
        var entidade = entidadeSalva();
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));
        clearInvocations(deviceTokenRepository);

        assertThat(service.validar(UUID.randomUUID(), vinculo.deviceToken())).isFalse();
        verify(deviceTokenRepository, never()).save(any());
    }

    @Test
    void validarDeveRecusarEmBranco() {
        assertThat(service.validar(adminId, null)).isFalse();
        assertThat(service.validar(adminId, "  ")).isFalse();
        assertThat(service.validar(null, "qualquer")).isFalse();
        verifyNoRepositoryInteractions();
    }

    @Test
    void revogarTodosDeveMarcarTodosOsVinculosAtivos() {
        var entidade1 = novaEntidadeSalva();
        var entidade2 = novaEntidadeSalva();
        when(deviceTokenRepository.findByAdminIdAndRevogadoEmIsNull(adminId))
                .thenReturn(List.of(entidade1, entidade2));

        int revogados = service.revogarTodos(adminId);

        assertThat(revogados).isEqualTo(2);
        assertThat(entidade1.getRevogadoEm()).isNotNull();
        assertThat(entidade2.getRevogadoEm()).isNotNull();
        verify(deviceTokenRepository).saveAll(List.of(entidade1, entidade2));
    }

    @Test
    void revogarTodosSemVinculosDeveRetornarZero() {
        when(deviceTokenRepository.findByAdminIdAndRevogadoEmIsNull(adminId))
                .thenReturn(List.of());

        assertThat(service.revogarTodos(adminId)).isZero();
        verify(deviceTokenRepository, never()).saveAll(any());
    }

    private AdminDeviceTokenService.VinculoAdminDeviceToken vincularPadrao() {
        return service.vincular(adminId, "Device Teste");
    }

    private AdminDeviceTokenJpaEntity entidadeSalva() {
        var captor = ArgumentCaptor.forClass(AdminDeviceTokenJpaEntity.class);
        verify(deviceTokenRepository, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private AdminDeviceTokenJpaEntity novaEntidadeSalva() {
        var entidade = new AdminDeviceTokenJpaEntity();
        entidade.setId(UUID.randomUUID());
        entidade.setAdminId(adminId);
        entidade.setTokenHash(UUID.randomUUID().toString().replace("-", "").repeat(2).substring(0, 64));
        entidade.setCriadoEm(Instant.now());
        entidade.setExpiraEm(Instant.now().plus(Duration.ofDays(30)));
        return entidade;
    }

    private void verifyNoRepositoryInteractions() {
        verify(deviceTokenRepository, never()).save(any());
        verify(deviceTokenRepository, never()).saveAll(any());
        verify(deviceTokenRepository, never()).findByTokenHash(anyString());
    }
}
