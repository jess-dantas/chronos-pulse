package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.BuscarPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.CadastrarEmpresaCompletoUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RefreshTokenUseCase;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.CadastrarEmpresaCompletoRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.LoginRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.LoginResponseDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.RefreshTokenRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final CadastrarEmpresaCompletoUseCase cadastrarEmpresaCompletoUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final BuscarPerfilUseCase buscarPerfilUseCase;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                          CadastrarEmpresaCompletoUseCase cadastrarEmpresaCompletoUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          BuscarPerfilUseCase buscarPerfilUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.cadastrarEmpresaCompletoUseCase = cadastrarEmpresaCompletoUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.buscarPerfilUseCase = buscarPerfilUseCase;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("status", "UP", "timestamp", String.valueOf(System.currentTimeMillis())));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        var resultado = autenticarUsuarioUseCase.executar(
                new AutenticarUsuarioUseCase.Comando(request.cpf(), request.senha()));
        return ResponseEntity.ok(new LoginResponseDTO(
                resultado.accessToken(), resultado.refreshToken(),
                resultado.role(), resultado.cpcId(),
                resultado.nome(), resultado.email(),
                resultado.tenantId(), resultado.acessoEstoque()));
    }

    @PostMapping("/cadastrar-empresa")
    public ResponseEntity<LoginResponseDTO> cadastrarEmpresa(
            @RequestBody @Valid CadastrarEmpresaCompletoRequestDTO request) {
        var resultado = cadastrarEmpresaCompletoUseCase.executar(
                new CadastrarEmpresaCompletoUseCase.Comando(
                        request.cnpj(), request.nomeEmpresa(),
                        request.responsavelNome(), request.responsavelCpf(),
                        request.responsavelEmail(), request.responsavelCelular(),
                        request.responsavelSenha()));
        return ResponseEntity.ok(new LoginResponseDTO(
                resultado.accessToken(), resultado.refreshToken(),
                resultado.role(), resultado.cpcId(),
                resultado.nome(), resultado.email(),
                resultado.tenantId(), resultado.acessoEstoque()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO request) {
        var resultado = refreshTokenUseCase.executar(
                new RefreshTokenUseCase.Comando(request.refreshToken()));
        return ResponseEntity.ok(new LoginResponseDTO(
                resultado.accessToken(), null,
                resultado.role(), resultado.cpcId(),
                resultado.nome(), resultado.email(),
                resultado.tenantId(), resultado.acessoEstoque()));
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
}
