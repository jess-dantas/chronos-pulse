package br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.CadastrarColaboradorUseCase;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest.dto.CadastrarColaboradorRequestDTO;
import br.com.jess.chronos.pulse.modules.colaborador.infrastructure.adapters.input.rest.dto.ColaboradorResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/colaboradores")
public class ColaboradorController {

    private final CadastrarColaboradorUseCase cadastrarColaboradorUseCase;

    public ColaboradorController(CadastrarColaboradorUseCase cadastrarColaboradorUseCase) {
        this.cadastrarColaboradorUseCase = cadastrarColaboradorUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_EMPRESA')")
    public ResponseEntity<ColaboradorResponseDTO> cadastrar(@RequestBody @Valid CadastrarColaboradorRequestDTO request) {
        var colaborador = cadastrarColaboradorUseCase.executar(new CadastrarColaboradorUseCase.Comando(
                request.cpf(), request.nome(), request.emailCorporativo(), request.senha(),
                request.matricula(), request.cargo(), request.departamento(),
                request.dataNascimento(), request.dataAdmissao(),
                request.tenantId(), request.configuracaoJornadaId()));

        return ResponseEntity.ok(new ColaboradorResponseDTO(
                colaborador.getId(), colaborador.getCpcUsuarioId(), colaborador.getTenantId(),
                colaborador.getMatricula(), colaborador.getCargo(), colaborador.getDepartamento()));
    }
}
