package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.CriarLeadEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.CriarLeadEmpresaRequestDTO;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.LeadEmpresaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private final CriarLeadEmpresaUseCase criarLeadEmpresaUseCase;

    public LeadController(CriarLeadEmpresaUseCase criarLeadEmpresaUseCase) {
        this.criarLeadEmpresaUseCase = criarLeadEmpresaUseCase;
    }

    @PostMapping("/empresas")
    public ResponseEntity<LeadEmpresaResponseDTO> criarLead(
            @RequestBody @Valid CriarLeadEmpresaRequestDTO request) {
        LeadEmpresa salvo = criarLeadEmpresaUseCase.executar(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(LeadEmpresaResponseDTO.of(salvo));
    }
}