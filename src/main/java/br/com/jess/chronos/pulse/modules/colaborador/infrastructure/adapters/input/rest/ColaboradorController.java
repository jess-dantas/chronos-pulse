package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
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
    private final AuditoriaService auditoriaService;

    public ColaboradorController(CadastrarColaboradorUseCase cadastrarColaboradorUseCase,
                                 ListarColaboradoresUseCase listarColaboradoresUseCase,
                                 AtualizarColaboradorUseCase atualizarColaboradorUseCase,
                                 ExcluirColaboradorUseCase excluirColaboradorUseCase,
                                 AuditoriaService auditoriaService) {
        this.cadastrarColaboradorUseCase = cadastrarColaboradorUseCase;
        this.listarColaboradoresUseCase = listarColaboradoresUseCase;
        this.atualizarColaboradorUseCase = atualizarColaboradorUseCase;
        this.excluirColaboradorUseCase = excluirColaboradorUseCase;
        this.auditoriaService = auditoriaService;
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
        boolean isPlataforma = usuarioLogado != null && usuarioLogado.getRole() == Role.ADMIN_PLATAFORMA;
        if (!isPlataforma && !tenantId.equals(usuarioLogado.getTenantId())) {
            throw new IllegalArgumentException("Não é permitido cadastrar colaborador em outro tenant.");
        }

        boolean acessoEstoque = Boolean.TRUE.equals(request.acessoEstoque());
        boolean acessoPatrimonio = Boolean.TRUE.equals(request.acessoPatrimonio());
        boolean acessoFrota = Boolean.TRUE.equals(request.acessoFrota());
        boolean acessoProtocolo = Boolean.TRUE.equals(request.acessoProtocolo());

        var colaborador = cadastrarColaboradorUseCase.executar(new CadastrarColaboradorUseCase.Comando(
                request.cpf(), request.nome(), request.emailCorporativo(), request.senha(),
                request.matricula(), request.cargo(), request.departamento(),
                request.dataNascimento(), request.dataAdmissao(), request.dataDesligamento(),
                tenantId, request.configuracaoJornadaId(),
                acessoEstoque, acessoPatrimonio, acessoFrota, acessoProtocolo));

        auditoriaService.registrar("CADASTRO", "COLABORADOR", colaborador.getId(),
                "Cadastro de colaborador " + request.cpf(),
                tenantId, usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null,
                "cpf=" + request.cpf(), null);

        return ResponseEntity.ok(new ColaboradorResponseDTO(
                colaborador.getId(), colaborador.getCpcUsuarioId(), colaborador.getTenantId(),
                colaborador.getMatricula(), colaborador.getCargo(), colaborador.getDepartamento(),
                acessoEstoque, acessoPatrimonio, acessoFrota, acessoProtocolo));
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

        UUID tenantId = usuarioLogado != null ? usuarioLogado.getTenantId() : null;
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID obrigatório.");
        }

        atualizarColaboradorUseCase.executar(new AtualizarColaboradorUseCase.Comando(
                id, tenantId, request.nome(), request.emailCorporativo(),
                request.matricula(), request.cargo(), request.departamento(),
                request.dataNascimento(), request.dataAdmissao(), request.dataDesligamento(),
                Boolean.TRUE.equals(request.acessoEstoque()),
                Boolean.TRUE.equals(request.acessoPatrimonio()),
                Boolean.TRUE.equals(request.acessoFrota()),
                Boolean.TRUE.equals(request.acessoProtocolo())));

        auditoriaService.registrar("ATUALIZACAO", "COLABORADOR", id,
                "Atualização de colaborador",
                tenantId, usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, null, null);

        return ResponseEntity.ok(Map.of("mensagem", "Colaborador atualizado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'ADMIN_EMPRESA', 'GESTOR_RH')")
    public ResponseEntity<Map<String, String>> excluir(
            @PathVariable UUID id,
            @AuthenticationPrincipal CpcUsuario usuarioLogado) {

        UUID tenantId = usuarioLogado != null ? usuarioLogado.getTenantId() : null;
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID obrigatório.");
        }

        excluirColaboradorUseCase.executar(id, tenantId);
        auditoriaService.registrar("EXCLUSAO", "COLABORADOR", id,
                "Desativação de colaborador",
                tenantId, usuarioLogado.getCpcId(), usuarioLogado.getCpf(),
                usuarioLogado.getRole().name(), null, null, null);
        return ResponseEntity.ok(Map.of("mensagem", "Colaborador removido com sucesso"));
    }
}
