package br.com.jess.chronos.pulse.modules.auth.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${chronos.jwt.secret}") String secret,
            @Value("${chronos.jwt.expiration-ms}") long expirationMs,
            @Value("${chronos.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String gerarAccessToken(String cpf, String role, String cpcId, String tenantId) {
        return gerarAccessToken(cpf, role, cpcId, tenantId, false);
    }

    public String gerarAccessToken(String cpf, String role, String cpcId, String tenantId, boolean acessoEstoque) {
        return gerarAccessToken(cpf, role, cpcId, tenantId, acessoEstoque, false, false, false);
    }

    public String gerarAccessToken(String cpf, String role, String cpcId, String tenantId,
                                   boolean acessoEstoque, boolean acessoPatrimonio,
                                   boolean acessoFrota, boolean acessoProtocolo) {
        return Jwts.builder()
                .subject(cpf)
                .claim("typ", "access")
                .claim("role", role)
                .claim("cpcId", cpcId)
                .claim("tenantId", tenantId)
                .claim("acessoEstoque", acessoEstoque)
                .claim("acessoPatrimonio", acessoPatrimonio)
                .claim("acessoFrota", acessoFrota)
                .claim("acessoProtocolo", acessoProtocolo)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String gerarRefreshToken(String cpf) {
        return Jwts.builder()
                .subject(cpf)
                .claim("typ", "refresh")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    // Admin tokens (sem CPF, sem tenant, sem roles de módulos)
    public String gerarAccessTokenAdmin(String username, String adminId) {
        return Jwts.builder()
                .subject(username)
                .claim("typ", "access")
                .claim("role", "ADMIN_PLATAFORMA")
                .claim("adminId", adminId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String gerarRefreshTokenAdmin(String username, String adminId) {
        return Jwts.builder()
                .subject(username)
                .claim("typ", "refresh")
                .claim("adminId", adminId)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    // Token temporário entre senha OK e código 2FA (5 minutos)
    public String gerarTempTokenTwoFactor(String adminId) {
        return Jwts.builder()
                .subject(adminId)
                .claim("typ", "two_factor")
                .claim("adminId", adminId)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 300_000L))
                .signWith(secretKey)
                .compact();
    }

    public Claims extrairClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public String extrairCpf(String token) {
        return extrairClaims(token).getSubject();
    }

    /**
     * Tokens antigos (sem claim "typ") continuam aceitos durante a transição;
     * tokens novos "access" NÃO valem como refresh e vice-versa.
     */
    public boolean isRefreshToken(Claims claims) {
        String typ = claims.get("typ", String.class);
        return typ == null || "refresh".equals(typ);
    }

    public boolean isAccessToken(Claims claims) {
        String typ = claims.get("typ", String.class);
        return typ == null || "access".equals(typ);
    }

    public boolean isTwoFactorToken(Claims claims) {
        return "two_factor".equals(claims.get("typ", String.class));
    }

    public boolean isTokenValido(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
