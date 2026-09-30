package br.com.jess.chronos.pulse.modules.auth.application.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence.DeviceTokenJpaEntity;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence.DeviceTokenJpaRepository;
import br.com.jess.chronos.pulse.modules.privacidade.repository.ConsentimentoPrivacidadeRepository;
import br.com.jess.chronos.pulse.modules.privacidade.service.PrivacidadeService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    private static final long SETE_DIAS_MS = Duration.ofDays(7).toMillis();

    @Mock
    private DeviceTokenJpaRepository deviceTokenRepository;
    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;
    @Mock
    private ConsentimentoPrivacidadeRepository consentimentoRepository;

    private DeviceTokenService service;
    private CpcUsuario usuario;

    @BeforeEach
    void setUp() {
        service = new DeviceTokenService(
                deviceTokenRepository, usuarioRepository, consentimentoRepository, SETE_DIAS_MS);
        usuario = new CpcUsuario(UUID.randomUUID(), UUID.randomUUID(), "12345678901",
                "Colaborador", "colab@empresa.com", "hash", Role.COLABORADOR, UUID.randomUUID());
    }

    @Test
    void vincularDeveGerarTokenCruEArmazenarSomenteHash() {
        when(consentimentoRepository.existsByCpcIdAndVersaoPoliticaAndAceitoTrue(
                usuario.getCpcId(), PrivacidadeService.VERSAO_POLITICA_ATUAL)).thenReturn(true);

        var vinculo = service.vincular(usuario, "Samsung A32");

        assertThat(vinculo.deviceToken()).isNotBlank().hasSizeGreaterThanOrEqualTo(43);
        assertThat(vinculo.expiraEm()).isAfter(Instant.now().plus(Duration.ofDays(6)));

        var captor = ArgumentCaptor.forClass(DeviceTokenJpaEntity.class);
        verify(deviceTokenRepository).save(captor.capture());
        var salvo = captor.getValue();
        assertThat(salvo.getTokenHash()).hasSize(64).isNotEqualTo(vinculo.deviceToken());
        assertThat(salvo.getUsuarioId()).isEqualTo(usuario.getId());
        assertThat(salvo.getDeviceName()).isEqualTo("Samsung A32");
        assertThat(salvo.getRevogadoEm()).isNull();
    }

    @Test
    void vincularDeveExigirAceiteAtivoDoTermo() {
        when(consentimentoRepository.existsByCpcIdAndVersaoPoliticaAndAceitoTrue(
                usuario.getCpcId(), PrivacidadeService.VERSAO_POLITICA_ATUAL)).thenReturn(false);

        assertThatThrownBy(() -> service.vincular(usuario, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("privacidade");

        verify(deviceTokenRepository, never()).save(any());
    }

    @Test
    void vincularDeveNormalizarDeviceNameNulo() {
        when(consentimentoRepository.existsByCpcIdAndVersaoPoliticaAndAceitoTrue(
                usuario.getCpcId(), PrivacidadeService.VERSAO_POLITICA_ATUAL)).thenReturn(true);

        service.vincular(usuario, "   ");

        var captor = ArgumentCaptor.forClass(DeviceTokenJpaEntity.class);
        verify(deviceTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getDeviceName()).isNull();
    }

    @Test
    void autenticarDeveAceitarTokenValidoEDevolverDono() {
        var vinculo = vincularPadrao();
        var entidade = entidadeSalva();
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));
        when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));

        var resultado = service.autenticar(vinculo.deviceToken());

        assertThat(resultado).contains(usuario);
        assertThat(entidade.getUltimoUsoEm()).isNotNull();
        verify(deviceTokenRepository, atLeastOnce()).save(entidade);
    }

    @Test
    void autenticarDeveRecusarHashDesconhecido() {
        when(deviceTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThat(service.autenticar("valor-falso")).isEmpty();
        verify(usuarioRepository, never()).buscarPorId(any());
    }

    @Test
    void autenticarDeveRecusarTokenExpirado() {
        when(consentimentoRepository.existsByCpcIdAndVersaoPoliticaAndAceitoTrue(
                usuario.getCpcId(), PrivacidadeService.VERSAO_POLITICA_ATUAL)).thenReturn(true);
        // service com expiração negativa: o próprio vínculo já nasce vencido
        var serviceExpirado = new DeviceTokenService(
                deviceTokenRepository, usuarioRepository, consentimentoRepository, -1000L);
        var vinculo = serviceExpirado.vincular(usuario, null);
        var entidade = entidadeSalva();
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));

        assertThat(serviceExpirado.autenticar(vinculo.deviceToken())).isEmpty();
        verify(usuarioRepository, never()).buscarPorId(any());
    }

    @Test
    void autenticarDeveRecusarTokenRevogado() {
        var vinculo = vincularPadrao();
        var entidade = entidadeSalva();
        entidade.setRevogadoEm(Instant.now().minus(Duration.ofHours(1)));
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));

        assertThat(service.autenticar(vinculo.deviceToken())).isEmpty();
        verify(usuarioRepository, never()).buscarPorId(any());
    }

    @Test
    void autenticarDeveRecusarUsuarioInativo() {
        var vinculo = vincularPadrao();
        var entidade = entidadeSalva();
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));
        var inativo = new CpcUsuario(usuario.getId(), usuario.getCpcId(), usuario.getCpf(),
                usuario.getNome(), usuario.getEmailCorporativo(), null, null, null,
                null, "hash", Role.COLABORADOR, usuario.getTenantId(),
                false, false, false, false, false, Instant.now());

        when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(inativo));

        assertThat(service.autenticar(vinculo.deviceToken())).isEmpty();
    }

    @Test
    void autenticarDeveMorrerComTrocaDeSenhaPosteriorAoVinculo() {
        var vinculo = vincularPadrao();
        var entidade = entidadeSalva();
        // vínculo antigo (o relógio do Windows tem resolução de ~15ms, então o
        // criadoEm é fixado no passado para tornar a ordem determinística)
        entidade.setCriadoEm(Instant.now().minus(Duration.ofDays(1)));
        when(deviceTokenRepository.findByTokenHash(entidade.getTokenHash()))
                .thenReturn(Optional.of(entidade));
        // troca de senha DEPOIS do vínculo: senhaAlteradaEm > criadoEm do token
        var trocada = usuario.comSenha("novo-hash-bcrypt");

        when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(trocada));

        assertThat(service.autenticar(vinculo.deviceToken())).isEmpty();
    }

    @Test
    void revogarTodosDeveMarcarTodosOsVinculosAtivos() {
        var entidade1 = novaEntidadeSalva();
        var entidade2 = novaEntidadeSalva();
        when(deviceTokenRepository.findByUsuarioIdAndRevogadoEmIsNull(usuario.getId()))
                .thenReturn(List.of(entidade1, entidade2));

        int revogados = service.revogarTodos(usuario.getId());

        assertThat(revogados).isEqualTo(2);
        assertThat(entidade1.getRevogadoEm()).isNotNull();
        assertThat(entidade2.getRevogadoEm()).isNotNull();
        verify(deviceTokenRepository).saveAll(List.of(entidade1, entidade2));
    }

    @Test
    void revogarTodosSemVinculosDeveRetornarZero() {
        when(deviceTokenRepository.findByUsuarioIdAndRevogadoEmIsNull(usuario.getId()))
                .thenReturn(List.of());

        assertThat(service.revogarTodos(usuario.getId())).isZero();
        verify(deviceTokenRepository, never()).saveAll(any());
    }

    private DeviceTokenService.VinculoDeviceToken vincularPadrao() {
        when(consentimentoRepository.existsByCpcIdAndVersaoPoliticaAndAceitoTrue(
                usuario.getCpcId(), PrivacidadeService.VERSAO_POLITICA_ATUAL)).thenReturn(true);
        return service.vincular(usuario, "Device Teste");
    }

    private DeviceTokenJpaEntity entidadeSalva() {
        var captor = ArgumentCaptor.forClass(DeviceTokenJpaEntity.class);
        verify(deviceTokenRepository, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private DeviceTokenJpaEntity novaEntidadeSalva() {
        var entidade = new DeviceTokenJpaEntity();
        entidade.setId(UUID.randomUUID());
        entidade.setUsuarioId(usuario.getId());
        entidade.setTokenHash(UUID.randomUUID().toString().replace("-", "").repeat(2).substring(0, 64));
        entidade.setCriadoEm(Instant.now());
        entidade.setExpiraEm(Instant.now().plus(Duration.ofDays(7)));
        return entidade;
    }
}
