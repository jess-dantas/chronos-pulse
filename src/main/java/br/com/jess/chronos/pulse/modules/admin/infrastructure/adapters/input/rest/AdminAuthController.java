package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.admin.application.service.AdminDeviceTokenService;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.BootstrapAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AutenticarAdminPlataformaUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AlterarSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.EnviarCodigoEmailAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.GerenciarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RecuperarAcessoAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RefreshAdminTokenUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RedefinirSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.SolicitarResetSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarCodigoEmailAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminAlterarSenhaRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminBootstrapRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminBootstrapStatusDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminDispositivoRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminDeviceTokenResponseDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminEmailCodigoRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminEmailVerifyRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminLoginRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminLoginResponseDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminRecoverRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminRefreshRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminResetSenhaEnviarRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminResetSenhaVerificarRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorCodigoRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorSetupDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorStatusDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorVerifyRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AutenticarAdminPlataformaUseCase autenticarAdminPlataformaUseCase;
    private final VerificarTwoFactorAdminUseCase verificarTwoFactorAdminUseCase;
    private final GerenciarTwoFactorAdminUseCase gerenciarTwoFactorAdminUseCase;
    private final AlterarSenhaAdminUseCase alterarSenhaAdminUseCase;
    private final BootstrapAdminUseCase bootstrapAdminUseCase;
    private final RecuperarAcessoAdminUseCase recuperarAcessoAdminUseCase;
    private final EnviarCodigoEmailAdminUseCase enviarCodigoEmailAdminUseCase;
    private final VerificarCodigoEmailAdminUseCase verificarCodigoEmailAdminUseCase;
    private final SolicitarResetSenhaAdminUseCase solicitarResetSenhaAdminUseCase;
    private final RedefinirSenhaAdminUseCase redefinirSenhaAdminUseCase;
    private final RefreshAdminTokenUseCase refreshAdminTokenUseCase;
    private final AdminDeviceTokenService adminDeviceTokenService;
    private final JwtService jwtService;

    public AdminAuthController(
            AutenticarAdminPlataformaUseCase autenticarAdminPlataformaUseCase,
            VerificarTwoFactorAdminUseCase verificarTwoFactorAdminUseCase,
            GerenciarTwoFactorAdminUseCase gerenciarTwoFactorAdminUseCase,
            AlterarSenhaAdminUseCase alterarSenhaAdminUseCase,
            BootstrapAdminUseCase bootstrapAdminUseCase,
            RecuperarAcessoAdminUseCase recuperarAcessoAdminUseCase,
            EnviarCodigoEmailAdminUseCase enviarCodigoEmailAdminUseCase,
            VerificarCodigoEmailAdminUseCase verificarCodigoEmailAdminUseCase,
            SolicitarResetSenhaAdminUseCase solicitarResetSenhaAdminUseCase,
            RedefinirSenhaAdminUseCase redefinirSenhaAdminUseCase,
            RefreshAdminTokenUseCase refreshAdminTokenUseCase,
            AdminDeviceTokenService adminDeviceTokenService,
            JwtService jwtService) {
        this.autenticarAdminPlataformaUseCase = autenticarAdminPlataformaUseCase;
        this.verificarTwoFactorAdminUseCase = verificarTwoFactorAdminUseCase;
        this.gerenciarTwoFactorAdminUseCase = gerenciarTwoFactorAdminUseCase;
        this.alterarSenhaAdminUseCase = alterarSenhaAdminUseCase;
        this.bootstrapAdminUseCase = bootstrapAdminUseCase;
        this.recuperarAcessoAdminUseCase = recuperarAcessoAdminUseCase;
        this.enviarCodigoEmailAdminUseCase = enviarCodigoEmailAdminUseCase;
        this.verificarCodigoEmailAdminUseCase = verificarCodigoEmailAdminUseCase;
        this.solicitarResetSenhaAdminUseCase = solicitarResetSenhaAdminUseCase;
        this.redefinirSenhaAdminUseCase = redefinirSenhaAdminUseCase;
        this.refreshAdminTokenUseCase = refreshAdminTokenUseCase;
        this.adminDeviceTokenService = adminDeviceTokenService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponseDTO> login(@RequestBody @Valid AdminLoginRequestDTO request) {
        var resultado = autenticarAdminPlataformaUseCase.executar(
                new AutenticarAdminPlataformaUseCase.Comando(
                        request.getUsername(), request.getSenha(), request.getDeviceToken())
        );

        if (resultado.requiresTwoFactor()) {
            if (resultado.setupRequired()) {
                return ResponseEntity.ok(AdminLoginResponseDTO.twoFactorSetupRequired(resultado.tempToken()));
            }
            return ResponseEntity.ok(AdminLoginResponseDTO.twoFactorRequired(resultado.tempToken()));
        }

        return ResponseEntity.ok(AdminLoginResponseDTO.fromDomain(
                resultado.admin(), resultado.accessToken(), resultado.refreshToken()
        ));
    }

    @GetMapping("/bootstrap/status")
    public ResponseEntity<AdminBootstrapStatusDTO> bootstrapStatus() {
        return ResponseEntity.ok(new AdminBootstrapStatusDTO(bootstrapAdminUseCase.disponivel()));
    }

    @PostMapping("/bootstrap")
    public ResponseEntity<AdminLoginResponseDTO> bootstrap(
            @RequestBody @Valid AdminBootstrapRequestDTO request) {
        var resultado = bootstrapAdminUseCase.executar(new BootstrapAdminUseCase.Comando(
                request.getUsername(), request.getSenha(), request.getNomeCompleto(), request.getEmail()
        ));
        return ResponseEntity.ok(AdminLoginResponseDTO.twoFactorSetupRequired(resultado.tempToken()));
    }

    @PostMapping("/2fa/recover")
    public ResponseEntity<AdminLoginResponseDTO> recover(
            @RequestBody @Valid AdminRecoverRequestDTO request) {
        var resultado = recuperarAcessoAdminUseCase.executar(new RecuperarAcessoAdminUseCase.Comando(
                request.getUsername(), request.getSenha(), request.getRecoveryCode(), request.getNovaSenha()
        ));
        var response = AdminLoginResponseDTO.fromDomain(
                resultado.admin(), resultado.accessToken(), resultado.refreshToken());
        response.setRecoveryCodes(resultado.novosRecoveryCodes());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Em uma implementação completa, invalidar refresh token aqui
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AdminLoginResponseDTO> refresh(
            @RequestBody @Valid AdminRefreshRequestDTO request) {
        var resultado = refreshAdminTokenUseCase.executar(
                new RefreshAdminTokenUseCase.Comando(request.refreshToken()));
        return ResponseEntity.ok(AdminLoginResponseDTO.fromDomain(
                resultado.admin(), resultado.accessToken(), resultado.refreshToken()
        ));
    }

    @PostMapping("/2fa/email/send")
    public ResponseEntity<Void> sendEmailCode(
            @RequestBody @Valid AdminEmailCodigoRequestDTO request) {
        enviarCodigoEmailAdminUseCase.executar(
                new EnviarCodigoEmailAdminUseCase.Comando(request.getTempToken()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/2fa/email/verify")
    public ResponseEntity<AdminLoginResponseDTO> verifyEmailCode(
            @RequestBody @Valid AdminEmailVerifyRequestDTO request) {
        var resultado = verificarCodigoEmailAdminUseCase.executar(
                new VerificarCodigoEmailAdminUseCase.Comando(request.getTempToken(), request.getCodigo()));
        return ResponseEntity.ok(AdminLoginResponseDTO.fromDomain(
                resultado.admin(), resultado.accessToken(), resultado.refreshToken()
        ));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<AdminLoginResponseDTO> verifyTwoFactor(
            @RequestBody @Valid AdminTwoFactorVerifyRequestDTO request) {
        var resultado = verificarTwoFactorAdminUseCase.executar(
                new VerificarTwoFactorAdminUseCase.Comando(request.getTempToken(), request.getCodigo())
        );
        return ResponseEntity.ok(AdminLoginResponseDTO.fromDomain(
                resultado.admin(), resultado.accessToken(), resultado.refreshToken()
        ));
    }

    @GetMapping("/2fa/status")
    public ResponseEntity<AdminTwoFactorStatusDTO> twoFactorStatus(
            @RequestHeader("Authorization") String authorization) {
        String adminId = extrairAdminId(authorization);
        var status = gerenciarTwoFactorAdminUseCase.status(adminId);
        return ResponseEntity.ok(AdminTwoFactorStatusDTO.builder().enabled(status.enabled()).build());
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<AdminTwoFactorSetupDTO> twoFactorSetup(
            @RequestHeader("Authorization") String authorization) {
        String adminId = extrairAdminId(authorization);
        var setup = gerenciarTwoFactorAdminUseCase.setup(adminId);
        return ResponseEntity.ok(AdminTwoFactorSetupDTO.builder()
                .secret(setup.secret())
                .otpauthUri(setup.otpauthUri())
                .build());
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<AdminLoginResponseDTO> twoFactorConfirm(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid AdminTwoFactorCodigoRequestDTO request) {
        Claims claims = extrairClaims(authorization);
        String adminId = claims.get("adminId", String.class);
        if (adminId == null) {
            throw new IllegalArgumentException("Token inválido");
        }
        var resultado = gerenciarTwoFactorAdminUseCase.confirmar(adminId, request.getCodigo());

        String accessToken = null;
        String refreshToken = null;
        if (jwtService.isTwoFactorToken(claims)) {
            // Fluxo de bootstrap/setup sem sessão ativa: emite os tokens finais.
            String username = resultado.admin().getUsername();
            accessToken = jwtService.gerarAccessTokenAdmin(username, adminId);
            refreshToken = jwtService.gerarRefreshTokenAdmin(username, adminId);
        }

        var response = AdminLoginResponseDTO.fromDomain(
                resultado.admin(), accessToken, refreshToken);
        response.setRecoveryCodes(resultado.recoveryCodes());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<Void> twoFactorDisable(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid AdminTwoFactorCodigoRequestDTO request) {
        // Sempre permitido: exige apenas código TOTP válido do dispositivo
        // atual. Perda/troca de celular é coberta pelos recovery codes
        // (POST /2fa/recover) na próxima autenticação.
        String adminId = extrairAdminId(authorization);
        gerenciarTwoFactorAdminUseCase.desabilitar(adminId, request.getCodigo());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(
            @RequestHeader("Authorization") String authorization,
            @RequestBody @Valid AdminAlterarSenhaRequestDTO request) {
        String adminId = extrairAdminId(authorization);
        alterarSenhaAdminUseCase.executar(new AlterarSenhaAdminUseCase.Comando(
                adminId, request.getSenhaAtual(), request.getNovaSenha()
        ));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-senha/enviar")
    public ResponseEntity<Void> resetSenhaEnviar(
            @RequestBody @Valid AdminResetSenhaEnviarRequestDTO request) {
        solicitarResetSenhaAdminUseCase.executar(
                new SolicitarResetSenhaAdminUseCase.Comando(request.getUsername()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-senha/verificar")
    public ResponseEntity<Void> resetSenhaVerificar(
            @RequestBody @Valid AdminResetSenhaVerificarRequestDTO request) {
        redefinirSenhaAdminUseCase.executar(new RedefinirSenhaAdminUseCase.Comando(
                request.getUsername(), request.getCodigo(), request.getNovaSenha()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dispositivo")
    public ResponseEntity<AdminDeviceTokenResponseDTO> dispositivoVincular(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) @Valid AdminDispositivoRequestDTO request) {
        // Exige access token admin (o JwtAuthFilter já recusa tempToken).
        // A checagem explícita abaixo é defesa em profundidade: um tempToken
        // do 2FA-first não pode confiar o dispositivo sem o código.
        Claims claims = extrairClaims(authorization);
        if (jwtService.isTwoFactorToken(claims)) {
            throw new IllegalArgumentException("Token inválido");
        }
        String adminId = claims.get("adminId", String.class);
        if (adminId == null) {
            throw new IllegalArgumentException("Token inválido");
        }
        var vinculo = adminDeviceTokenService.vincular(
                UUID.fromString(adminId), request == null ? null : request.getDeviceName());
        return ResponseEntity.ok(new AdminDeviceTokenResponseDTO(
                vinculo.deviceToken(), vinculo.expiraEm()));
    }

    @DeleteMapping("/dispositivo")
    public ResponseEntity<Void> dispositivoRevogar(
            @RequestHeader("Authorization") String authorization) {
        // Revoga TODOS os vínculos do admin (perda/troca de aparelho).
        String adminId = extrairAdminId(authorization);
        adminDeviceTokenService.revogarTodos(UUID.fromString(adminId));
        return ResponseEntity.ok().build();
    }

    private Claims extrairClaims(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token ausente");
        }
        try {
            return jwtService.extrairClaims(authorization.substring(7));
        } catch (Exception e) {
            throw new IllegalArgumentException("Token inválido");
        }
    }

    private String extrairAdminId(String authorization) {
        String adminId = extrairClaims(authorization).get("adminId", String.class);
        if (adminId == null) {
            throw new IllegalArgumentException("Token inválido");
        }
        return adminId;
    }
}
