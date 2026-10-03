package br.com.jess.chronos.pulse.modules.admin.application.service;

import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence.AdminDeviceTokenJpaEntity;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence.AdminDeviceTokenJpaRepository;
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
import java.util.UUID;

/// Dispositivo confiável do admin da plataforma (login "biometria-first"):
/// um token opaco de vida de 30 dias permite autenticar direto, pulando
/// senha e 2FA — a biometria é confirmada no aparelho antes de o cliente
/// enviar o token.
///
/// Garantias de segurança (espelho do DeviceTokenService do colaborador):
/// - somente o hash SHA-256 do token é persistido (o valor cru é devolvido
///   uma única vez, no vínculo);
/// - o vínculo é revogável a qualquer momento (revogarTodos);
/// - só aceita o token emitido para o mesmo admin que está logando.
@Service
public class AdminDeviceTokenService {

    private static final Logger log = LoggerFactory.getLogger(AdminDeviceTokenService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TAMANHO_DEVICE_NAME = 120;

    private final AdminDeviceTokenJpaRepository deviceTokenRepository;
    private final long deviceExpirationMs;

    public AdminDeviceTokenService(
            AdminDeviceTokenJpaRepository deviceTokenRepository,
            @Value("${chronos.admin.device-expiration-ms:2592000000}") long deviceExpirationMs) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.deviceExpirationMs = deviceExpirationMs;
    }

    /// Valor cru + expiração devolvidos uma única vez ao admin.
    public record VinculoAdminDeviceToken(String deviceToken, Instant expiraEm) {}

    /// Cria o vínculo do dispositivo confiável. O valor cru nunca é persistido.
    @Transactional
    public VinculoAdminDeviceToken vincular(UUID adminId, String deviceName) {
        if (adminId == null) {
            throw new IllegalArgumentException("Admin inválido para vínculo de dispositivo.");
        }

        String valor = gerarValorToken();
        Instant agora = Instant.now();

        var entidade = new AdminDeviceTokenJpaEntity();
        entidade.setId(UUID.randomUUID());
        entidade.setAdminId(adminId);
        entidade.setTokenHash(sha256Hex(valor));
        entidade.setDeviceName(normalizarDeviceName(deviceName));
        entidade.setCriadoEm(agora);
        entidade.setExpiraEm(agora.plusMillis(deviceExpirationMs));
        deviceTokenRepository.save(entidade);

        log.info("Dispositivo confiável do admin criado adminId={} expiraEm={}",
                adminId, entidade.getExpiraEm());
        return new VinculoAdminDeviceToken(valor, entidade.getExpiraEm());
    }

    /// Valida o valor cru enviado no login. Qualquer condição de segurança
    /// (hash desconhecido, de outro admin, revogado ou expirado) resulta em
    /// `false` — nunca autentica. Em sucesso atualiza `ultimo_uso_em`.
    @Transactional
    public boolean validar(UUID adminId, String valorBruto) {
        if (adminId == null || valorBruto == null || valorBruto.isBlank()) {
            return false;
        }
        var entidade = deviceTokenRepository
                .findByTokenHash(sha256Hex(valorBruto.trim()))
                .orElse(null);
        if (entidade == null || !adminId.equals(entidade.getAdminId())) {
            return false;
        }

        Instant agora = Instant.now();
        if (entidade.getRevogadoEm() != null || !entidade.getExpiraEm().isAfter(agora)) {
            return false;
        }

        entidade.setUltimoUsoEm(agora);
        deviceTokenRepository.save(entidade);
        return true;
    }

    /// Revoga todos os vínculos ativos do admin (troca de aparelho/perda).
    @Transactional
    public int revogarTodos(UUID adminId) {
        if (adminId == null) {
            return 0;
        }
        var ativos = deviceTokenRepository.findByAdminIdAndRevogadoEmIsNull(adminId);
        if (ativos.isEmpty()) {
            return 0;
        }
        Instant agora = Instant.now();
        ativos.forEach(t -> t.setRevogadoEm(agora));
        deviceTokenRepository.saveAll(ativos);
        log.info("{} vínculo(s) de dispositivo do admin revogado(s) adminId={}", ativos.size(), adminId);
        return ativos.size();
    }

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
