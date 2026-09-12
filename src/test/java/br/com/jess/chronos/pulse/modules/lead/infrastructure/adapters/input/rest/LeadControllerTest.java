package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.CriarLeadEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.CriarLeadEmpresaRequestDTO;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.LeadEmpresaResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadControllerTest {

    @Mock private CriarLeadEmpresaUseCase criarLeadEmpresaUseCase;
    @InjectMocks private LeadController controller;

    @Test
    void deveCriarLeadComSucesso() {
        LeadEmpresa salvo = new LeadEmpresa(
                UUID.randomUUID(), "12345678000199", "Empresa Exemplo LTDA", "João Silva",
                "joao@empresa.com", "1123456789", "11987654321",
                "Rua A", "100", null, "Centro", "São Paulo", "SP", "01001000", null
        );
        when(criarLeadEmpresaUseCase.executar(any(LeadEmpresa.class))).thenReturn(salvo);

        CriarLeadEmpresaRequestDTO request = new CriarLeadEmpresaRequestDTO(
                "12345678000199", "Empresa Exemplo LTDA", "João Silva", "joao@empresa.com",
                "1123456789", "11987654321", "Rua A", "100", null, "Centro",
                "São Paulo", "SP", "01001000", null
        );

        ResponseEntity<LeadEmpresaResponseDTO> response = controller.criarLead(request);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody().id()).isEqualTo(salvo.getId());
        assertThat(response.getBody().cnpj()).isEqualTo("12345678000199");
        assertThat(response.getBody().status()).isEqualTo("NOVO");
        assertThat(response.getBody().criadoEm()).isNotNull();
    }
}