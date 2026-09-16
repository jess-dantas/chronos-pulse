package br.com.jess.chronos.pulse.modules.lead.application.usecases;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresaStatus;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.output.LeadEmpresaRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarStatusLeadUseCaseImplTest {

    @Mock private LeadEmpresaRepositoryPort repositoryPort;
    @InjectMocks private AtualizarStatusLeadUseCaseImpl useCase;

    @Test
    void deveAtualizarStatusEPersistir() {
        UUID id = UUID.randomUUID();
        LeadEmpresa existente = new LeadEmpresa(
                id, "12345678000199", "Empresa Exemplo LTDA", "João Silva",
                "joao@empresa.com", null, null, null, null, null, null, "São Paulo", "SP", null, null
        );
        when(repositoryPort.buscarPorId(id)).thenReturn(java.util.Optional.of(existente));
        when(repositoryPort.salvar(org.mockito.ArgumentMatchers.any(LeadEmpresa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeadEmpresa resultado = useCase.executar(id, LeadEmpresaStatus.AGENDADO);

        assertThat(resultado.getStatus()).isEqualTo(LeadEmpresaStatus.AGENDADO);
        assertThat(resultado.getId()).isEqualTo(id);
    }

    @Test
    void deveLancarErroQuandoLeadNaoExiste() {
        UUID id = UUID.randomUUID();
        when(repositoryPort.buscarPorId(id)).thenReturn(java.util.Optional.empty());

        try {
            useCase.executar(id, LeadEmpresaStatus.DESCARTADO);
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Lead não encontrado.");
            return;
        }
        throw new AssertionError("Deveria lançar IllegalArgumentException.");
    }
}