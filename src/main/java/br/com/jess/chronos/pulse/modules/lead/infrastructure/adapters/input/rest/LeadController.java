package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.AtualizarStatusLeadUseCase;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.CriarLeadEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.ListarLeadsEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.AlterarStatusLeadRequestDTO;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.CriarLeadEmpresaRequestDTO;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.LeadEmpresaResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leads")
public class LeadController {

    private static final String ROLES_LEAD =
            "hasAnyRole('ADMIN_PLATAFORMA', 'SUPORTE_N1', 'SUPORTE_N2')";

    private final CriarLeadEmpresaUseCase criarLeadEmpresaUseCase;
    private final ListarLeadsEmpresaUseCase listarLeadsEmpresaUseCase;
    private final AtualizarStatusLeadUseCase atualizarStatusLeadUseCase;

    public LeadController(CriarLeadEmpresaUseCase criarLeadEmpresaUseCase,
                          ListarLeadsEmpresaUseCase listarLeadsEmpresaUseCase,
                          AtualizarStatusLeadUseCase atualizarStatusLeadUseCase) {
        this.criarLeadEmpresaUseCase = criarLeadEmpresaUseCase;
        this.listarLeadsEmpresaUseCase = listarLeadsEmpresaUseCase;
        this.atualizarStatusLeadUseCase = atualizarStatusLeadUseCase;
    }

    @PostMapping("/empresas")
    public ResponseEntity<LeadEmpresaResponseDTO> criarLead(
            @RequestBody @Valid CriarLeadEmpresaRequestDTO request) {
        LeadEmpresa salvo = criarLeadEmpresaUseCase.executar(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(LeadEmpresaResponseDTO.of(salvo));
    }

    @GetMapping
    @PreAuthorize(ROLES_LEAD)
    public ResponseEntity<List<LeadEmpresaResponseDTO>> listarLeads() {
        List<LeadEmpresaResponseDTO> leads = listarLeadsEmpresaUseCase.executar().stream()
                .map(LeadEmpresaResponseDTO::of)
                .toList();
        return ResponseEntity.ok(leads);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(ROLES_LEAD)
    public ResponseEntity<LeadEmpresaResponseDTO> alterarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AlterarStatusLeadRequestDTO request) {
        LeadEmpresa atualizado = atualizarStatusLeadUseCase.executar(id, request.status());
        return ResponseEntity.ok(LeadEmpresaResponseDTO.of(atualizado));
    }
}