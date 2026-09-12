package br.com.jess.chronos.pulse.modules.lead.application.usecases;

import br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresa;
import br.com.jess.chronos.pulse.modules.lead.domain.ports.output.LeadEmpresaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarLeadEmpresaUseCaseImplTest {

    @Mock private LeadEmpresaRepositoryPort repositoryPort;
    @InjectMocks private CriarLeadEmpresaUseCaseImpl useCase;

    private LeadEmpresa leadValido;

    @BeforeEach
    void setUp() {
        leadValido = new LeadEmpresa(
                null, "12345678000199", "Empresa Exemplo LTDA", "João Silva",
                "joao@empresa.com", "1123456789", "11987654321",
                "Rua A", "100", "Sala 1", "Centro", "São Paulo", "SP", "01001000", null
        );
    }

    @Test
    void deveSalvarLeadValido() {
        when(repositoryPort.salvar(any(LeadEmpresa.class))).thenAnswer(inv -> inv.getArgument(0));

        LeadEmpresa salvo = useCase.executar(leadValido);

        assertThat(salvo).isNotSameAs(leadValido);
        assertThat(salvo.getCnpj()).isEqualTo("12345678000199");
        assertThat(salvo.getStatus()).isEqualTo(br.com.jess.chronos.pulse.modules.lead.domain.model.LeadEmpresaStatus.NOVO);
        assertThat(salvo.getCriadoEm()).isNotNull();
        verify(repositoryPort).salvar(any(LeadEmpresa.class));
    }

    @Test
    void deveRejeitarCnpjComQuantidadeErradaDeDigitos() {
        LeadEmpresa cnpjCurto = new LeadEmpresa(
                null, "123", "Empresa Exemplo LTDA", "João Silva",
                "joao@empresa.com", "1123456789", "11987654321",
                "Rua A", "100", null, "Centro", "São Paulo", "SP", "01001000", null
        );

        assertThatThrownBy(() -> useCase.executar(cnpjCurto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ inválido");
    }

    @Test
    void deveAceitarCnpjCorrigindoDigitosMascarados() {
        LeadEmpresa mascarado = new LeadEmpresa(
                null, "12.345.678/0001-99", "Empresa Exemplo LTDA", "João Silva",
                "joao@empresa.com", null, null,
                null, null, null, null, null, null, null, null
        );

        when(repositoryPort.salvar(any(LeadEmpresa.class))).thenAnswer(inv -> inv.getArgument(0));

        LeadEmpresa salvo = useCase.executar(mascarado);

        assertThat(salvo.getCnpj()).isEqualTo("12345678000199");
        verify(repositoryPort).salvar(argThat(l -> "12345678000199".equals(l.getCnpj())));
    }
}