package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AlterarFotoPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AlterarSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.AutenticarUsuarioUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.BuscarPerfilUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.CadastrarEmpresaCompletoUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RedefinirSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.RefreshTokenUseCase;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.SolicitarRecuperacaoSenhaUseCase;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.AlterarSenhaRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.CadastrarEmpresaCompletoRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.EsqueciSenhaRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.LoginRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.LoginResponseDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.RedefinirSenhaRequestDTO;
import br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.input.rest.dto.RefreshTokenRequestDTO;
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

    public AuthController(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                          CadastrarEmpresaCompletoUseCase cadastrarEmpresaCompletoUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          BuscarPerfilUseCase buscarPerfilUseCase,
                          AlterarSenhaUseCase alterarSenhaUseCase,
                          SolicitarRecuperacaoSenhaUseCase solicitarRecuperacaoSenhaUseCase,
                          RedefinirSenhaUseCase redefinirSenhaUseCase,
                          AlterarFotoPerfilUseCase alterarFotoPerfilUseCase) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.cadastrarEmpresaCompletoUseCase = cadastrarEmpresaCompletoUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.buscarPerfilUseCase = buscarPerfilUseCase;
        this.alterarSenhaUseCase = alterarSenhaUseCase;
        this.solicitarRecuperacaoSenhaUseCase = solicitarRecuperacaoSenhaUseCase;
        this.redefinirSenhaUseCase = redefinirSenhaUseCase;
        this.alterarFotoPerfilUseCase = alterarFotoPerfilUseCase;
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
                resultado.tenantId(), resultado.acessoEstoque(),
                resultado.foto()));
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
                resultado.role(), resultado.cpcId(),
                resultado.nome(), resultado.email(),
                resultado.tenantId(), resultado.acessoEstoque(),
                resultado.foto()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO request) {
        var resultado = refreshTokenUseCase.executar(
                new RefreshTokenUseCase.Comando(request.refreshToken()));
        return ResponseEntity.ok(new LoginResponseDTO(
                resultado.accessToken(), null,
                resultado.role(), resultado.cpcId(),
                resultado.nome(), resultado.email(),
                resultado.tenantId(), resultado.acessoEstoque(),
                resultado.foto()));
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
                usuarioLogado.getCpf(), request.novaSenha()));
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
}