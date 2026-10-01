package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.application.service.DeviceTokenService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AlterarFotoPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AlterarSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.BuscarPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.CadastrarEmpresaCompletoUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.ConsultarStatusDeviceUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.EnviarCodigoEmailUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.GerenciarTwoFactorUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RedefinirSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RefreshTokenUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.SolicitarRecuperacaoSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.VerificarCodigoEmailUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.VerificarDeviceTwoFactorUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.VerificarTwoFactorUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.AlterarSenhaRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.CadastrarEmpresaCompletoRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.DeviceStatusResponseDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.DeviceVerificarRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.DeviceVerificarResponseDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.DeviceVinculoRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.DeviceVinculoResponseDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.EsqueciSenhaRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.LoginRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.LoginResponseDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.RedefinirSenhaRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.RefreshTokenRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.TwoFactorCodigoRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.TwoFactorConfirmarRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.TwoFactorEmailCodigoRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.TwoFactorMetodo;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.TwoFactorSetupDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.TwoFactorStatusDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final CadastrarEmpresaCompletoUseCase cadastrarEmpresaCompletoUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final BuscarPerfilUseCase buscarPerfilUseCase;
    private final AlterarSenhaUseCase alterarSenhaUseCase;
    private final SolicitarRecuperacaoSenhaUseCase solicitarRecuperacaoSenhaUseCase;
    private final RedefinirSenhaUseCase redefinirSenhaUseCase;
    private final AlterarFotoPerfilUseCase alterarFotoPerfilUseCase;
    private final AuditoriaService auditoriaService;
    private final DeviceTokenService deviceTokenService;
    private final VerificarTwoFactorUsuarioUseCase verificarTwoFactorUsuarioUseCase;
    private final EnviarCodigoEmailUsuarioUseCase enviarCodigoEmailUsuarioUseCase;
    private final VerificarCodigoEmailUsuarioUseCase verificarCodigoEmailUsuarioUseCase;
    private final GerenciarTwoFactorUsuarioUseCase gerenciarTwoFactorUsuarioUseCase;
    private final ConsultarStatusDeviceUseCase consultarStatusDeviceUseCase;
    private final VerificarDeviceTwoFactorUseCase verificarDeviceTwoFactorUseCase;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                          CadastrarEmpresaCompletoUseCase cadastrarEmpresaCompletoUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          BuscarPerfilUseCase buscarPerfilUseCase,
                          AlterarSenhaUseCase alterarSenhaUseCase,
                          SolicitarRecuperacaoSenhaUseCase solicitarRecuperacaoSenhaUseCase,
                          RedefinirSenhaUseCase redefinirSenhaUseCase,
                          AlterarFotoPerfilUseCase alterarFotoPerfilUseCase,
                          AuditoriaService auditoriaService,
                          DeviceTokenService deviceTokenService,
                          VerificarTwoFactorUsuarioUseCase verificarTwoFactorUsuarioUseCase,
                          EnviarCodigoEmailUsuarioUseCase enviarCodigoEmailUsuarioUseCase,
                          VerificarCodigoEmailUsuarioUseCase verificarCodigoEmailUsuarioUseCase,
                          GerenciarTwoFactorUsuarioUseCase gerenciarTwoFactorUsuarioUseCase,
                          ConsultarStatusDeviceUseCase consultarStatusDeviceUseCase,
                          VerificarDeviceTwoFactorUseCase verificarDeviceTwoFactorUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.cadastrarEmpresaCompletoUseCase = cadastrarEmpresaCompletoUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.buscarPerfilUseCase = buscarPerfilUseCase;
        this.alterarSenhaUseCase = alterarSenhaUseCase;
        this.solicitarRecuperacaoSenhaUseCase = solicitarRecuperacaoSenhaUseCase;
        this.redefinirSenhaUseCase = redefinirSenhaUseCase;
        this.alterarFotoPerfilUseCase = alterarFotoPerfilUseCase;
        this.auditoriaService = auditoriaService;
        this.deviceTokenService = deviceTokenService;
        this.verificarTwoFactorUsuarioUseCase = verificarTwoFactorUsuarioUseCase;
        this.enviarCodigoEmailUsuarioUseCase = enviarCodigoEmailUsuarioUseCase;
        this.verificarCodigoEmailUsuarioUseCase = verificarCodigoEmailUsuarioUseCase;
        this.gerenciarTwoFactorUsuarioUseCase = gerenciarTwoFactorUsuarioUseCase;
        this.consultarStatusDeviceUseCase = consultarStatusDeviceUseCase;
        this.verificarDeviceTwoFactorUseCase = verificarDeviceTwoFactorUseCase;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "UP", "timestamp", String.valueOf(System.currentTimeMillis())));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        var resultado = autenticarUsuarioUseCase.executar(
                new AutenticarUsuarioUseCase.Comando(request.cpf(), request.senha()));

        if (!resultado.requiresTwoFactor()) {
            auditoriaService.registrar("LOGIN", "USUARIO", null,
                    "Acesso realizado",
                    resultado.tenantId() != null ? java.util.UUID.fromString(resultado.tenantId()) : null,
                    resultado.cpcId() != null ? java.util.UUID.fromString(resultado.cpcId()) : null,
                    request.cpf(), resultado.role(), null, null, null);
        }
        return ResponseEntity.ok(montarLogin(resultado));
    }

    /// Monta o DTO de login; `requiresTwoFactor/tempToken` vêm do resultado
    /// quando a sessão está pendente de verificação em /auth/2fa/*.
    private LoginResponseDTO montarLogin(AutenticarUsuarioUseCase.Resultado r) {
        return new LoginResponseDTO(
                r.accessToken(), r.refreshToken(), r.role(), r.cpf(), r.cpcId(),
                r.nome(), r.email(), r.tenantId(), r.tenantSlug(), r.acessoEstoque(),
                r.acessoPatrimonio(), r.acessoFrota(), r.acessoProtocolo(),
                r.foto(), r.modulos(),
                r.requiresTwoFactor() ? Boolean.TRUE : null,
                r.requiresTwoFactor() ? r.tempToken() : null);
    }

    /// 2FA do colaborador — TOTP na etapa de login.
    @PostMapping("/2fa/verify")
    public ResponseEntity<LoginResponseDTO> verifyTwoFactor(
            @RequestBody @Valid TwoFactorCodigoRequestDTO request) {
        var resultado = verificarTwoFactorUsuarioUseCase.executar(
                new VerificarTwoFactorUsuarioUseCase.Comando(request.tempToken(), request.codigo()));
        registrarLogin(resultado);
        return ResponseEntity.ok(montarLogin(resultado));
    }

    /// Envia o OTP de 8 dígitos por e-mail na etapa de login (15 min).
    @PostMapping("/2fa/email/send")
    public ResponseEntity<Void> sendEmailCode(
            @RequestBody @Valid TwoFactorEmailCodigoRequestDTO request) {
        enviarCodigoEmailUsuarioUseCase.executar(
                new EnviarCodigoEmailUsuarioUseCase.Comando(request.tempToken()));
        return ResponseEntity.ok().build();
    }

    /// Valida o OTP por e-mail na etapa de login e emite os tokens.
    @PostMapping("/2fa/email/verify")
    public ResponseEntity<LoginResponseDTO> verifyEmailCode(
            @RequestBody @Valid TwoFactorCodigoRequestDTO request) {
        var resultado = verificarCodigoEmailUsuarioUseCase.executar(
                new VerificarCodigoEmailUsuarioUseCase.Comando(request.tempToken(), request.codigo()));
        registrarLogin(resultado);
        return ResponseEntity.ok(montarLogin(resultado));
    }

    @GetMapping("/2fa/status")
    public ResponseEntity<TwoFactorStatusDTO> twoFactorStatus(
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        var status = gerenciarTwoFactorUsuarioUseCase.status(usuarioLogado.getId().toString());
        return ResponseEntity.ok(new TwoFactorStatusDTO(status.enabled()));
    }

    @PostMapping("/2fa/setup")
    public ResponseEntity<TwoFactorSetupDTO> twoFactorSetup(
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        var setup = gerenciarTwoFactorUsuarioUseCase.setup(usuarioLogado.getId().toString());
        return ResponseEntity.ok(new TwoFactorSetupDTO(setup.secret(), setup.otpauthUri()));
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<Void> twoFactorConfirm(
            @AuthenticationPrincipal CpcUsuario usuarioLogado,
            @RequestBody @Valid TwoFactorConfirmarRequestDTO request) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        gerenciarTwoFactorUsuarioUseCase.confirmar(usuarioLogado.getId().toString(), request.codigo());
        auditoriaService.registrar("HABILITAR_2FA", "USUARIO", usuarioLogado.getId(),
                "2FA TOTP habilitado na conta do colaborador",
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole() != null ? usuarioLogado.getRole().name() : null,
                null, null, null);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<Void> twoFactorDisable(
            @AuthenticationPrincipal CpcUsuario usuarioLogado,
            @RequestBody @Valid TwoFactorConfirmarRequestDTO request) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        gerenciarTwoFactorUsuarioUseCase.desabilitar(usuarioLogado.getId().toString(), request.codigo());
        auditoriaService.registrar("DESABILITAR_2FA", "USUARIO", usuarioLogado.getId(),
                "2FA desabilitado na conta do colaborador",
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole() != null ? usuarioLogado.getRole().name() : null,
                null, null, null);
        return ResponseEntity.ok().build();
    }

    /// Status do vínculo + 2FA no modo sem login (sem sessão; exige o
    /// header X-Device-Token válido).
    @GetMapping("/device/status")
    public ResponseEntity<DeviceStatusResponseDTO> deviceStatus(
            @RequestHeader(value = "X-Device-Token", required = false) String deviceToken) {
        return consultarStatusDeviceUseCase.executar(deviceToken)
                .map(status -> ResponseEntity.ok(new DeviceStatusResponseDTO(
                        status.cpcId(), status.nome(), status.twoFactorEnabled(), status.expiraEm())))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    /// Verificação do 2FA no modo sem login (ordem biometria → 2FA → vínculo).
    /// `metodo=EMAIL` sem código gera e envia o OTP.
    @PostMapping("/device/verificar")
    public ResponseEntity<DeviceVerificarResponseDTO> deviceVerificar(
            @RequestHeader(value = "X-Device-Token", required = false) String deviceToken,
            @RequestBody @Valid DeviceVerificarRequestDTO request) {
        TwoFactorMetodo metodoDto = request.metodo() != null ? request.metodo() : TwoFactorMetodo.TOTP;
        var metodo = metodoDto == TwoFactorMetodo.EMAIL
                ? VerificarDeviceTwoFactorUseCase.Metodo.EMAIL
                : VerificarDeviceTwoFactorUseCase.Metodo.TOTP;
        var resultado = verificarDeviceTwoFactorUseCase.executar(
                new VerificarDeviceTwoFactorUseCase.Comando(deviceToken, metodo, request.codigo()));
        if (resultado.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        var r = resultado.get();
        if (r.verificado()) {
            auditoriaService.registrar("LOGIN_2FA_DEVICE", "USUARIO", null,
                    "2FA verificado no modo sem login",
                    null, r.cpcId(), r.cpf(), r.role(), null, null, null);
        }
        return ResponseEntity.ok(new DeviceVerificarResponseDTO(r.verificado(), r.enviado(), r.expiraEm()));
    }

    private void registrarLogin(AutenticarUsuarioUseCase.Resultado resultado) {
        auditoriaService.registrar("LOGIN", "USUARIO", null,
                "Acesso realizado",
                resultado.tenantId() != null ? java.util.UUID.fromString(resultado.tenantId()) : null,
                resultado.cpcId() != null ? java.util.UUID.fromString(resultado.cpcId()) : null,
                resultado.cpf(), resultado.role(), null, null, null);
    }

    @PostMapping("/cadastrar-empresa")
    public ResponseEntity<LoginResponseDTO> cadastrarEmpresa(
            @RequestBody @Valid CadastrarEmpresaCompletoRequestDTO request) {
        var resultado = cadastrarEmpresaCompletoUseCase.executar(
                new CadastrarEmpresaCompletoUseCase.Comando(
                        request.cnpj(), request.nomeEmpresa(),
                        request.responsavelNome(), request.responsavelCpf(),
                        request.responsavelEmail(), request.responsavelCelular(),
                        request.responsavelSenha(), request.responsavelTelefone(),
                        request.enderecoLogradouro(), request.enderecoNumero(),
                        request.enderecoComplemento(), request.enderecoBairro(),
                        request.enderecoCidade(), request.enderecoUf(),
                        request.enderecoCep()));
return ResponseEntity.ok(new LoginResponseDTO(
                resultado.accessToken(), resultado.refreshToken(),
                resultado.role(), request.responsavelCpf(),
                resultado.cpcId(),
                resultado.nome(), resultado.email(),
                resultado.tenantId(), resultado.tenantSlug(), resultado.acessoEstoque(),
                resultado.acessoPatrimonio(), resultado.acessoFrota(),
                resultado.acessoProtocolo(),
                resultado.foto(), resultado.modulos(), null, null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO request) {
        var resultado = refreshTokenUseCase.executar(
                new RefreshTokenUseCase.Comando(request.refreshToken()));
        return ResponseEntity.ok(new LoginResponseDTO(
                resultado.accessToken(), null,
                resultado.role(), resultado.cpf(),
                resultado.cpcId(),
                resultado.nome(), resultado.email(),
                resultado.tenantId(), resultado.tenantSlug(), resultado.acessoEstoque(),
                resultado.acessoPatrimonio(), resultado.acessoFrota(),
                resultado.acessoProtocolo(),
                resultado.foto(), resultado.modulos(), null, null));
    }

    @GetMapping("/me")
    public ResponseEntity<BuscarPerfilUseCase.Resultado> me(
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        var perfil = buscarPerfilUseCase.executar(usuarioLogado.getCpf());
        return ResponseEntity.ok(perfil);
    }

    @PostMapping("/alterar-senha")
    public ResponseEntity<Map<String, String>> alterarSenha(
            @AuthenticationPrincipal CpcUsuario usuarioLogado,
            @RequestBody @Valid AlterarSenhaRequestDTO request) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        alterarSenhaUseCase.executar(new AlterarSenhaUseCase.Comando(
                usuarioLogado.getCpf(), request.senhaAtual(), request.novaSenha()));
        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso."));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Map<String, String>> esqueciSenha(
            @RequestBody @Valid EsqueciSenhaRequestDTO request) {
        solicitarRecuperacaoSenhaUseCase.executar(new SolicitarRecuperacaoSenhaUseCase.Comando(request.cpf()));
        return ResponseEntity.ok(Map.of(
                "mensagem", "Se o CPF estiver cadastrado, um codigo de recuperacao sera enviado para o e-mail registrado."));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(
            @RequestBody @Valid RedefinirSenhaRequestDTO request) {
        redefinirSenhaUseCase.executar(new RedefinirSenhaUseCase.Comando(
                request.cpf(), request.codigo(), request.novaSenha()));
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso."));
    }

    @PostMapping("/me/foto")
    public ResponseEntity<Map<String, String>> atualizarFoto(
            @AuthenticationPrincipal CpcUsuario usuarioLogado,
            @RequestParam("foto") MultipartFile foto) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            String fotoBase64 = alterarFotoPerfilUseCase.executar(
                    new AlterarFotoPerfilUseCase.Comando(usuarioLogado.getCpf(), foto.getBytes()));
            return ResponseEntity.ok(Map.of("foto", fotoBase64));
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Erro ao ler o arquivo de imagem.");
        }
    }

    /// Vincula o dispositivo ao usuário ("Modo Ponto"): exige sessão ativa e
    /// aceite do termo de privacidade. Devolve o token cru uma única vez.
    @PostMapping("/device/vincular")
    public ResponseEntity<DeviceVinculoResponseDTO> vincularDeviceToken(
            @AuthenticationPrincipal CpcUsuario usuarioLogado,
            @RequestBody(required = false) @Valid DeviceVinculoRequestDTO request) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        var vinculo = deviceTokenService.vincular(usuarioLogado,
                request != null ? request.deviceName() : null);
        auditoriaService.registrar("VINCULO_DEVICE_TOKEN", "USUARIO", usuarioLogado.getId(),
                "Vinculo de dispositivo para modo ponto (7 dias)",
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole() != null ? usuarioLogado.getRole().name() : null,
                null, null, null);
        return ResponseEntity.ok(
                new DeviceVinculoResponseDTO(vinculo.deviceToken(), vinculo.expiraEm()));
    }

    /// Desvincula o dispositivo: revoga todos os vínculos ativos do usuário.
    @PostMapping("/device/revogar")
    public ResponseEntity<Map<String, Object>> revogarDeviceToken(
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        if (usuarioLogado == null) {
            return ResponseEntity.status(401).build();
        }
        int revogados = deviceTokenService.revogarTodos(usuarioLogado.getId());
        auditoriaService.registrar("REVOGACAO_DEVICE_TOKEN", "USUARIO", usuarioLogado.getId(),
                "Revogacao de vinculos de dispositivo do modo ponto",
                usuarioLogado.getTenantId(), usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole() != null ? usuarioLogado.getRole().name() : null,
                null, null, null);
        return ResponseEntity.ok(Map.of("revogados", revogados));
    }
}