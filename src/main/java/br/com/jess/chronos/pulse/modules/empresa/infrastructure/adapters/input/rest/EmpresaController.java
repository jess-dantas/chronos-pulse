package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.ListarEmpresasUseCase;
import br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest.dto.CadastrarEmpresaRequestDTO;
import br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest.dto.EmpresaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private final CadastrarEmpresaUseCase cadastrarEmpresaUseCase;
    private final ListarEmpresasUseCase listarEmpresasUseCase;

    public EmpresaController(CadastrarEmpresaUseCase cadastrarEmpresaUseCase,
                             ListarEmpresasUseCase listarEmpresasUseCase) {
        this.cadastrarEmpresaUseCase = cadastrarEmpresaUseCase;
        this.listarEmpresasUseCase = listarEmpresasUseCase;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'SUPORTE_N1', 'SUPORTE_N2')")
    public ResponseEntity<EmpresaResponseDTO> cadastrar(@RequestBody @Valid CadastrarEmpresaRequestDTO request) {
        var empresa = cadastrarEmpresaUseCase.executar(
                new CadastrarEmpresaUseCase.Comando(request.cnpj(), request.nome()));
        return ResponseEntity.ok(EmpresaResponseDTO.fromDomain(empresa));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_PLATAFORMA', 'SUPORTE_N1', 'SUPORTE_N2')")
    public ResponseEntity<List<EmpresaResponseDTO>> listar() {
        var empresas = listarEmpresasUseCase.executar();
        return ResponseEntity.ok(empresas.stream().map(EmpresaResponseDTO::fromDomain).toList());
    }
}
