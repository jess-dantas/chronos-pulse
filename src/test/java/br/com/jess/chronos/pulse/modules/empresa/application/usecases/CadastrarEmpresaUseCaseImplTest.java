package br.com.jess.chronos.pulse.modules.empresa.application.usecases;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.input.CadastrarEmpresaUseCase.Comando;
import br.com.jess.chronos.pulse.modules.empresa.domain.ports.output.EmpresaRepositoryPort;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
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

    @Mock
    private ModulosPort modulosPort;

    private CadastrarEmpresaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CadastrarEmpresaUseCaseImpl(repositoryPort, modulosPort);
    }

    @Test
    void deveCadastrarEmpresaComSucesso() {
        Empresa empresaSalva = new Empresa(UUID.randomUUID(), "12345678000195", "Empresa Exemplo LTDA");
        when(repositoryPort.existePorCnpj("12345678000195")).thenReturn(false);
        when(repositoryPort.salvar(any())).thenReturn(empresaSalva);

        Empresa resultado = useCase.executar(new Comando(
                "12345678000195", "Empresa Exemplo LTDA",
                "João da Silva", "joao@empresa.com.br",
                "(11) 1111-1111", "(11) 99999-9999",
                "Rua Exemplo", "100", "Apto 1",
                "Centro", "São Paulo", "SP", "01000-000"));

        assertThat(resultado).isNotNull();
        assertThat(resultado.getCnpj()).isEqualTo("12345678000195");
        assertThat(resultado.getNome()).isEqualTo("Empresa Exemplo LTDA");
        verify(repositoryPort).salvar(any(Empresa.class));
        verify(modulosPort).ativarModulosPadrao(empresaSalva.getId());
    }

    @Test
    void deveLancarExcecaoQuandoCnpjJaExiste() {
        when(repositoryPort.existePorCnpj("12345678000195")).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(new Comando(
                "12345678000195", "Empresa Exemplo LTDA",
                null, null, null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CNPJ já cadastrado");
        verify(repositoryPort, never()).salvar(any());
        verify(modulosPort, never()).ativarModulosPadrao(any());
    }
}