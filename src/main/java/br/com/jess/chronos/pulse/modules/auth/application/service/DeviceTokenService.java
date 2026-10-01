package br.com.jess.chronos.pulse.modules.auth.application.service;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence.DeviceTokenJpaEntity;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence.DeviceTokenJpaRepository;
import br.com.jess.chronos.pulse.modules.privacidade.service.PrivacidadeService;
import br.com.jess.chronos.pulse.modules.privacidade.repository.ConsentimentoPrivacidadeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/// Vinculo de dispositivo ("Modo Ponto"): permite ao app autenticar o lote de
/// sincronização de ponto sem sessão ativa (offline/sem login), usando um
/// token opaco de vida de 7 dias.
///
/// Garantias de segurança:
/// - somente o hash SHA-256 do token é persistido (o valor cru é devolvido
///   uma única vez, no vínculo);
/// - o vínculo é revogável a qualquer momento (revogarTodos) e morre com a
///   troca de senha;
/// - exige aceite ativo do termo de privacidade para ser emitido.
@Service
public class DeviceTokenService {

    private static final Logger log = LoggerFactory.getLogger(DeviceTokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TAMANHO_DEVICE_NAME = 120;

    private final DeviceTokenJpaRepository deviceTokenRepository;
    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final ConsentimentoPrivacidadeRepository consentimentoRepository;
    private final long deviceExpirationMs;

    public DeviceTokenService(
            DeviceTokenJpaRepository deviceTokenRepository,
            CpcUsuarioRepositoryPort usuarioRepository,
            ConsentimentoPrivacidadeRepository consentimentoRepository,
            @Value("${chronos.jwt.device-expiration-ms:604800000}") long deviceExpirationMs) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.consentimentoRepository = consentimentoRepository;
        this.deviceExpirationMs = deviceExpirationMs;
    }

    /// Valor cru + expiração devolvidos uma única vez ao titular.
    public record VinculoDeviceToken(String deviceToken, Instant expiraEm) {}

    /// Cria um vínculo para o usuário logado. O valor cru nunca é persistido.
    @Transactional
    public VinculoDeviceToken vincular(CpcUsuario usuario, String deviceName) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido para vínculo de dispositivo.");
        }
        // REGRA: sem aceite ativo do termo não há vínculo (o mesmo gate que
        // libera o sistema libera o modo ponto).
        if (usuario.getCpcId() == null || !consentimentoRepository
                .existsByCpcIdAndVersaoPoliticaAndAceitoTrue(
                        usuario.getCpcId(), PrivacidadeService.VERSAO_POLITICA_ATUAL)) {
            throw new IllegalStateException(
                    "Aceite o termo de ciência de privacidade antes de vincular o dispositivo.");
        }

        String valor = gerarValorToken();
        Instant agora = Instant.now();

        var entidade = new DeviceTokenJpaEntity();
        entidade.setId(UUID.randomUUID());
        entidade.setUsuarioId(usuario.getId());
        entidade.setTokenHash(sha256Hex(valor));
        entidade.setDeviceName(normalizarDeviceName(deviceName));
        entidade.setCriadoEm(agora);
        entidade.setExpiraEm(agora.plusMillis(deviceExpirationMs));
        deviceTokenRepository.save(entidade);

        log.info("Vinculo de dispositivo criado usuarioId={} expiraEm={}",
                usuario.getId(), entidade.getExpiraEm());
        return new VinculoDeviceToken(valor, entidade.getExpiraEm());
    }

    /// Revoga todos os vínculos ativos do usuário (desvincular dispositivo).
    @Transactional
    public int revogarTodos(UUID usuarioId) {
        if (usuarioId == null) {
            return 0;
        }
        var ativos = deviceTokenRepository.findByUsuarioIdAndRevogadoEmIsNull(usuarioId);
        if (ativos.isEmpty()) {
            return 0;
        }
        Instant agora = Instant.now();
        ativos.forEach(t -> t.setRevogadoEm(agora));
        deviceTokenRepository.saveAll(ativos);
        log.info("{} vinculo(s) de dispositivo revogado(s) usuarioId={}", ativos.size(), usuarioId);
        return ativos.size();
    }

    /// Valida o valor cru do header `X-Device-Token` e devolve o dono do
    /// vínculo. Qualquer condição de segurança (hash desconhecido, revogado,
    /// expirado, usuário inativo ou troca de senha posterior ao vínculo)
    /// resulta em `Optional.empty()` — nunca autentica.
    @Transactional
    public Optional<CpcUsuario> autenticar(String valorBruto) {
        return autenticarComVinculo(valorBruto).map(VinculoAtivo::usuario);
    }

    /// Idem a `autenticar`, mas também devolve a expiração do vínculo
    /// (usado pelo status do modo sem login para o aviso).
    @Transactional
    public Optional<VinculoAtivo> autenticarComVinculo(String valorBruto) {
        if (valorBruto == null || valorBruto.isBlank()) {
            return Optional.empty();
        }
        var entidade = deviceTokenRepository
                .findByTokenHash(sha256Hex(valorBruto.trim()))
                .orElse(null);
        if (entidade == null) {
            return Optional.empty();
        }

        Instant agora = Instant.now();
        if (entidade.getRevogadoEm() != null || !entidade.getExpiraEm().isAfter(agora)) {
            return Optional.empty();
        }

        var usuario = usuarioRepository.buscarPorId(entidade.getUsuarioId()).orElse(null);
        if (usuario == null || !usuario.isAtivo()) {
            return Optional.empty();
        }
        // Revogação por troca de senha (mesma regra do access token JWT).
        if (usuario.getSenhaAlteradaEm() != null
                && entidade.getCriadoEm().isBefore(usuario.getSenhaAlteradaEm())) {
            return Optional.empty();
        }

        entidade.setUltimoUsoEm(agora);
        deviceTokenRepository.save(entidade);
        return Optional.of(new VinculoAtivo(usuario, entidade.getExpiraEm(), entidade.getDeviceName()));
    }

    /// Dono + metadados do vínculo autenticado.
    public record VinculoAtivo(CpcUsuario usuario, Instant expiraEm, String deviceName) {}

    private String gerarValorToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizarDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isBlank()) {
            return null;
        }
        String limpo = deviceName.trim();
        return limpo.length() > TAMANHO_DEVICE_NAME
                ? limpo.substring(0, TAMANHO_DEVICE_NAME)
                : limpo;
    }

    private static String sha256Hex(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
