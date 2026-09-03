package br.com.jess.chronos.pulse.modules.colaborador.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.colaborador.domain.model.Colaborador;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.input.ListarColaboradoresUseCase.ColaboradorItem;
import br.com.jess.chronos.pulse.modules.colaborador.domain.ports.output.ColaboradorRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarColaboradoresUseCaseImplTest {

    @Mock
    private ColaboradorRepositoryPort colaboradorRepository;

    @Mock
    private CpcUsuarioRepositoryPort usuarioRepository;

    private ListarColaboradoresUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListarColaboradoresUseCaseImpl(colaboradorRepository, usuarioRepository);
    }

    @Test
    void deveListarColaboradoresPorTenantComDetalhesCompletos() {
        UUID tenantId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID colaboradorId = UUID.randomUUID();

        Colaborador colaborador = new Colaborador(
                colaboradorId, usuarioId, tenantId, "MAT100", "Analista", "Administrativo",
                LocalDate.of(1992, 3, 10), LocalDate.of(2024, 1, 15), null
        );

        CpcUsuario usuario = new CpcUsuario(
                usuarioId, UUID.randomUUID(), "11122233344", "Maria Silva",
                "maria@empresa.com", "hash", Role.COLABORADOR, tenantId, true
        );

        when(colaboradorRepository.listarPorTenant(tenantId)).thenReturn(List.of(colaborador));
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

        List<ColaboradorItem> itens = useCase.executar(tenantId);

        assertThat(itens).hasSize(1);
        ColaboradorItem item = itens.get(0);
        assertThat(item.id()).isEqualTo(colaboradorId);
        assertThat(item.nome()).isEqualTo("Maria Silva");
        assertThat(item.cpf()).isEqualTo("11122233344");
        assertThat(item.cargo()).isEqualTo("Analista");
        assertThat(item.departamento()).isEqualTo("Administrativo");
        assertThat(item.acessoEstoque()).isTrue();
        assertThat(item.ativo()).isTrue();
    }
}
