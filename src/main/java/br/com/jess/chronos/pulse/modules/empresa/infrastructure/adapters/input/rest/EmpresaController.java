package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest.dto.CadastrarEmpresaRequestDTO;
import br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest.dto.EmpresaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private final CadastrarEmpresaUseCase cadastrarEmpresaUseCase;

    public EmpresaController(CadastrarEmpresaUseCase cadastrarEmpresaUseCase) {
        this.cadastrarEmpresaUseCase = cadastrarEmpresaUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_PLATAFORMA')")
    public ResponseEntity<EmpresaResponseDTO> cadastrar(@RequestBody @Valid CadastrarEmpresaRequestDTO request) {
        var empresa = cadastrarEmpresaUseCase.executar(
                new CadastrarEmpresaUseCase.Comando(request.cnpj(), request.nome()));
        return ResponseEntity.ok(new EmpresaResponseDTO(empresa.getId(), empresa.getCnpj(), empresa.getNome()));
    }
}
