package br.com.jess.chronos.pulse.modules.empresa.application.usecases;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase.Comando;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CadastrarEmpresaUseCaseImplTest {

    @Mock
    private EmpresaRepositoryPort repositoryPort;

    private CadastrarEmpresaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CadastrarEmpresaUseCaseImpl(repositoryPort);
    }

    @Test
    void deveCadastrarEmpresaComSucesso() {
        Empresa empresaSalva = new Empresa(UUID.randomUUID(), "12345678000195", "Empresa Exemplo LTDA");
        when(repositoryPort.existePorCnpj("12345678000195")).thenReturn(false);
        when(repositoryPort.salvar(any())).thenReturn(empresaSalva);

        Empresa resultado = useCase.executar(new Comando("12345678000195", "Empresa Exemplo LTDA"));

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCnpj()).isEqualTo("12345678000195");
        assertThat(resultado.getNome()).isEqualTo("Empresa Exemplo LTDA");
        verify(repositoryPort).salvar(any(Empresa.class));
    }

    @Test
    void deveLancarExcecaoQuandoCnpjJaExiste() {
        when(repositoryPort.existePorCnpj("12345678000195")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(new Comando("12345678000195", "Empresa Exemplo LTDA")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ já cadastrado");
        verify(repositoryPort, never()).salvar(any());
    }
}
