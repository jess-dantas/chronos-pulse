package br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresaStatus;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.AtualizarStatusLeadUseCase;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.CriarLeadEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.input.ListarLeadsEmpresaUseCase;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.AlterarStatusLeadRequestDTO;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.CriarLeadEmpresaRequestDTO;
import br.com.jess.chronos.pulse.modules.lead.infrastructure.adapters.input.rest.dto.LeadEmpresaResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadControllerTest {

    @Mock private CriarLeadEmpresaUseCase criarLeadEmpresaUseCase;
    @Mock private ListarLeadsEmpresaUseCase listarLeadsEmpresaUseCase;
    @Mock private AtualizarStatusLeadUseCase atualizarStatusLeadUseCase;
    @InjectMocks private LeadController controller;

    private LeadEmpresa leadExemplo() {
        return new LeadEmpresa(
                UUID.randomUUID(), "12345678000199", "Empresa Exemplo LTDA", "João Silva",
                "joao@empresa.com", "1123456789", "11987654321",
                "Rua A", "100", null, "Centro", "São Paulo", "SP", "01001000", null
        );
    }

    @Test
    void deveCriarLeadComSucesso() {
        LeadEmpresa salvo = leadExemplo();
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

    @Test
    void deveListarLeadsOrdenadosMaisRecentesPrimeiro() {
        LeadEmpresa primeiro = leadExemplo();
        LeadEmpresa segundo = leadExemplo();
        when(listarLeadsEmpresaUseCase.executar()).thenReturn(List.of(primeiro, segundo));

        ResponseEntity<List<LeadEmpresaResponseDTO>> response = controller.listarLeads();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).id()).isEqualTo(primeiro.getId());
        assertThat(response.getBody().get(1).id()).isEqualTo(segundo.getId());
    }

    @Test
    void deveAlterarStatusDoLead() {
        LeadEmpresa atualizado = new LeadEmpresa(
                UUID.randomUUID(), "12345678000199", "Empresa Exemplo LTDA", "João Silva",
                "joao@empresa.com", "1123456789", "11987654321",
                "Rua A", "100", null, "Centro", "São Paulo", "SP", "01001000", null,
                LeadEmpresaStatus.REUNIAO, java.time.Instant.now().minusSeconds(60)
        );
        UUID id = atualizado.getId();
        when(atualizarStatusLeadUseCase.executar(eq(id), eq(LeadEmpresaStatus.REUNIAO)))
                .thenReturn(atualizado);

        AlterarStatusLeadRequestDTO request = new AlterarStatusLeadRequestDTO(LeadEmpresaStatus.REUNIAO);
        ResponseEntity<LeadEmpresaResponseDTO> response = controller.alterarStatus(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(id);
        assertThat(response.getBody().status()).isEqualTo("REUNIAO");
    }
}