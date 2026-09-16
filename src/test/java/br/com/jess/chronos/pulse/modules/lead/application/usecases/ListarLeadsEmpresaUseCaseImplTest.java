package br.com.jess.chronos.pulse.modules.lead.application.usecases;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
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
class ListarLeadsEmpresaUseCaseImplTest {

    @Mock private LeadEmpresaRepositoryPort repositoryPort;
    @InjectMocks private ListarLeadsEmpresaUseCaseImpl useCase;

    @Test
    void deveDelegarAListaParaORepositorio() {
        LeadEmpresa lead = new LeadEmpresa(
                UUID.randomUUID(), "12345678000199", "Empresa Exemplo LTDA", "João Silva",
                "joao@empresa.com", null, null, null, null, null, null, "São Paulo", "SP", null, null
        );
        when(repositoryPort.listarTodos()).thenReturn(List.of(lead));

        List<LeadEmpresa> resultado = useCase.executar();

        assertThat(resultado).containsExactly(lead);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaohouverLeads() {
        when(repositoryPort.listarTodos()).thenReturn(List.of());

        List<LeadEmpresa> resultado = useCase.executar();

        assertThat(resultado).isEmpty();
    }
}