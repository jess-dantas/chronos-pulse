package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.AtualizarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.CadastrarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ExcluirColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ListarColaboradoresUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest.dto.AtualizarColaboradorRequestDTO;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest.dto.CadastrarColaboradorRequestDTO;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest.dto.ColaboradorResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/colaboradores")
public class ColaboradorController {

    private final CadastrarColaboradorUseCase cadastrarColaboradorUseCase;
    private final ListarColaboradoresUseCase listarColaboradoresUseCase;
    private final AtualizarColaboradorUseCase atualizarColaboradorUseCase;
    private final ExcluirColaboradorUseCase excluirColaboradorUseCase;

    public ColaboradorController(CadastrarColaboradorUseCase cadastrarColaboradorUseCase,
                                 ListarColaboradoresUseCase listarColaboradoresUseCase,
                                 AtualizarColaboradorUseCase atualizarColaboradorUseCase,
                                 ExcluirColaboradorUseCase excluirColaboradorUseCase) {
        this.cadastrarColaboradorUseCase = cadastrarColaboradorUseCase;
        this.listarColaboradoresUseCase = listarColaboradoresUseCase;
        this.atualizarColaboradorUseCase = atualizarColaboradorUseCase;
        this.excluirColaboradorUseCase = excluirColaboradorUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')")
    public ResponseEntity<ColaboradorResponseDTO> cadastrar(
            @RequestBody @Valid CadastrarColaboradorRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        
        UUID tenantId = request.tenantId() != null ? request.tenantId() : (usuarioLogado != null ? usuarioLogado.getTenantId() : null);
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID obrigatório para cadastro de colaborador.");
        }

        boolean acessoEstoque = Boolean.TRUE.equals(request.acessoEstoque());

        var colaborador = cadastrarColaboradorUseCase.executar(new CadastrarColaboradorUseCase.Comando(
                request.cpf(), request.nome(), request.emailCorporativo(), request.senha(),
                request.matricula(), request.cargo(), request.departamento(),
                request.dataNascimento(), request.dataAdmissao(),
                tenantId, request.configuracaoJornadaId(), acessoEstoque));

        return ResponseEntity.ok(new ColaboradorResponseDTO(
                colaborador.getId(), colaborador.getCpcUsuarioId(), colaborador.getTenantId(),
                colaborador.getMatricula(), colaborador.getCargo(), colaborador.getDepartamento(),
                acessoEstoque));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')")
    public ResponseEntity<List<ListarColaboradoresUseCase.ColaboradorItem>> listar(
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {
        
        UUID tenantId = (usuarioLogado != null && usuarioLogado.getRole() != Role.ADMIN_PLATAFORMA) 
                ? usuarioLogado.getTenantId() 
                : (usuarioLogado != null ? usuarioLogado.getTenantId() : null);

        var lista = listarColaboradoresUseCase.executar(tenantId);
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')")
    public ResponseEntity<Map<String, String>> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid AtualizarColaboradorRequestDTO request,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        atualizarColaboradorUseCase.executar(new AtualizarColaboradorUseCase.Comando(
                id, request.nome(), request.emailCorporativo(),
                request.matricula(), request.cargo(), request.departamento(),
                request.dataNascimento(), request.dataAdmissao(),
                Boolean.TRUE.equals(request.acessoEstoque())));

        return ResponseEntity.ok(Map.of("mensagem", "Colaborador atualizado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')")
    public ResponseEntity<Map<String, String>> excluir(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        excluirColaboradorUseCase.executar(id);
        return ResponseEntity.ok(Map.of("mensagem", "Colaborador removido com sucesso"));
    }
}
