package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.LoginRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        var resultado = autenticarUsuarioUseCase.executar(
                new AutenticarUsuarioUseCase.Comando(request.cpf(), request.senha()));
        return ResponseEntity.ok(new LoginResponseDTO(
                resultado.accessToken(), resultado.refreshToken(),
                resultado.role(), resultado.cpcId()));
    }
}
