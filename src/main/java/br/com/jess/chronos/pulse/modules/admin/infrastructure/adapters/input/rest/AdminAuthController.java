package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.BootstrapAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AutenticarAdminPlataformaUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.AlterarSenhaAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.GerenciarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.RecuperarAcessoAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.domain.ports.input.VerificarTwoFactorAdminUseCase;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminAlterarSenhaRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminBootstrapRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminBootstrapStatusDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminLoginRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminLoginResponseDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminRecoverRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorCodigoRequestDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorSetupDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorStatusDTO;
import br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.input.rest.dto.AdminTwoFactorVerifyRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.security.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {

    private final AutenticarAdminPlataformaUseCase autenticarAdminPlataformaUseCase;
    private final VerificarTwoFactorAdminUseCase verificarTwoFactorAdminUseCase;
    private final GerenciarTwoFactorAdminUseCase gerenciarTwoFactorAdminUseCase;
    private final AlterarSenhaAdminUseCase alterarSenhaAdminUseCase;
    private final BootstrapAdminUseCase bootstrapAdminUseCase;
    private final RecuperarAcessoAdminUseCase recuperarAcessoAdminUseCase;
    private final JwtService jwtService;
    private final boolean twoFactorRequired;

    public AdminAuthController(
            AutenticarAdminPlataformaUseCase autenticarAdminPlataformaUseCase,
            VerificarTwoFactorAdminUseCase verificarTwoFactorAdminUseCase,
            GerenciarTwoFactorAdminUseCase gerenciarTwoFactorAdminUseCase,
            AlterarSenhaAdminUseCase alterarSenhaAdminUseCase,
            BootstrapAdminUseCase bootstrapAdminUseCase,
            RecuperarAcessoAdminUseCase recuperarAcessoAdminUseCase,
            JwtService jwtService,
            @Value("${chronos.admin.two-factor-required:true}") boolean twoFactorRequired) {
        this.autenticarAdminPlataformaUseCase = autenticarAdminPlataformaUseCase;
        this.verificarTwoFactorAdminUseCase = verificarTwoFactorAdminUseCase;
        this.gerenciarTwoFactorAdminUseCase = gerenciarTwoFactorAdminUseCase;
        this.alterarSenhaAdminUseCase = alterarSenhaAdminUseCase;
        this.bootstrapAdminUseCase = bootstrapAdminUseCase;
        this.recuperarAcessoAdminUseCase = recuperarAcessoAdminUseCase;
        this.jwtService = jwtService;
        this.twoFactorRequired = twoFactorRequired;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponseDTO> login(@RequestBody @Valid AdminLoginRequestDTO request) {
        var resultado = autenticarAdminPlataformaUseCase.executar(
                new AutenticarAdminPlataformaUseCase.Comando(request.getUsername(), request.getSenha())
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
                request.getUsername(), request.getSenha(), request.getRecoveryCode()
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
        if (twoFactorRequired) {
            throw new IllegalStateException("2FA é obrigatório e não pode ser desativado");
        }
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
